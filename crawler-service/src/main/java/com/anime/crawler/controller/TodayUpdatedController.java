package com.anime.crawler.controller;

import com.anime.common.result.Result;
import com.anime.crawler.entity.dto.TodayUpdatedDTO;
import com.anime.crawler.service.CrawlerProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 今日更新数据
 * 遵循阿里巴巴开发规范，统一RESTful风格，Result返回
 *
 * @author anime-platform
 * @date 2026-08-10
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class TodayUpdatedController {
    private final CrawlerProgressService crawlerProgressService;

    /**
     * 获取今日更新数据
     *
     * @return 今日更新数据
     */
    @GetMapping("/getTodayUpdated")
    public Result<List<TodayUpdatedDTO>> todayUpdated() {
        return Result.ok(crawlerProgressService.getTodayUpdated());
    }

}
