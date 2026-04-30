package com.anime.anime.controller;

import com.anime.anime.service.AccessDataService;
import com.anime.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/access/data")
@RequiredArgsConstructor
public class AccessDataController {
    private final AccessDataService accessDataService;
    @RequestMapping("init")
    public Result<?> initRedisData(){
        accessDataService.aggregateHourlyAccessData();
        return Result.ok();
    }
}
