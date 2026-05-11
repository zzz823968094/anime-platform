package com.anime.crawler.controller;

import com.anime.common.result.Result;
import com.anime.crawler.entity.CrawlerProgressInfo;
import com.anime.crawler.service.CrawlerProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 爬虫任务进度查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler/progress")
@RequiredArgsConstructor
public class CrawlerProgressController {
    
    private final CrawlerProgressService progressService;
    
    /**
     * 查询指定任务的进度
     */
    @GetMapping("/{taskKey}")
    public Result<?> getProgress(@PathVariable String taskKey) {
        CrawlerProgressInfo progress = progressService.getProgress(taskKey);
        if (progress == null) {
            return Result.fail(404, "任务不存在或已过期");
        }
        return Result.ok(progress);
    }
    
    /**
     * 获取最近的任务列表
     */
    @GetMapping("/recent")
    public Result<?> getRecentProgress(@RequestParam(defaultValue = "10") int limit) {
        // 限制最大查询数量
        if (limit > 50) {
            limit = 50;
        }
        return Result.ok(progressService.getRecentProgress(limit));
    }
    
    /**
     * 获取指定类型的运行中任务
     */
    @GetMapping("/running/{taskType}")
    public Result<?> getRunningProgress(@PathVariable Integer taskType) {
        CrawlerProgressInfo progress = progressService.getRunningProgress(taskType);
        if (progress == null) {
            return Result.ok(null);
        }
        return Result.ok(progress);
    }
    
    /**
     * 清理过期的进度记录
     */
    @PostMapping("/clean")
    public Result<?> cleanExpiredProgress() {
        progressService.cleanExpiredProgress();
        return Result.ok("清理完成");
    }
}
