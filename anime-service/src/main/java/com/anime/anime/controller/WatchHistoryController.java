package com.anime.anime.controller;

import com.anime.anime.entity.WatchHistory;
import com.anime.anime.entity.dto.WatchHistorySaveRequest;
import com.anime.anime.service.WatchHistoryService;
import com.anime.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 观看历史 Controller
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    /**
     * 保存或更新观看历史
     */
    @PostMapping("/save")
    public Result<?> saveHistory(@RequestBody WatchHistorySaveRequest request,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        request.setUserId(userId);
        try {
            watchHistoryService.saveOrUpdateHistory(request);

            return Result.ok("保存成功");
        } catch (Exception e) {
            return Result.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的观看历史列表
     */
    @GetMapping("/list")
    public Result<?> getHistory(@RequestParam(value = "limit", defaultValue = "100") int limit,
                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        List<WatchHistory> historyList = watchHistoryService.getUserHistory(userId, limit);
        return Result.ok(historyList);
    }

    /**
     * 删除某条观看历史
     */
    @DeleteMapping("/{animeId}")
    public Result<?> deleteHistory(@PathVariable("animeId") Long animeId,
                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        watchHistoryService.deleteHistory(userId, animeId);
        return Result.ok("删除成功");
    }

    /**
     * 清空所有观看历史
     */
    @DeleteMapping("/clear")
    public Result<?> clearHistory(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        watchHistoryService.clearUserHistory(userId);
        return Result.ok("清空成功");
    }

    /**
     * 统计观看历史数量
     */
    @GetMapping("/count")
    public Result<?> countHistory(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        int count = watchHistoryService.countUserHistory(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.ok(data);
    }
}
