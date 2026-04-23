package com.anime.anime.controller;

import com.anime.anime.entity.AnimeTable;
import com.anime.anime.entity.Carousel;
import com.anime.anime.service.AnimeTableService;
import com.anime.anime.service.CarouselService;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/anime/carousel")
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService;
    private final AnimeTableService animeTableService;

    /**
     * 获取轮播图列表（前端调用）
     * 只返回启用状态的轮播图，按排序字段升序排列
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(value = "type", required = false) String type) {
        LambdaQueryWrapper<Carousel> wrapper = new LambdaQueryWrapper<>();
        // 只查询启用状态的轮播图
        wrapper.eq(Carousel::getStatus, "enabled");
        if (type != null && !type.isBlank()) {
            wrapper.eq(Carousel::getType, type);
        }
        // 按排序字段升序排列
        wrapper.orderByAsc(Carousel::getSortOrder);
        List<Carousel> carouselList = carouselService.list(wrapper);
        List<AnimeTable> list;
        if(null == carouselList || carouselList.isEmpty()){
            list = animeTableService.search(null, 1, 5).getRecords();
        }else{
            list = new ArrayList<>();
            carouselList.forEach(it->{
                AnimeTable animeTable = animeTableService.getById(it.getVideoId());
                list.add(animeTable);
            });
        }
        return Result.ok(list);
    }


}
