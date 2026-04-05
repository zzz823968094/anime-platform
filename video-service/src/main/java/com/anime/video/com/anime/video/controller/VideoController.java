package com.anime.video.com.anime.video.controller;

import com.anime.common.result.Result;
import com.anime.video.com.anime.video.entity.Video;
import com.anime.video.com.anime.video.mapper.AnimeMapper;
import com.anime.video.com.anime.video.mapper.VideoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoMapper videoMapper;
    private final AnimeMapper animeMapper;

    @GetMapping("/anime/{animeId}")
    public Result<?> listByAnime(@PathVariable("animeId") Long animeId) {
        List<Video> list = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getAnimeId, animeId)
                        .eq(Video::getStatus, 1)
                        .orderByAsc(Video::getEpisode)
        );
        return Result.ok(list);
    }

    @GetMapping("/{videoId}")
    public Result<?> getVideo(@PathVariable("videoId") Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) return Result.fail(404, "视频不存在");

        // video 播放量 +1
        video.setViewCount(video.getViewCount() + 1);
        videoMapper.updateById(video);

        // anime 播放量原子自增（直接 SQL，避免并发问题）
        if (video.getAnimeId() != null) {
            animeMapper.incrementViewCount(video.getAnimeId());
        }

        return Result.ok(video);
    }
}