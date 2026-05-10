package com.anime.crawler.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Joiner;
import com.anime.common.exception.BusinessException;
import com.anime.common.utils.IdUtil;
import com.anime.crawler.entity.AnimeTable;
import com.anime.crawler.entity.Video;
import com.anime.crawler.mapper.AnimeTableMapper;
import com.anime.crawler.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private static final String VIDEO_LIST_URL = "https://api.yzzy-api.com/inc/apijson.php?ac=list";
    private static final String VIDEO_DETAIL_URL = "https://api.yzzy-api.com/inc/apijson.php?ac=detail&ids=";
    private static final String VIDEO_UPDATE_BY_HOUR = "https://api.yzzy-api.com/inc/apijson.php?ac=detail&h=";

    private final AnimeTableMapper animeTableMapper;
    private final VideoMapper videoMapper;

    public void clawerByHour(Integer type, Integer hour, Integer page) {
        String url = VIDEO_UPDATE_BY_HOUR + hour + "&t=" + type + "&pg=" + page;
        JSONObject listResult = httpGet(url);
        Integer pagecount = listResult.getInt("pagecount");
        JSONArray detailJsonArray = listResult.getJSONArray("list");
        processAnimeData(detailJsonArray);
        if (page < pagecount) {
            clawerByHour(type, hour, page + 1);  // 修复：应该递归调用clawerByHour
        }
    }

    public void clawerByType(Integer type, Integer page) {
        String url = VIDEO_LIST_URL + "&t=" + type + "&pg=" + page;
        JSONObject listResult = httpGet(url);
        Integer pagecount = listResult.getInt("pagecount");
        JSONArray jsonArray = listResult.getJSONArray("list");
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            Integer vodId = item.getInt("vod_id");
            ids.add(vodId);
        }
        String idsString = Joiner.on(",").join(ids);
        JSONObject detailResult = httpGet(VIDEO_DETAIL_URL + idsString);
        JSONArray detailJsonArray = detailResult.getJSONArray("list");
        processAnimeData(detailJsonArray);
        if (page < pagecount) {
            clawerByType(type, page + 1);
        }
    }

    /**
     * 处理动漫数据，根据vodId判断是新增还是更新
     * 优化：使用批量查询替代逐条查询，减少数据库交互
     *
     * @param detailJsonArray 动漫详情JSON数组
     */
    public void processAnimeData(JSONArray detailJsonArray) {
        if (detailJsonArray == null || detailJsonArray.isEmpty()) {
            return;
        }

        List<AnimeTable> insertList = new ArrayList<>();
        List<AnimeTable> updateList = new ArrayList<>();

        // 第一步：收集所有vodId
        List<Integer> vodIds = new ArrayList<>(detailJsonArray.size());
        for (int i = 0; i < detailJsonArray.size(); i++) {
            JSONObject item = detailJsonArray.getJSONObject(i);
            Integer vodId = item.getInt("vod_id");
            if (vodId != null) {
                vodIds.add(vodId);
            }
        }

        // 第二步：批量查询已存在的动漫（关键优化：避免N+1查询）
        java.util.Map<Integer, AnimeTable> existingMap = new java.util.HashMap<>();
        if (!vodIds.isEmpty()) {
            List<AnimeTable> existingAnimes = animeTableMapper.selectByVodIds(vodIds);
            for (AnimeTable existing : existingAnimes) {
                existingMap.put(existing.getVodId(), existing);
            }
            log.debug("批量查询到 {} 条已存在的动漫记录", existingMap.size());
        }

        // 第三步：处理每条数据
        for (int i = 0; i < detailJsonArray.size(); i++) {
            JSONObject item = detailJsonArray.getJSONObject(i);
            String vodPlayUrl = item.getStr("vod_play_url");
            Integer vodId = item.getInt("vod_id");

            // 映射动漫数据
            AnimeTable animeTable = mapJsonToAnimeTable(item);

            // 从内存Map中查找是否已存在（O(1)复杂度）
            AnimeTable existingAnime = vodId != null ? existingMap.get(vodId) : null;

            if (existingAnime != null) {
                // 存在则使用原有ID，进行更新
                animeTable.setId(existingAnime.getId());
                updateList.add(animeTable);
            } else {
                // 不存在则生成新ID，进行插入
                long newId = IdUtil.nextId();
                animeTable.setId(newId);
                insertList.add(animeTable);
            }

            // 处理视频数据，获取集数
            if (animeTable.getId() != null && vodPlayUrl != null && !vodPlayUrl.isEmpty()) {
                Integer count = processVideoData(vodPlayUrl, animeTable.getId());
                animeTable.setVodTotal(count);
                log.info("动漫 {} (ID: {}, vodId: {}) 解析到 {} 集视频",
                        animeTable.getVodName(), animeTable.getId(), vodId, count);
            } else {
                animeTable.setVodTotal(0);
            }
        }

        // 第四步：批量插入新数据
        batchInsertAnime(insertList);

        // 第五步：批量更新已有数据
        batchUpdateAnime(updateList);
    }

    /**
     * 批量插入动漫数据
     */
    private void batchInsertAnime(List<AnimeTable> insertList) {
        if (insertList.isEmpty()) {
            return;
        }

        try {
            int batchSize = 100;
            for (int i = 0; i < insertList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, insertList.size());
                List<AnimeTable> batch = insertList.subList(i, end);
                animeTableMapper.insertBatchIgnore(batch);
            }
            log.info("批量新增动漫数据: {} 条", insertList.size());
        } catch (Exception e) {
            log.error("批量插入动漫数据失败", e);
        }
    }

    /**
     * 批量更新动漫数据
     */
    private void batchUpdateAnime(List<AnimeTable> updateList) {
        if (updateList.isEmpty()) {
            return;
        }

        try {
            int batchSize = 100;
            for (int i = 0; i < updateList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, updateList.size());
                List<AnimeTable> batch = updateList.subList(i, end);
                animeTableMapper.updateBatchById(batch);
            }
            log.info("批量更新动漫数据: {} 条", updateList.size());
        } catch (Exception e) {
            log.error("批量更新动漫数据失败", e);
        }
    }

    /**
     * 处理视频数据，从vod_play_url中解析集数和播放地址
     * 格式：第01集$https://...#第02集$https://...
     *
     * @param vodPlayUrl 播放URL字符串
     * @param animeId    动漫ID
     * @return 解析后的视频数量
     */
    public Integer processVideoData(String vodPlayUrl, Long animeId) {
        if (vodPlayUrl == null || vodPlayUrl.trim().isEmpty()) {
            log.warn("播放URL为空，animeId: {}", animeId);
            return 0;
        }

        String trim = vodPlayUrl.trim();
        List<Video> videoList = new ArrayList<>();

        try {
            // 按 # 分割不同的集数
            String[] episodes = trim.split("#");

            for (int i = 0; i < episodes.length; i++) {
                String episodeStr = episodes[i].trim();
                if (episodeStr.isEmpty()) {
                    continue;
                }

                // 按 $ 分割集数标题和URL
                String[] parts = episodeStr.split("\\$");
                if (parts.length != 2) {
                    log.warn("视频数据格式错误: {}", episodeStr);
                    continue;
                }

                String title = parts[0].trim();  // 例如：第01集
                String m3u8Url = parts[1].trim(); // 播放地址

                // 创建Video对象
                Video video = new Video();
                video.setId(IdUtil.nextId());
                video.setAnimeId(animeId);
                video.setEpisode(i + 1);  // 集数从1开始
                video.setTitle(title);
                video.setM3u8Url(m3u8Url);
                video.setStatus(1);  // 默认状态为正常
                video.setViewCount(0);
                video.setCreatedAt(LocalDateTime.now());
                video.setUpdatedAt(LocalDateTime.now());

                videoList.add(video);
            }

            // 批量插入或更新视频数据
            if (!videoList.isEmpty()) {
                videoMapper.insertBatchOrUpdate(videoList);
                log.info("动漫 {} 成功保存 {} 集视频数据", animeId, videoList.size());
            }

            return videoList.size();

        } catch (Exception e) {
            log.error("解析视频数据失败，animeId: {}", animeId, e);
            return 0;
        }
    }

    /**
     * HTTP GET请求，带重试机制和浏览器伪装
     *
     * @param url 请求URL
     * @return JSON对象
     */
    public JSONObject httpGet(String url) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                // 使用HttpRequest模拟浏览器请求
                String result = HttpRequest.get(url)
                        .timeout(30000) // 30秒超时
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .header("Connection", "keep-alive")
                        .execute()
                        .body();

                if (null == result || result.isEmpty()) {
                    throw new BusinessException("返回参数为空");
                }

                if (!JSONUtil.isTypeJSON(result)) {
                    log.warn("返回数据不是有效的JSON格式，URL: {}, 前100字符: {}", url,
                            result.length() > 100 ? result.substring(0, 100) : result);
                    throw new BusinessException("返回参数异常");
                }

                JSONObject resultObj = JSONUtil.parseObj(result);
                if (resultObj.getInt("code") != 1) {
                    log.warn("API返回错误码: {}, URL: {}", resultObj.getInt("code"), url);
                    throw new BusinessException("请求错误，错误码: " + resultObj.getInt("code"));
                }

                // 请求成功，返回结果
                if (retryCount > 0) {
                    log.info("URL: {} 在第 {} 次重试后成功", url, retryCount);
                }
                return resultObj;

            } catch (cn.hutool.core.io.IORuntimeException e) {
                // Hutool的IO异常，包含连接失败、超时等网络问题
                lastException = e;
                retryCount++;
                if (retryCount < maxRetries) {
                    long delay = 1000L * retryCount; // 递增延迟：1s, 2s, 3s
                    String errorMsg = e.getMessage();
                    log.warn("网络连接失败，第 {} 次重试: {}, 延迟: {}ms, 错误: {}",
                            retryCount, url, delay, errorMsg);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("请求被中断");
                    }
                }
            } catch (BusinessException e) {
                // 业务异常不重试，直接抛出
                throw e;
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount < maxRetries) {
                    log.warn("请求异常，第 {} 次重试: {}, 错误: {}", retryCount, url, e.getMessage());
                    try {
                        Thread.sleep(1000L * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("请求被中断");
                    }
                }
            }
        }

        // 所有重试都失败
        log.error("URL: {} 在 {} 次重试后仍然失败", url, maxRetries);
        throw new BusinessException("请求失败，已重试" + maxRetries + "次: " + lastException.getMessage());
    }

    /**
     * 手动映射API返回的JSON数据到AnimeTable实体
     *
     * @param jsonObject API返回的单个动漫详情JSON对象
     * @return AnimeTable实体对象
     */
    public AnimeTable mapJsonToAnimeTable(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        AnimeTable animeTable = new AnimeTable();

        // 基础信息
        animeTable.setVodId(jsonObject.getInt("vod_id"));
        animeTable.setVodName(jsonObject.getStr("vod_name"));
        animeTable.setVodSub(jsonObject.getStr("vod_sub"));
        animeTable.setVodEn(jsonObject.getStr("vod_enname"));
        animeTable.setTypeId(jsonObject.getInt("type_id"));
        animeTable.setTypeName(jsonObject.getStr("type_name"));

        // 分类和标签
        animeTable.setVodClass(jsonObject.getStr("vod_class"));
        animeTable.setVodTag(jsonObject.getStr("vod_tag"));
        animeTable.setVodLetter(jsonObject.getStr("vod_letter"));
        animeTable.setVodColor(jsonObject.getStr("vod_color"));

        // 图片和媒体
        animeTable.setVodPic(jsonObject.getStr("vod_pic"));

        // 基本信息
        animeTable.setVodArea(jsonObject.getStr("vod_area"));
        animeTable.setVodLang(jsonObject.getStr("vod_lang"));
        animeTable.setVodYear(jsonObject.getStr("vod_year"));
        animeTable.setVodRemarks(jsonObject.getStr("vod_remarks"));

        // 人员信息
        animeTable.setVodActor(jsonObject.getStr("vod_actor"));
        animeTable.setVodDirector(jsonObject.getStr("vod_director"));

        // 状态信息
        animeTable.setVodIsend(jsonObject.getInt("vod_isend"));
        animeTable.setVodLock(jsonObject.getInt("vod_lock"));
        animeTable.setVodLevel(jsonObject.getInt("vod_level"));
        animeTable.setVodSerial(jsonObject.getStr("vod_serial"));

        // 统计信息
        animeTable.setVodHits(jsonObject.getInt("vod_hits"));
        animeTable.setVodHitsDay(jsonObject.getInt("vod_hits_day"));
        animeTable.setVodHitsWeek(jsonObject.getInt("vod_hits_week"));
        animeTable.setVodHitsMonth(jsonObject.getInt("vod_hits_month"));
        animeTable.setVodUp(jsonObject.getInt("vod_up"));
        animeTable.setVodDown(jsonObject.getInt("vod_down"));

        // 评分信息
        animeTable.setVodScore(jsonObject.getBigDecimal("vod_score"));
        animeTable.setVodScoreAll(jsonObject.getInt("vod_score_all"));
        animeTable.setVodScoreNum(jsonObject.getInt("vod_score_num"));

        // 时长和积分
        animeTable.setVodDuration(jsonObject.getStr("vod_duration"));
        animeTable.setVodPointsPlay(jsonObject.getInt("vod_points_play"));
        animeTable.setVodPointsDown(jsonObject.getInt("vod_points_down"));

        // 内容简介
        animeTable.setVodContent(jsonObject.getStr("vod_content"));

        // 播放信息
        animeTable.setVodPlayFrom(jsonObject.getStr("vod_play_from"));
        animeTable.setVodPlayNote(jsonObject.getStr("vod_play_note"));
        animeTable.setVodPlayServer(jsonObject.getStr("vod_play_server"));

        // 下载信息
        animeTable.setVodDownFrom(jsonObject.getStr("vod_down_from"));
        animeTable.setVodDownNote(jsonObject.getStr("vod_down_note"));
        animeTable.setVodDownServer(jsonObject.getStr("vod_down_server"));
        animeTable.setVodDownUrl(jsonObject.getStr("vod_down_url"));

        // 时间信息 - 将字符串时间转换为Date对象
        String vodTimeStr = jsonObject.getStr("vod_time");
        if (vodTimeStr != null && !vodTimeStr.isEmpty()) {
            try {
                Date vodTime = DateUtil.parse(vodTimeStr, "yyyy-MM-dd HH:mm:ss");
                animeTable.setVodTime(vodTime);
            } catch (Exception e) {
                log.warn("解析vod_time失败: {}", vodTimeStr, e);
            }
        }

        // 设置默认值
        if (animeTable.getVodStatus() == null) {
            animeTable.setVodStatus((byte) 1); // 默认状态为已发布
        }

        return animeTable;
    }

}
