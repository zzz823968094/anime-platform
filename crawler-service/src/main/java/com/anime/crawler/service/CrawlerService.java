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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    @Async
    public void crawlNow(Integer type,Integer hour) {
        hour = hour == null ? 24 : hour;
        String firstPageResult = fetchData(CRAWLER_BY_TYPE_URL + type + "&pg=1&h=" + hour);
        Crawler(firstPageResult, type);
    }

    @Async
    public void CrawlerByType(int type) {
        String firstPageResult = fetchData(CRAWLER_BY_TYPE_URL + type + "&pg=1");
        Crawler(firstPageResult, type);
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
            processList(firstList);
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
                                    processList(list);
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
     */
    private String fetchData(String url) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                String result = HttpUtil.get(url, 10000); // 10秒超时
                if (StrUtil.isNotEmpty(result)) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("请求失败,第 {} 次重试: {}", i + 1, url, e);
                try {
                    Thread.sleep(1000 * (i + 1)); // 递增延迟重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 处理动漫列表数据
     */
    public void processList(JSONArray list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        List<AnimeTable> animeBatch = new ArrayList<>(BATCH_SIZE);
        List<Video> allVideos = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            try {
                JSONObject object = JSONUtil.parseObj(list.get(i));
                AnimeTable bean = JSONUtil.toBean(object, AnimeTable.class);
                long id = IdUtil.nextId();
                bean.setId(id);
                String vodContent = object.getStr("vod_play_url");
                if (StrUtil.isNotEmpty(vodContent)) {
                    List<Video> videos = processVideoList(id, vodContent);
                    allVideos.addAll(videos);
                }
                bean.setVodTotal(allVideos.size());
                animeBatch.add(bean);

                // 达到批量大小时执行插入
                if (animeBatch.size() >= BATCH_SIZE) {
                    insertBatch(animeBatch, allVideos);
                    animeBatch.clear();
                    allVideos.clear();
                }
            } catch (Exception e) {
                log.error("处理动漫数据时发生异常", e);
            }
        }

        // 插入剩余数据
        if (!animeBatch.isEmpty()) {
            insertBatch(animeBatch, allVideos);
        }
    }

    /**
     * 批量插入动漫和视频数据
     */
    private void insertBatch(List<AnimeTable> animeList, List<Video> videoList) {
        try {
            if (!animeList.isEmpty()) {
                // 分批插入动漫数据
                List<List<AnimeTable>> animePartitions = CollUtil.split(animeList, BATCH_SIZE);
                for (List<AnimeTable> partition : animePartitions) {
                    animeTableMapper.insert(partition);
                }
            }

            if (!videoList.isEmpty()) {
                // 分批插入视频数据
                List<List<Video>> videoPartitions = CollUtil.split(videoList, BATCH_SIZE);
                for (List<Video> partition : videoPartitions) {
                    videoMapper.insert(partition);
                }
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
                String[] split1 = url.split("\\$", 2); // 限制分割次数为2
                if (split1.length < 2) {
                    log.warn("视频链接格式错误: {}", url);
                    continue;
                }

                Video video = new Video();
                video.setId(IdUtil.nextId());
                video.setTitle(split1[0]);
                video.setAnimeId(animeId);
                video.setStatus(1);
                video.setM3u8Url(BASE_VIDEO_URL + split1[1]);

                // 使用预编译的正则表达式提取数字
                String number = NON_DIGIT_PATTERN.matcher(split1[0]).replaceAll("");
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

}
