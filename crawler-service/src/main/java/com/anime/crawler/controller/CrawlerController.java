package com.anime.crawler.controller;

import com.anime.common.result.Result;
import com.anime.crawler.service.BangumiService;
import com.anime.crawler.service.CrawlerService;
import com.anime.crawler.service.CrawlerService2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final BangumiService bangumiService;

    /** 爬取所有分类最新第1页 */
    @PostMapping("/crawl-now")
    public Result<?> crawlNow(Integer type,Integer hour) {
        // 直接调用服务层方法,由CrawlerService内部的线程池管理并发
        crawlerService.crawlNow(type,hour);
        return Result.ok("已启动：爬取所有分类最新数据");
    }

    /**
     * 增量爬取指定分类（遇到10个无变化自动停止）
     * type: 25=日本动漫, 26=欧美动漫, 24=中国动漫
     */
    @PostMapping("/incremental/{type}")
    public Result<?> incremental(@PathVariable int type) {
        if (type != 25 && type != 26 && type != 24) {
            return Result.fail("不支持的分类 type，只允许 25/26/24");
        }
        // 直接调用服务层方法,由CrawlerService内部的线程池管理并发,避免线程数量失控
        crawlerService.CrawlerByType(type);
        String typeName = type == 25 ? "日本动漫" : type == 26 ? "欧美动漫" : "中国动漫";
        return Result.ok("已启动：" + typeName + " 增量爬取（遇到10个无变化自动停止）");
    }
}