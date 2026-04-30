package com.anime.crawler.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.anime.common.utils.IdUtil;
import com.anime.crawler.entity.AnimeTable;
import com.anime.crawler.entity.Video;
import com.anime.crawler.mapper.AnimeTableMapper;
import com.anime.crawler.mapper.VideoMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {
    private static final String BASE_VIDEO_URL = "https://hhzyjiexi.com/play/?url=";
    private static final String CRAWLER_BY_TYPE_URL = "https://hhzyapi.com/api.php/provide/vod/from/hhm3u8/at/json?ac=videolist&t=";
    private static final String CRAWLER_BY_IDS_TYPE_URL = "https://hhzyapi.com/api.php/provide/vod/from/hhm3u8/at/json?ac=videolist&ids=";
    // 批量插入的大小,避免一次性插入过多数据导致内存溢出或数据库连接超时
    private static final int BATCH_SIZE = 100;
    // 预编译正则表达式,避免重复编译
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D+");

    // 线程池用于并发请求
    // 生产环境建议: 根据服务器CPU核心数动态调整,避免过度占用资源影响其他服务
    // 公式: 线程数 = CPU核心数 * (1 + IO等待时间/CPU计算时间)
    // 对于IO密集型任务(如HTTP请求),可以适当增加线程数,但不宜过多
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final AnimeTableMapper animeTableMapper;
    private final VideoMapper videoMapper;
    private final StringRedisTemplate redisTemplate;

    public void crawlById(Set<String> appIps, int type) {
        String requestUrl = CRAWLER_BY_IDS_TYPE_URL + String.join(",", appIps);
        String result = fetchData(requestUrl);
        processList(JSONUtil.parseArray(result), type);
        log.info("爬取成功, 共处理 {} 条数据", appIps.size());
    }

    @Async
    public void crawlNow(Integer type, Integer hour) {
        try {
            hour = hour == null ? 24 : hour;
            String firstPageResult = fetchData(CRAWLER_BY_TYPE_URL + type + "&pg=1&h=" + hour);
            Crawler(firstPageResult, type);
        } catch (Exception e) {
            log.error("爬取类型 {} 失败", type, e);
        }
    }

    @Async
    public void CrawlerByType(int type) {
        try {
            String firstPageResult = fetchData(CRAWLER_BY_TYPE_URL + type + "&pg=1");
            Crawler(firstPageResult, type);
        } catch (Exception e) {
            log.error("爬取类型 {} 失败", type, e);
        }
    }

    /**
     * 根据类型爬取动漫数据
     * 注意: 爬取过程会占用较多系统资源(CPU、网络、数据库连接)
     * 建议在业务低峰期执行,或限制并发线程数以减少对其他服务的影响
     *
     */
    public void Crawler(String firstPageResult, int type) {
        try {
            if (StrUtil.isEmpty(firstPageResult)) {
                log.warn("爬取类型 {} 失败: 第一页数据为空", type);
                return;
            }

            JSONObject firstObject = JSONUtil.parseObj(firstPageResult);
            if (firstObject.getInt("code") != 1) {
                log.warn("爬取类型 {} 失败: API返回错误码 {}", type, firstObject.getInt("code"));
                return;
            }

            int totalPages = firstObject.getInt("pagecount");
            JSONArray firstList = JSONUtil.parseArray(firstObject.get("list"));

            // 处理第一页数据
            processList(firstList, type);
            log.info("类型 {} 第 1/{} 页处理完成", type, totalPages);

            // 并发处理剩余页面
            if (totalPages > 1) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int i = 2; i <= totalPages; i++) {
                    final int currentPage = i;
                    // 添加延迟,避免过快请求导致目标服务器拒绝服务或占用过多本地资源
                    try {
                        Thread.sleep(500); // 每页之间间隔500ms,降低并发压力
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            String result = fetchData(CRAWLER_BY_TYPE_URL + type + "&pg=" + currentPage);
                            if (StrUtil.isNotEmpty(result)) {
                                JSONObject object = JSONUtil.parseObj(result);
                                if (object.getInt("code") == 1) {
                                    JSONArray list = JSONUtil.parseArray(object.get("list"));
                                    processList(list, type);
                                    log.info("类型 {} 第 {}/{} 页处理完成", type, currentPage, totalPages);
                                }
                            }
                        } catch (Exception e) {
                            log.error("类型 {} 第 {} 页处理失败", type, currentPage, e);
                        }
                    }, executorService);
                    futures.add(future);
                }

                // 等待所有并发任务完成
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            log.info("类型 {} 爬取完成,共 {} 页", type, totalPages);
        } catch (Exception e) {
            log.error("爬取类型 {} 时发生异常", type, e);
        }
        // 注意: 不要在此处关闭线程池,线程池应该在应用关闭时统一销毁
        // shutdown()方法仅用于Spring容器销毁Bean时调用
    }

    /**
     * 发起HTTP请求并获取结果,带有重试机制
     * 使用递增延迟重试策略应对网络波动
     */
    private String fetchData(String url) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                // 设置30秒超时,适应较慢的网络环境
                String result = HttpUtil.get(url, 60000);
                if (StrUtil.isNotEmpty(result)) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("请求失败,第 {} 次重试: {}", i + 1, url);
                if (i < maxRetries - 1) {
                    try {
                        // 指数退避: 1s -> 2s -> 4s
                        long delay = 1000L * (long) Math.pow(2, i);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.error("请求最终失败,已达到最大重试次数: {}", url);
        return null;
    }

    /**
     * 处理动漫列表数据
     * 增加去重逻辑:检查vod_total是否更新,避免重复爬取相同数据
     * 优化: 使用批量查询替代循环查询,减少数据库压力
     */
    public void processList(JSONArray list, Integer type) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 第一步: 收集所有需要检查的vodId
        List<Integer> vodIdsToCheck = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            try {
                JSONObject object = JSONUtil.parseObj(list.get(i));
                Integer vodId = object.getInt("vod_id");
                if (vodId != null) {
                    vodIdsToCheck.add(vodId);
                }
            } catch (Exception e) {
                log.error("解析vodId失败", e);
            }
        }

        // 第二步: 批量查询已存在的动漫(一次性查询)
        java.util.Map<Integer, AnimeTable> existingAnimeMap = new java.util.HashMap<>();
        if (!vodIdsToCheck.isEmpty()) {
            List<AnimeTable> existingAnimes = animeTableMapper.selectByVodIds(vodIdsToCheck);
            for (AnimeTable anime : existingAnimes) {
                existingAnimeMap.put(anime.getVodId(), anime);
            }
            log.debug("批量查询到 {} 条已存在的动漫记录", existingAnimeMap.size());
        }

        // 第三步: 处理数据
        List<AnimeTable> newAnimeList = new ArrayList<>(BATCH_SIZE);
        List<AnimeTable> updateAnimeList = new ArrayList<>(BATCH_SIZE);
        List<Video> allVideos = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            AnimeTable bean = null;
            try {
                JSONObject object = JSONUtil.parseObj(list.get(i));
                bean = JSONUtil.toBean(object, AnimeTable.class);

                // 检查该动漫是否已存在(通过vodId从内存Map中查找)
                if (bean.getVodId() != null && existingAnimeMap.containsKey(bean.getVodId())) {
                    AnimeTable existingAnime = existingAnimeMap.get(bean.getVodId());
                    // 计算新爬取的集数
                    String vodContent = object.getStr("vod_play_url");
                    int newEpisodeCount = 0;
                    if (StrUtil.isNotEmpty(vodContent)) {
                        newEpisodeCount = vodContent.split("#").length;
                    }

                    // 如果数据库中已有的集数 >= 新爬取的集数,说明没有更新,跳过
                    if (existingAnime.getVodTotal() != null && existingAnime.getVodTotal() >= newEpisodeCount) {
                        log.debug("动漫 {} 无更新,数据库集数: {}, 新爬取集数: {}, 跳过",
                                bean.getVodName(), existingAnime.getVodTotal(), newEpisodeCount);
                        continue;
                    }

                    // 有更新,使用已有ID
                    bean.setId(existingAnime.getId());
                    updateAnimeList.add(bean);
                    log.info("动漫 {} 有更新,数据库集数: {}, 新爬取集数: {}",
                            bean.getVodName(), existingAnime.getVodTotal(), newEpisodeCount);
                } else {
                    // 不存在,生成新ID
                    long id = IdUtil.nextId();
                    bean.setId(id);
                    newAnimeList.add(bean);
                }

                String vodContent = object.getStr("vod_play_url");
                if (StrUtil.isNotEmpty(vodContent)) {
                    List<Video> videos = processVideoList(bean.getId(), vodContent);
                    bean.setVodTotal(videos.size());
                    allVideos.addAll(videos);
                }

                // 达到批量大小时执行插入
                if (newAnimeList.size() + updateAnimeList.size() >= BATCH_SIZE) {
                    insertBatch(newAnimeList, updateAnimeList, allVideos);
                    newAnimeList.clear();
                    updateAnimeList.clear();
                    allVideos.clear();
                }
            } catch (Exception e) {
                log.error("处理动漫数据时发生异常", e);
                // 记录失败的vodId到Redis
                if (bean != null && bean.getVodId() != null) {
                    recordFailedVodId(type, bean.getVodId());
                }
            }
        }

        // 插入剩余数据
        if (!newAnimeList.isEmpty() || !updateAnimeList.isEmpty() || !allVideos.isEmpty()) {
            insertBatch(newAnimeList, updateAnimeList, allVideos);
        }
    }

    /**
     * 批量插入动漫和视频数据
     * 使用INSERT IGNORE避免主键冲突异常
     * 对于已存在的动漫(通过vodId判断),执行更新操作
     * 优化: 直接接收已分类的新增和更新列表,无需再次查询数据库
     */
    private void insertBatch(List<AnimeTable> newAnimeList, List<AnimeTable> updateAnimeList, List<Video> videoList) {
        try {
            // 分批插入新动漫数据,忽略重复记录
            if (!newAnimeList.isEmpty()) {
                List<List<AnimeTable>> newPartitions = CollUtil.split(newAnimeList, BATCH_SIZE);
                for (List<AnimeTable> partition : newPartitions) {
                    animeTableMapper.insertBatchIgnore(partition);
                }
                log.info("批量新增动漫: {} 条", newAnimeList.size());
            }

            // 分批更新已有动漫数据
            if (!updateAnimeList.isEmpty()) {
                List<List<AnimeTable>> updatePartitions = CollUtil.split(updateAnimeList, BATCH_SIZE);
                for (List<AnimeTable> partition : updatePartitions) {
                    animeTableMapper.updateBatchById(partition);
                }
                log.info("批量更新动漫: {} 条", updateAnimeList.size());
            }

            if (!videoList.isEmpty()) {
                // 分批插入视频数据,忽略重复记录
                List<List<Video>> videoPartitions = CollUtil.split(videoList, BATCH_SIZE);
                for (List<Video> partition : videoPartitions) {
                    videoMapper.insertBatchIgnore(partition);
                }
                log.info("批量插入视频: {} 条", videoList.size());
            }
        } catch (Exception e) {
            log.error("批量插入数据时发生异常", e);
        }

    }

    /**
     * 处理视频列表
     */
    public List<Video> processVideoList(Long animeId, String vodContent) {
        List<Video> list = new ArrayList<>();
        String[] split = vodContent.split("#");

        for (String url : split) {
            try {
                // 先去除首尾空白字符和制表符,避免分割失败
                String trimmedUrl = url.trim();
                if (StrUtil.isEmpty(trimmedUrl)) {
                    continue;
                }

                String[] split1 = trimmedUrl.split("\\$", 2); // 限制分割次数为2
                if (split1.length < 2) {
                    log.warn("视频链接格式错误: {}", url);
                    continue;
                }

                // 清理标题中的空白字符和制表符
                String title = split1[0].trim().replaceAll("\\s+", "");
                String videoUrl = split1[1].trim();

                if (StrUtil.isEmpty(title) || StrUtil.isEmpty(videoUrl)) {
                    log.warn("视频标题或URL为空: {}", url);
                    continue;
                }

                Video video = new Video();
                video.setId(IdUtil.nextId());
                video.setTitle(title);
                video.setAnimeId(animeId);
                video.setStatus(1);
                video.setM3u8Url(BASE_VIDEO_URL + videoUrl);

                // 使用预编译的正则表达式提取数字
                String number = NON_DIGIT_PATTERN.matcher(title).replaceAll("");
                if (StrUtil.isNotEmpty(number)) {
                    video.setEpisode(Integer.parseInt(number));
                } else {
                    video.setEpisode(0);
                }

                list.add(video);
            } catch (Exception e) {
                log.error("处理视频链接时发生异常: {}", url, e);
            }
        }

        return list;
    }

    /**
     * 优雅关闭线程池(在应用关闭时由Spring容器调用)
     * 使用@PreDestroy注解确保Bean销毁时正确释放资源
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭线程池");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.info("线程池关闭失败，重新关闭中");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("线程池已关闭");
    }

    /**
     * 记录失败的爬虫任务到Redis
     * KEY格式: crawler:failed:{type}
     * VALUE格式: Set集合,存储失败的vodId列表
     *
     * @param type  爬虫类型
     * @param vodId 失败的vodId
     */
    private void recordFailedVodId(Integer type, Integer vodId) {
        try {
            String redisKey = "crawler:failed:" + type;
            // 将失败的vodId添加到Set中,自动去重
            redisTemplate.opsForSet().add(redisKey, String.valueOf(vodId));
            // 设置过期时间为7天
            redisTemplate.expire(redisKey, 7, java.util.concurrent.TimeUnit.DAYS);
            log.debug("已记录失败的vodId到Redis: key={}, vodId={}", redisKey, vodId);
        } catch (Exception e) {
            log.error("记录失败的vodId到Redis失败", e);
        }
    }

}
