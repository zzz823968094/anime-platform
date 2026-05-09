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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * API数据同步服务
 * 处理特定格式的API返回数据，同步到anime_table和video表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiDataSyncService {
    
    private static final int BATCH_SIZE = 100;
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D+");
    
    private final AnimeTableMapper animeTableMapper;
    private final VideoMapper videoMapper;
    
    /**
     * 从API响应中解析并同步数据
     * @param apiResponse API返回的JSON字符串
     */
    public void syncFromApiResponse(String apiResponse) {
        if (StrUtil.isEmpty(apiResponse)) {
            log.warn("API响应为空");
            return;
        }
        
        try {
            JSONObject responseObj = JSONUtil.parseObj(apiResponse);
            
            // 检查返回码
            Integer code = responseObj.getInt("code");
            if (code == null || code != 1) {
                log.warn("API返回错误码: {}", code);
                return;
            }
            
            JSONArray list = responseObj.getJSONArray("list");
            if (list == null || list.isEmpty()) {
                log.info("API返回数据列表为空");
                return;
            }
            
            log.info("开始同步 {} 条数据", list.size());
            
            List<AnimeTable> newAnimeList = new ArrayList<>();
            List<AnimeTable> updateAnimeList = new ArrayList<>();
            List<Video> allVideos = new ArrayList<>();
            
            for (int i = 0; i < list.size(); i++) {
                try {
                    JSONObject item = list.getJSONObject(i);
                    processSingleItem(item, newAnimeList, updateAnimeList, allVideos);
                    
                    // 达到批量大小时执行插入
                    if (newAnimeList.size() + updateAnimeList.size() >= BATCH_SIZE) {
                        insertBatch(newAnimeList, updateAnimeList, allVideos);
                        newAnimeList.clear();
                        updateAnimeList.clear();
                        allVideos.clear();
                    }
                } catch (Exception e) {
                    log.error("处理第 {} 条数据时发生异常", i, e);
                }
            }
            
            // 插入剩余数据
            if (!newAnimeList.isEmpty() || !updateAnimeList.isEmpty() || !allVideos.isEmpty()) {
                insertBatch(newAnimeList, updateAnimeList, allVideos);
            }
            
            log.info("数据同步完成");
            
        } catch (Exception e) {
            log.error("同步API数据时发生异常", e);
            throw new RuntimeException("同步API数据失败", e);
        }
    }
    
    /**
     * 处理单条数据项
     */
    private void processSingleItem(JSONObject item, 
                                   List<AnimeTable> newAnimeList,
                                   List<AnimeTable> updateAnimeList,
                                   List<Video> allVideos) {
        // 检查是否有完整数据（判断是否为详情接口返回）
        boolean isDetailData = item.containsKey("vod_content") || item.containsKey("vod_play_url");
        
        // 解析AnimeTable数据
        AnimeTable animeTable = JSONUtil.toBean(item, AnimeTable.class);
        
        if (animeTable.getVodId() == null) {
            log.warn("vod_id为空，跳过该条数据");
            return;
        }
        
        // 检查是否已存在（根据vod_id）
        AnimeTable existingAnime = animeTableMapper.selectByVodId(animeTable.getVodId());
        
        if (existingAnime != null) {
            // 已存在，需要合并数据
            if (isDetailData) {
                // 详情数据：完整更新
                animeTable.setId(existingAnime.getId());
                updateAnimeList.add(animeTable);
                log.debug("更新动漫(详情): vodId={}, name={}", animeTable.getVodId(), animeTable.getVodName());
            } else {
                // 列表数据：只更新存在的字段，避免用null覆盖
                mergeListData(existingAnime, item);
                updateAnimeList.add(existingAnime);
                log.debug("更新动漫(列表): vodId={}, name={}", existingAnime.getVodId(), existingAnime.getVodName());
            }
        } else {
            // 不存在，新增
            long id = IdUtil.nextId();
            animeTable.setId(id);
            newAnimeList.add(animeTable);
            log.debug("新增动漫: vodId={}, name={}", animeTable.getVodId(), animeTable.getVodName());
        }
        
        // 处理视频列表
        String vodPlayUrl = item.getStr("vod_play_url");
        if (StrUtil.isNotEmpty(vodPlayUrl)) {
            List<Video> videos = parseVideoList(animeTable.getId(), vodPlayUrl);
            allVideos.addAll(videos);
            log.debug("解析到 {} 个视频", videos.size());
        }
    }
    
    /**
     * 合并列表数据到已有记录（只更新非null字段）
     */
    private void mergeListData(AnimeTable existing, JSONObject item) {
        // 只更新列表接口返回的字段
        if (item.containsKey("vod_name")) {
            existing.setVodName(item.getStr("vod_name"));
        }
        if (item.containsKey("type_id")) {
            existing.setTypeId(item.getInt("type_id"));
        }
        if (item.containsKey("type_name")) {
            existing.setTypeName(item.getStr("type_name"));
        }
        if (item.containsKey("vod_en")) {
            existing.setVodEn(item.getStr("vod_en"));
        }
        if (item.containsKey("vod_time")) {
            existing.setVodTime(item.getDate("vod_time"));
        }
        if (item.containsKey("vod_remarks")) {
            existing.setVodRemarks(item.getStr("vod_remarks"));
        }
        if (item.containsKey("vod_play_from")) {
            existing.setVodPlayFrom(item.getStr("vod_play_from"));
        }
        // 更新时间戳
        existing.setUpdatedAt(new java.util.Date());
    }
    
    /**
     * 解析视频列表
     * @param animeId 动漫ID
     * @param vodPlayUrl 播放URL字符串，格式：第01集$URL#第02集$URL
     * @return 视频列表
     */
    private List<Video> parseVideoList(Long animeId, String vodPlayUrl) {
        List<Video> videos = new ArrayList<>();
        
        if (StrUtil.isEmpty(vodPlayUrl)) {
            return videos;
        }
        
        // 按#分割各个剧集
        String[] episodes = vodPlayUrl.split("#");
        
        for (String episode : episodes) {
            try {
                String trimmedEpisode = episode.trim();
                if (StrUtil.isEmpty(trimmedEpisode)) {
                    continue;
                }
                
                // 按$分割标题和URL
                String[] parts = trimmedEpisode.split("\\$", 2);
                if (parts.length < 2) {
                    log.warn("视频格式错误: {}", episode);
                    continue;
                }
                
                String title = parts[0].trim();
                String url = parts[1].trim();
                
                if (StrUtil.isEmpty(title) || StrUtil.isEmpty(url)) {
                    continue;
                }
                
                Video video = new Video();
                video.setId(IdUtil.nextId());
                video.setAnimeId(animeId);
                video.setTitle(title);
                video.setM3u8Url(url);
                video.setStatus(1);
                video.setViewCount(0);
                
                // 提取集数
                String number = NON_DIGIT_PATTERN.matcher(title).replaceAll("");
                if (StrUtil.isNotEmpty(number)) {
                    try {
                        video.setEpisode(Integer.parseInt(number));
                    } catch (NumberFormatException e) {
                        log.warn("无法解析集数: {}", title);
                        video.setEpisode(0);
                    }
                } else {
                    video.setEpisode(0);
                }
                
                videos.add(video);
                
            } catch (Exception e) {
                log.error("解析视频失败: {}", episode, e);
            }
        }
        
        return videos;
    }
    
    /**
     * 批量插入/更新数据
     */
    private void insertBatch(List<AnimeTable> newAnimeList, 
                            List<AnimeTable> updateAnimeList, 
                            List<Video> videoList) {
        try {
            // 分批插入新动漫数据
            if (!newAnimeList.isEmpty()) {
                List<List<AnimeTable>> partitions = CollUtil.split(newAnimeList, BATCH_SIZE);
                for (List<AnimeTable> partition : partitions) {
                    animeTableMapper.insertBatchIgnore(partition);
                }
                log.info("批量新增动漫: {} 条", newAnimeList.size());
            }
            
            // 分批更新已有动漫数据
            if (!updateAnimeList.isEmpty()) {
                List<List<AnimeTable>> partitions = CollUtil.split(updateAnimeList, BATCH_SIZE);
                for (List<AnimeTable> partition : partitions) {
                    animeTableMapper.updateBatchById(partition);
                }
                log.info("批量更新动漫: {} 条", updateAnimeList.size());
            }
            
            // 分批插入视频数据（有则更新无则新增）
            if (!videoList.isEmpty()) {
                List<List<Video>> partitions = CollUtil.split(videoList, BATCH_SIZE);
                for (List<Video> partition : partitions) {
                    videoMapper.insertBatchOrUpdate(partition);
                }
                log.info("批量插入或更新视频: {} 条", videoList.size());
            }
        } catch (Exception e) {
            log.error("批量插入数据时发生异常", e);
            throw new RuntimeException("批量插入数据失败", e);
        }
    }
}
