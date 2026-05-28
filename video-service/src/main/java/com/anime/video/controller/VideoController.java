package com.anime.video.controller;

import com.anime.common.constant.CommonConstant;
import com.anime.common.result.Result;
import com.anime.video.entity.Video;
import com.anime.video.mapper.AnimeTableMapper;
import com.anime.video.mapper.VideoMapper;
import com.anime.video.service.VideoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 视频控制器
 * 遵循阿里巴巴开发规范，统一RESTful风格，Result返回
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;
    private final VideoMapper videoMapper;
    private final AnimeTableMapper animeTableMapper;

    /**
     * 获取动漫的视频列表
     *
     * @param animeId 动漫ID
     * @return 视频列表
     */
    @GetMapping("/anime/{animeId}")
    public Result<List<Video>> listByAnime(@PathVariable("animeId") Long animeId) {
        log.info("获取动漫视频列表，animeId: {}", animeId);

        List<Video> list = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getAnimeId, animeId)
                        .eq(Video::getStatus, CommonConstant.ANIME_STATUS_PUBLISHED)
                        .orderByAsc(Video::getEpisode)
        );

        log.info("获取动漫视频列表完成，animeId: {}, count: {}", animeId, list.size());
        return Result.ok(list);
    }

    /**
     * 获取视频详情并增加播放量
     *
     * @param videoId 视频ID
     * @return 视频详情
     */
    @GetMapping("/{videoId}")
    public Result<Video> getVideo(@PathVariable("videoId") Long videoId) {
        log.info("获取视频详情，videoId: {}", videoId);

        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            log.warn("视频不存在，videoId: {}", videoId);
            return Result.fail(CommonConstant.HTTP_STATUS_NOT_FOUND, "视频不存在");
        }

        // video 播放量 +1
        video.setViewCount(video.getViewCount() == null ? 0 : video.getViewCount() + 1);
        videoMapper.updateById(video);

        // anime 播放量原子自增（直接 SQL，避免并发问题）
        if (video.getAnimeId() != null) {
            animeTableMapper.incrementViewCount(video.getAnimeId());
        }

        log.debug("视频播放量更新成功，videoId: {}, viewCount: {}", videoId, video.getViewCount());
        return Result.ok(video);
    }
}