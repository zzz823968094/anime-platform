package com.anime.admin.controller;

import com.anime.admin.entity.Carousel;
import com.anime.admin.service.CarouselService;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/carousels")
public class CarouselController {

    private final CarouselService carouselService;
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        LambdaQueryWrapper<Carousel> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) wrapper.eq(Carousel::getType, type);
        if (status != null && !status.isBlank()) wrapper.eq(Carousel::getStatus, status);
        wrapper.orderByAsc(Carousel::getSortOrder).orderByDesc(Carousel::getCreateTime);
        Page<Carousel> page = new Page<>(pageNum, pageSize);
        carouselService.page(page, wrapper);
        return Result.ok(page);
    }
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Carousel carousel = carouselService.getById(id);
        if (carousel == null) return Result.fail(404, "轮播图不存在");
        return Result.ok(carousel);
    }

    @PostMapping
    public Result<?> create(@RequestBody Carousel carousel) {
        return Result.ok(carouselService.create(carousel));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Carousel carousel) {
        return Result.ok(carouselService.update(id, carousel));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        carouselService.delete(id);
        return Result.ok();
    }

    /**
     * 启用轮播图
     * 如果已有5个启用的轮播图，会自动禁用 updateTime 最早的一个
     */
    @PutMapping("/{id}/enable")
    public Result<?> enable(@PathVariable Long id) {
        carouselService.enable(id);
        return Result.ok("启用成功");
    }

    /**
     * 禁用轮播图
     */
    @PutMapping("/{id}/disable")
    public Result<?> disable(@PathVariable Long id) {
        carouselService.disable(id);
        return Result.ok("禁用成功");
    }
}
