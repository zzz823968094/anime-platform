package com.anime.danmaku.controller;

import com.anime.common.constant.CommonConstant;
import com.anime.common.result.Result;
import com.anime.danmaku.entity.Danmaku;
import com.anime.danmaku.mapper.DanmakuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 弹幕控制器
 * 遵循阿里巴巴开发规范，统一RESTful风格，Result返回
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@RestController
@RequestMapping("/api/danmaku")
@RequiredArgsConstructor
public class DanmakuController {

    private final DanmakuMapper danmakuMapper;

    /**
     * 获取视频弹幕列表
     *
     * @param videoId 视频ID
     * @param limit 限制数量，默认200
     * @return 弹幕列表
     */
    @GetMapping("/{videoId}")
    public Result<List<Danmaku>> list(
            @PathVariable("videoId") Long videoId,
            @RequestParam(value = "limit", defaultValue = "200") int limit) {
        log.info("获取视频弹幕列表，videoId: {}, limit: {}", videoId, limit);
        
        List<Danmaku> list = danmakuMapper.selectList(
                new LambdaQueryWrapper<Danmaku>()
                        .eq(Danmaku::getVideoId, videoId)
                        .eq(Danmaku::getStatus, CommonConstant.ANIME_STATUS_OFFLINE)
                        .orderByAsc(Danmaku::getTimePoint)
                        .last("LIMIT " + limit)
        );
        
        log.info("获取视频弹幕列表完成，videoId: {}, count: {}", videoId, list.size());
        return Result.ok(list);
    }
}