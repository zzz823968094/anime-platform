package com.anime.danmaku.controller;

import com.anime.common.result.Result;
import com.anime.danmaku.entity.Danmaku;
import com.anime.danmaku.mapper.DanmakuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danmaku")
@RequiredArgsConstructor
public class DanmakuController {

    private final DanmakuMapper danmakuMapper;

    /**
     * 获取视频弹幕列表
     * GET /api/danmaku/{videoId}
     */
    @GetMapping("/{videoId}")
    public Result<?> list(
            @PathVariable("videoId") Long videoId,
            @RequestParam(value = "limit", defaultValue = "200") int limit) {
        List<Danmaku> list = danmakuMapper.selectList(
                new LambdaQueryWrapper<Danmaku>()
                        .eq(Danmaku::getVideoId, videoId)
                        .eq(Danmaku::getStatus, 0)
                        .orderByAsc(Danmaku::getTimePoint)
                        .last("LIMIT " + limit)
        );
        return Result.ok(list);
    }
}