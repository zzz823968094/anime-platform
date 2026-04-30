package com.anime.crawler.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.anime.common.result.Result;
import com.anime.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawler")
public class CrawlerFailController {
    private final CrawlerService crawlerService;

    private final StringRedisTemplate redisTemplate;
    private final String CHINA_KEY = "crawler:failed:24";
    private final String JAPAN_KEY = "crawler:failed:25";
    private final String USA_KEY = "crawler:failed:26";

    @GetMapping("/fail")
    public Result<?> fail() {
        Map<String, Integer> map = new HashMap<>();
        Set<String> china = redisTemplate.opsForSet().members(CHINA_KEY);
        Set<String> japan = redisTemplate.opsForSet().members(JAPAN_KEY);
        Set<String> usa = redisTemplate.opsForSet().members(USA_KEY);
        map.put("china", china == null ? 0 : china.size());
        map.put("japan", japan == null ? 0 : japan.size());
        map.put("usa", usa == null ? 0 : usa.size());
        return Result.ok(map);
    }

    @PutMapping("/fail/restart/{type}")
    public Result<?> restart(@PathVariable("type") int type) {
        if (type != 25 && type != 26 && type != 24) {
            return Result.fail("不支持的分类 type，只允许 25/26/24");
        }
        Set<String> appIps = switch (type) {
            case 25 -> redisTemplate.opsForSet().members(JAPAN_KEY);
            case 26 -> redisTemplate.opsForSet().members(USA_KEY);
            case 24 -> redisTemplate.opsForSet().members(CHINA_KEY);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        switch (type) {
            case 25 -> redisTemplate.delete(JAPAN_KEY);
            case 26 -> redisTemplate.delete(USA_KEY);
            case 24 -> redisTemplate.delete(CHINA_KEY);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        ;
        if (appIps == null || appIps.isEmpty()) {
            return Result.fail("暂无数据");
        }
        crawlerService.crawlById(appIps, type);
        return Result.ok("已清空");
    }
}
