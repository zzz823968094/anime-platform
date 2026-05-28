package com.anime.crawler.controller;

import com.anime.common.result.Result;
import com.anime.crawler.entity.AnimeTable;
import com.anime.crawler.entity.dto.CrawlerRequestDTO;
import com.anime.crawler.mapper.AnimeTableMapper;
import com.anime.crawler.service.CrawlerProgressService;
import com.anime.crawler.service.Https1080Zyk3CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫管理控制器
 * 遵循阿里巴巴开发规范，统一RESTful风格，Result返回
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final AnimeTableMapper animeTableMapper;
    private final Https1080Zyk3CrawlerService https1080Zyk3CrawlerService;
    private final CrawlerProgressService progressService;

    /**
     * 爬取单个动漫详情
     *
     * @param id 动漫ID
     * @return 操作结果
     */
    @PutMapping("/crawl")
    public Result crawl(@RequestParam("id") Long id) {
        log.info("开始爬取单个动漫，id: {}", id);

        if (null == id) {
            log.warn("爬取参数错误，id为空");
            return Result.fail("参数错误");
        }

        AnimeTable animeTable = animeTableMapper.selectById(id);
        if (animeTable == null) {
            log.warn("动漫不存在，id: {}", id);
            return Result.fail("动漫不存在");
        }

        https1080Zyk3CrawlerService.clawerOneAsync(animeTable.getVodId().toString());
        log.info("已启动异步爬取任务，vodId: {}", animeTable.getVodId());
        return Result.ok("已启动异步爬取任务");
    }

    /**
     * 爬取所有分类最新第1页
     *
     * @param request 爬取请求参数
     * @return 任务信息
     */
    @PostMapping("/crawl-now")
    public Result crawlNow(@RequestBody(required = false) CrawlerRequestDTO request) {
        log.info("开始爬取所有分类最新数据");

        // 直接调用服务层方法,由CrawlerService内部的线程池管理并发
        Integer type = request != null ? request.getType() : null;
        Integer hour = request != null ? request.getHour() : null;

        // 创建进度跟踪并获取taskKey
        String typeName = progressService.getTypeName(type);
        String taskKey = progressService.createProgress(type,
                typeName + "-" + hour + "小时更新", null);

        // 异步执行爬取任务（传入taskKey）
        https1080Zyk3CrawlerService.clawerByHourAsync(type, hour, 1, taskKey);

        Map<String, Object> result = new HashMap<>();
        result.put("taskKey", taskKey);
        result.put("message", "已启动：爬取所有分类最新数据");
        result.put("progressUrl", "/api/crawler/progress/" + taskKey);

        log.info("爬取任务已启动，taskKey: {}", taskKey);
        return Result.ok(result);
    }

    /**
     * 增量爬取指定分类（遇到10个无变化自动停止）
     * type: 25=日本动漫, 26=欧美动漫, 24=中国动漫
     *
     * @param type 分类类型
     * @return 任务信息
     */
    @PostMapping("/incremental/{type}")
    public Result incremental(@PathVariable("type") int type) {
        log.info("开始增量爬取，type: {}", type);

        if (type != 66 && type != 67 && type != 68 && type != 69 && type != 70) {
            log.warn("不支持的分类 type: {}", type);
            return Result.fail("不支持的分类 type，只允许 66/67/68/69/70");
        }

        // 创建进度跟踪并获取taskKey
        String typeName = progressService.getTypeName(type);
        String taskKey = progressService.createProgress(type,
                typeName + "-全量爬取", null);

        // 异步执行爬取任务（传入taskKey）
        https1080Zyk3CrawlerService.clawerByTypeAsync(type, 1, taskKey);

        Map<String, Object> result = new HashMap<>();
        result.put("taskKey", taskKey);
        result.put("message", "已启动：" + typeName + " 增量爬取（遇到10个无变化自动停止）");
        result.put("progressUrl", "/api/crawler/progress/" + taskKey);

        log.info("增量爬取任务已启动，taskKey: {}, typeName: {}", taskKey, typeName);
        return Result.ok(result);
    }

}