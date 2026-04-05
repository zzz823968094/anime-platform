package com.anime.crawler.controller;

import com.anime.common.result.Result;
import com.anime.crawler.service.BangumiService;
import com.anime.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final BangumiService bangumiService;

    /** 爬取所有分类最新第1页 */
    @PostMapping("/crawl-now")
    public Result<?> crawlNow() {
        new Thread(crawlerService::crawlLatest).start();
        return Result.ok("已启动：爬取所有分类最新数据");
    }

    /** 全量爬取所有分类 */
    @PostMapping("/full-sync")
    public Result<?> fullSync() {
        new Thread(crawlerService::crawlAll).start();
        return Result.ok("已启动：全量爬取所有分类（后台运行）");
    }

    /**
     * 增量爬取指定分类（遇到10个无变化自动停止）
     * type: 25=日本动漫, 26=欧美动漫, 24=中国动漫
     */
    @PostMapping("/incremental/{type}")
    public Result<?> incremental(@PathVariable(value = "type") int type) {
        if (type != 25 && type != 26 && type != 24) {
            return Result.fail("不支持的分类 type，只允许 25/26/24");
        }
        new Thread(() -> crawlerService.crawlIncremental(type)).start();
        String typeName = type == 25 ? "日本动漫" : type == 26 ? "欧美动漫" : "中国动漫";
        return Result.ok("已启动：" + typeName + " 增量爬取（遇到10个无变化自动停止）");
    }

    /**
     * 全量爬取指定分类
     * type: 25=日本动漫, 26=欧美动漫, 24=中国动漫
     * pages: 爬取页数，不传则使用默认值（25→44页, 26→9页, 24→47页）
     */
    @PostMapping("/sync/{type}")
    public Result<?> syncType(
            @PathVariable(value = "type") int type,
            @RequestParam(value = "pages", required = false) Integer pages) {

        if (type != 25 && type != 26 && type != 24) {
            return Result.fail("不支持的分类 type，只允许 25/26/24");
        }

        int maxPages = pages != null ? pages : (type == 25 ? 44 : type == 26 ? 9 : 47);
        int finalMaxPages = maxPages;

        new Thread(() -> crawlerService.crawlType(type, finalMaxPages)).start();

        String typeName = type == 25 ? "日本动漫" : type == 26 ? "欧美动漫" : "中国动漫";
        return Result.ok("已启动：" + typeName + " 全量爬取，共 " + maxPages + " 页（后台运行）");
    }

    /**
     * 重试失败记录
     * type: 25=日本动漫, 26=欧美动漫, 24=中国动漫
     */
    @PostMapping("/retry/{type}")
    public Result<?> retry(@PathVariable(value = "type") int type) {
        if (type != 25 && type != 26 && type != 24) {
            return Result.fail("不支持的分类 type，只允许 25/26/24");
        }
        long count = crawlerService.getFailedCount(type);
        if (count == 0) return Result.ok("没有失败记录，无需重试");
        new Thread(() -> crawlerService.retryFailed(type)).start();
        String typeName = type == 25 ? "日本动漫" : type == 26 ? "欧美动漫" : "中国动漫";
        return Result.ok("已启动：" + typeName + " 重试，共 " + count + " 条失败记录");
    }

    /** 查询失败记录数量 */
    @GetMapping("/failed/{type}")
    public Result<?> failedCount(@PathVariable(value = "type") int type) {
        long count = crawlerService.getFailedCount(type);
        return Result.ok(count);
    }

    /** 用 Bangumi API 补全封面和简介 */
    @PostMapping("/fill-covers")
    public Result<?> fillCovers() {
        new Thread(bangumiService::fillMissingCovers).start();
        return Result.ok("封面补全任务已启动，预计需要 30-60 分钟");
    }
}