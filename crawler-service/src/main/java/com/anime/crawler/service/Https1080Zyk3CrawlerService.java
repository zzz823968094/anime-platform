package com.anime.crawler.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.anime.common.exception.BusinessException;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 1080资源站爬虫服务（优化版）
 * API文档: https://api.yzzy-api.com/
 * <p>
 * 优化策略：
 * 1. 使用线程池并发获取详情数据
 * 2. 批量查询去重，减少数据库压力
 * 3. 增量更新，只处理有变化的数据
 * 4. 合理的批次大小和延迟控制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Https1080Zyk3CrawlerService {
    private static final String VIDEO_LIST_URL = "https://api.yzzy-api.com/inc/api_mac10.php?ac=list";
    private static final String VIDEO_DETAIL_URL = "https://api.yzzy-api.com/inc/api_mac10.php?ac=detail&ids=";
    private static final String VIDEO_UPDATE_BY_HOUR = "https://api.yzzy-api.com/inc/api_mac10.php?ac=detail&h=";

    // 批量处理大小
    private static final int BATCH_SIZE = 100;           // 数据库批量插入大小
    private static final int DETAIL_BATCH_SIZE = 20;     // 详情接口每批ID数量
    private static final int MAX_CONCURRENT = 5;         // 最大并发数

    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D+");

    // 线程池用于并发获取详情数据
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            MAX_CONCURRENT,
            r -> {
                Thread thread = new Thread(r);
                thread.setName("yzzy-crawler-" + thread.getId());
                thread.setDaemon(true);
                return thread;
            }
    );

    private final AnimeTableMapper animeTableMapper;
    private final VideoMapper videoMapper;
    private final ApiDataSyncService apiDataSyncService;

    /**
     * 使用代理发送HTTP请求
     * @param url 请求URL
     * @param timeout 超时时间（毫秒）
     * @return 响应内容
     */
    private String getWithProxy(String url, int timeout) {
        try {
            HttpResponse response = HttpRequest.get(url)
                    .timeout(timeout)
                    .execute();
            return response.body();
        } catch (Exception e) {
            log.error("HTTP请求失败: {}", url, e);
            throw e;
        }
    }

    /**
     * 通用的API数据获取和同步方法
     * @param apiUrl API地址
     * @param description 操作描述（用于日志）
     */
    private void fetchAndSyncData(String apiUrl, String description) {
        try {
            log.info("开始{}", description);
            
            String result = getWithProxy(apiUrl, 60000);
            if (StrUtil.isEmpty(result)) {
                log.warn("API返回数据为空: {}", apiUrl);
                return;
            }
            
            if (!JSONUtil.isTypeJSON(result)) {
                log.warn("API返回数据格式错误: {}", apiUrl);
                return;
            }
            
            // 使用ApiDataSyncService进行数据同步
            apiDataSyncService.syncFromApiResponse(result);
            log.info("{}完成", description);
            
        } catch (Exception e) {
            log.error("{}失败", description, e);
            throw new BusinessException(description + "失败: " + e.getMessage());
        }
    }

    /**
     * 根据小时增量更新视频列表（处理所有页面）
     * @param type 类型ID
     * @param hour 小时数
     */
    public void updateVideoListByHour(Integer type, Integer hour) {
        try {
            String url = VIDEO_UPDATE_BY_HOUR + hour + "&t=" + type;
            log.info("开始获取最近{}小时内更新的视频数据(type={})", hour, type);
            
            // 获取第一页数据
            String result = getWithProxy(url + "&pg=1", 60000);
            if (StrUtil.isEmpty(result)) {
                log.warn("API返回数据为空");
                return;
            }
            
            if (!JSONUtil.isTypeJSON(result)) {
                log.warn("API返回数据格式错误");
                return;
            }
            
            // 使用ApiDataSyncService进行数据同步
            try {
                apiDataSyncService.syncFromApiResponse(result);
            } catch (Exception e) {
                log.error("第1页数据同步失败，但继续处理后续页面", e);
            }
            
            // 解析总页数，继续处理后续页面
            JSONObject resultObj = JSONUtil.parseObj(result);
            log.info("第一页响应结果: {}", resultObj.toString().substring(0, Math.min(200, resultObj.toString().length())));
            Integer pageCount = resultObj.getInt("pagecount");
            log.info("解析到总页数: {}", pageCount);
            
            if (pageCount != null && pageCount > 1) {
                log.info("共{}页，开始处理第2-{}页", pageCount, pageCount);
                // 处理第2页到最后一页
                for (int page = 2; page <= pageCount; page++) {
                    try {
                        Thread.sleep(500); // 页间延迟
                        String nextPageUrl = VIDEO_UPDATE_BY_HOUR + hour + "&t=" + type + "&pg=" + page;
                        String nextPageResult = getWithProxy(nextPageUrl, 60000);
                        
                        if (StrUtil.isNotEmpty(nextPageResult) && JSONUtil.isTypeJSON(nextPageResult)) {
                            try {
                                apiDataSyncService.syncFromApiResponse(nextPageResult);
                                log.info("已处理第 {}/{} 页", page, pageCount);
                            } catch (Exception e) {
                                log.error("第 {} 页数据同步失败，但继续处理后续页面", page, e);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("线程被中断", e);
                        break;
                    } catch (Exception e) {
                        log.error("获取第 {} 页失败", page, e);
                    }
                }
            }
            
            log.info("获取最近{}小时内更新的视频数据完成", hour);
            
        } catch (Exception e) {
            log.error("更新视频列表失败", e);
            throw new BusinessException("更新视频列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频列表并处理数据（入口方法 - 处理所有页面）
     *
     * @param type 类型ID
     * @param page 起始页码（通常为1）
     */
    @Async
    public void getVideoList(Integer type, Integer page) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("开始爬取type={}的数据，从第{}页开始", type, page);
            
            // 获取第一页数据
            String url = VIDEO_LIST_URL + "&t=" + type + "&pg=" + page;
            String result = getWithProxy(url, 60000);
            
            if (StrUtil.isEmpty(result)) {
                log.warn("API返回数据为空");
                return;
            }
            
            if (!JSONUtil.isTypeJSON(result)) {
                log.warn("API返回数据格式错误");
                return;
            }
            
            // 使用ApiDataSyncService进行数据同步
            try {
                apiDataSyncService.syncFromApiResponse(result);
            } catch (Exception e) {
                log.error("第{}页数据同步失败，但继续处理后续页面", page, e);
            }
            
            // 解析总页数，继续处理后续页面
            JSONObject resultObj = JSONUtil.parseObj(result);
            Integer pageCount = resultObj.getInt("pagecount");
            log.info("解析到总页数: {}", pageCount);
            
            if (pageCount != null && pageCount > page) {
                log.info("共{}页，开始处理第{}-{}页", pageCount, page + 1, pageCount);
                // 处理后续页面
                for (int currentPage = page + 1; currentPage <= pageCount; currentPage++) {
                    log.info("开始处理第 {} 页", currentPage);
                    try {
                        Thread.sleep(500); // 页间延迟
                        String nextPageUrl = VIDEO_LIST_URL + "&t=" + type + "&pg=" + currentPage;
                        String nextPageResult = getWithProxy(nextPageUrl, 60000);
                        
                        if (StrUtil.isNotEmpty(nextPageResult) && JSONUtil.isTypeJSON(nextPageResult)) {
                            try {
                                apiDataSyncService.syncFromApiResponse(nextPageResult);
                                log.info("已处理第 {}/{} 页", currentPage, pageCount);
                            } catch (Exception e) {
                                log.error("第 {} 页数据同步失败，但继续处理后续页面", currentPage, e);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("线程被中断", e);
                        break;
                    } catch (Exception e) {
                        log.error("获取第 {} 页失败", currentPage, e);
                    }
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
            log.info("爬取完成，耗时: {}时{}分{}秒", hours, minutes, seconds);
        } catch (Exception e) {
            log.error("爬取失败", e);
            throw new BusinessException("爬取失败: " + e.getMessage());
        }
    }

    /**
     * 异步处理列表数据
     */
    @Async
    public void processListAsync(Integer type, String result) {
        passData(type, result);
    }

    /**
     * 处理列表数据，批量获取详情并保存（优化版）
     *
     * @param type   类型ID
     * @param result JSON结果
     */
    public void passData(Integer type, String result) {
        JSONObject resultObj = JSONUtil.parseObj(result);
        Integer code = resultObj.getInt("code");
        if (code != 1) {
            log.error("API返回错误码: {}", code);
            return;
        }

        Integer page = resultObj.getInt("page");
        Integer pageCount = resultObj.getInt("pagecount");
        Integer total = resultObj.getInt("total");
        JSONArray list = resultObj.getJSONArray("list");

        if (list == null || list.isEmpty()) {
            log.warn("第 {} 页数据为空", page);
            return;
        }

        log.info("处理第 {}/{} 页，总数: {}, 本页: {} 条", page, pageCount, total, list.size());

        // 收集所有vodId
        List<Integer> vodIds = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            JSONObject object = list.getJSONObject(i);
            Integer vodId = object.getInt("vod_id");
            if (vodId != null) {
                vodIds.add(vodId);
            }
        }

        if (vodIds.isEmpty()) {
            log.warn("没有有效的vodId");
            return;
        }

        // 第一步：批量查询已存在的记录（一次性查询，减少DB压力）
        Map<Integer, AnimeTable> existingAnimeMap = batchQueryExisting(vodIds);

        // 第二步：筛选需要更新的ID（只获取有更新或新增的数据）
        List<Integer> needFetchIds = filterNeedUpdate(vodIds, existingAnimeMap, list);

        if (needFetchIds.isEmpty()) {
            log.info("第 {} 页无需更新的数据", page);
        } else {
            log.info("需要获取详情的ID数量: {}", needFetchIds.size());
            // 第三步：并发批量获取详情数据
            fetchDetailsConcurrently(needFetchIds, type, existingAnimeMap);
        }

        // 递归处理下一页
        if (page < pageCount) {
            try {
                Thread.sleep(500); // 页间延迟降低到500ms
                String nextPageUrl = VIDEO_LIST_URL + "&t=" + type + "&pg=" + (page + 1);
                String nextPageResult = getWithProxy(nextPageUrl, 60000);
                if (StrUtil.isNotEmpty(nextPageResult) && JSONUtil.isTypeJSON(nextPageResult)) {
                    passData(type, nextPageResult);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
            } catch (Exception e) {
                log.error("获取第 {} 页失败", page + 1, e);
            }
        } else {
            log.info("所有页面处理完成");
        }
    }

    /**
     * 批量查询已存在的动漫记录
     */
    private Map<Integer, AnimeTable> batchQueryExisting(List<Integer> vodIds) {
        if (vodIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AnimeTable> existingAnimes = animeTableMapper.selectByVodIds(vodIds);
        Map<Integer, AnimeTable> map = new HashMap<>(existingAnimes.size());
        for (AnimeTable anime : existingAnimes) {
            map.put(anime.getVodId(), anime);
        }
        log.debug("批量查询到 {} 条已存在记录", map.size());
        return map;
    }

    /**
     * 筛选需要更新的ID（优化：避免获取无变化的详情）
     */
    private List<Integer> filterNeedUpdate(List<Integer> vodIds,
                                           Map<Integer, AnimeTable> existingMap,
                                           JSONArray list) {
        List<Integer> needFetch = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            JSONObject object = list.getJSONObject(i);
            Integer vodId = object.getInt("vod_id");
            if (vodId == null) continue;

            // 新数据，需要获取详情
            if (!existingMap.containsKey(vodId)) {
                needFetch.add(vodId);
                continue;
            }

            // 已有数据，检查是否有更新（通过vod_time判断）
            AnimeTable existing = existingMap.get(vodId);
            String newTime = object.getStr("vod_time");
            if (StrUtil.isNotEmpty(newTime) && existing.getVodTime() != null) {
                // 如果更新时间不同，可能需要更新
                if (!newTime.equals(existing.getVodTime().toString())) {
                    needFetch.add(vodId);
                }
            } else {
                // 无法判断，保守起见获取详情
                needFetch.add(vodId);
            }
        }

        return needFetch;
    }

    /**
     * 并发获取详情数据
     */
    private void fetchDetailsConcurrently(List<Integer> vodIds, Integer type,
                                          Map<Integer, AnimeTable> existingMap) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < vodIds.size(); i += DETAIL_BATCH_SIZE) {
            int endIndex = Math.min(i + DETAIL_BATCH_SIZE, vodIds.size());
            List<Integer> batchIds = vodIds.subList(i, endIndex);
            String idsStr = batchIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    getDetailAndSave(idsStr, existingMap);
                } catch (Exception e) {
                    log.error("批量获取详情失败，IDs: {}", idsStr, e);
                }
            }, executorService);

            futures.add(future);

            // 控制并发节奏，避免瞬间大量请求
            if (futures.size() >= MAX_CONCURRENT) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                futures.clear();
                try {
                    Thread.sleep(200); // 每批之间短暂延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 等待剩余任务完成
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    /**
     * 获取详情数据并保存到数据库（优化版）
     *
     * @param ids         视频ID列表（逗号分隔）
     * @param existingMap 已存在的动漫映射表
     */
    public void getDetailAndSave(String ids, Map<Integer, AnimeTable> existingMap) {
        try {
            String url = VIDEO_DETAIL_URL + ids;
            String result = getWithProxy(url, 60000);

            if (StrUtil.isEmpty(result) || !JSONUtil.isTypeJSON(result)) {
                log.warn("详情数据无效，IDs: {}", ids);
                return;
            }

            JSONObject resultObj = JSONUtil.parseObj(result);
            if (resultObj.getInt("code") != 1) {
                log.warn("详情接口返回错误码: {}", resultObj.getInt("code"));
                return;
            }

            JSONArray list = resultObj.getJSONArray("list");
            if (list == null || list.isEmpty()) {
                return;
            }

            // 处理详情数据
            List<AnimeTable> newAnimeList = new ArrayList<>();
            List<AnimeTable> updateAnimeList = new ArrayList<>();
            List<Video> allVideos = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                try {
                    JSONObject object = list.getJSONObject(i);
                    AnimeTable bean = JSONUtil.toBean(object, AnimeTable.class);

                    if (bean.getVodId() == null) {
                        continue;
                    }

                    // 检查是否需要更新
                    if (existingMap.containsKey(bean.getVodId())) {
                        AnimeTable existingAnime = existingMap.get(bean.getVodId());

                        // 计算新爬取的集数
                        String vodPlayUrl = object.getStr("vod_play_url");
                        int newEpisodeCount = 0;
                        if (StrUtil.isNotEmpty(vodPlayUrl)) {
                            newEpisodeCount = vodPlayUrl.split("#").length;
                        }

                        // 如果数据库中已有的集数 >= 新爬取的集数，跳过
                        if (existingAnime.getVodTotal() != null && existingAnime.getVodTotal() >= newEpisodeCount) {
                            continue;
                        }

                        // 有更新，使用已有ID
                        bean.setId(existingAnime.getId());
                        updateAnimeList.add(bean);
                    } else {
                        // 不存在，生成新ID
                        bean.setId(IdUtil.nextId());
                        newAnimeList.add(bean);
                    }

                    // 处理视频列表
                    String vodPlayUrl = object.getStr("vod_play_url");
                    if (StrUtil.isNotEmpty(vodPlayUrl)) {
                        List<Video> videos = processVideoList(bean.getId(), vodPlayUrl);
                        bean.setVodTotal(videos.size());
                        allVideos.addAll(videos);
                    }
                } catch (Exception e) {
                    log.error("处理详情数据异常", e);
                }
            }

            // 批量插入/更新
            if (!newAnimeList.isEmpty() || !updateAnimeList.isEmpty() || !allVideos.isEmpty()) {
                insertBatch(newAnimeList, updateAnimeList, allVideos);
            }

        } catch (Exception e) {
            log.error("获取详情数据失败，IDs: {}", ids, e);
        }
    }

    /**
     * 处理视频列表，解析播放地址（优化版）
     */
    private List<Video> processVideoList(Long animeId, String vodPlayUrl) {
        List<Video> list = new ArrayList<>();
        String[] episodes = vodPlayUrl.split("#");
        LocalDateTime now = LocalDateTime.now();

        for (String episode : episodes) {
            try {
                String trimmedEpisode = episode.trim();
                if (StrUtil.isEmpty(trimmedEpisode)) {
                    continue;
                }

                String[] parts = trimmedEpisode.split("\\$", 2);
                if (parts.length < 2) {
                    continue;
                }

                String title = parts[0].trim();
                String videoUrl = parts[1].trim();

                if (StrUtil.isEmpty(title) || StrUtil.isEmpty(videoUrl)) {
                    continue;
                }

                Video video = new Video();
                video.setId(IdUtil.nextId());
                video.setTitle(title);
                video.setAnimeId(animeId);
                video.setStatus(1);
                video.setM3u8Url(videoUrl);
                video.setCreatedAt(now);
                video.setUpdatedAt(now);

                // 提取集数
                String number = NON_DIGIT_PATTERN.matcher(title).replaceAll("");
                if (StrUtil.isNotEmpty(number)) {
                    try {
                        video.setEpisode(Integer.parseInt(number));
                    } catch (NumberFormatException e) {
                        video.setEpisode(0);
                    }
                } else {
                    video.setEpisode(0);
                }

                list.add(video);
            } catch (Exception e) {
                // 单条失败不影响其他数据
            }
        }

        return list;
    }

    /**
     * 批量插入动漫和视频数据（优化版）
     */
    private void insertBatch(List<AnimeTable> newAnimeList, List<AnimeTable> updateAnimeList, List<Video> videoList) {
        try {
            int newCount = 0, updateCount = 0, videoCount = 0;

            // 分批插入新动漫数据
            if (!newAnimeList.isEmpty()) {
                List<List<AnimeTable>> partitions = CollUtil.split(newAnimeList, BATCH_SIZE);
                for (List<AnimeTable> partition : partitions) {
                    newCount += animeTableMapper.insertBatchIgnore(partition);
                }
            }

            // 分批更新已有动漫数据
            if (!updateAnimeList.isEmpty()) {
                List<List<AnimeTable>> partitions = CollUtil.split(updateAnimeList, BATCH_SIZE);
                for (List<AnimeTable> partition : partitions) {
                    updateCount += animeTableMapper.updateBatchById(partition);
                }
            }

            // 分批插入视频数据
            if (!videoList.isEmpty()) {
                List<List<Video>> partitions = CollUtil.split(videoList, BATCH_SIZE);
                for (List<Video> partition : partitions) {
                    videoCount += videoMapper.insertBatchIgnore(partition);
                }
            }

            if (newCount > 0 || updateCount > 0 || videoCount > 0) {
                log.info("批量保存完成 - 新增: {}, 更新: {}, 视频: {}", newCount, updateCount, videoCount);
            }
        } catch (Exception e) {
            log.error("批量插入数据异常", e);
        }
    }

    /**
     * 优雅关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭爬虫线程池");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("爬虫线程池已关闭");
    }

}
