package com.anime.crawler.controller;

import com.anime.common.result.Result;
import com.anime.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class testController {
    private final CrawlerService crawlerService_;
    @GetMapping("test")
    public Result<?> test(){
        crawlerService_.CrawlerByType(26);
        return Result.ok();
    }
}
