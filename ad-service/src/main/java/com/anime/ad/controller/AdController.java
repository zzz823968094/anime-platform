package com.anime.ad.controller;

import com.anime.ad.entity.Ad;
import com.anime.ad.entity.dto.AdDTO;
import com.anime.ad.service.AdService;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "广告管理")
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    @Operation(summary = "分页查询广告列表")
    @GetMapping("/page")
    public Result<Page<Ad>> pageAds(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) String status) {
        // 将字符串状态转换为整数
        Integer statusInt = null;
        if (status != null && !status.isEmpty()) {
            if ("enabled".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status)) {
                statusInt = 1;
            } else if ("disabled".equalsIgnoreCase(status) || "false".equalsIgnoreCase(status)) {
                statusInt = 0;
            } else {
                try {
                    statusInt = Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    statusInt = null;
                }
            }
        }
        Page<Ad> page = adService.pageAds(current, size, positionCode, statusInt);
        return Result.ok(page);
    }

    @Operation(summary = "根据广告位获取有效广告")
    @GetMapping("/position/{positionCode}")
    public Result<List<Ad>> getActiveAdsByPosition(@PathVariable String positionCode) {
        List<Ad> ads = adService.getActiveAdsByPosition(positionCode);
        return Result.ok(ads);
    }

    @Operation(summary = "根据广告位获取有效广告（带策略过滤）")
    @GetMapping("/position/{positionCode}/with-strategy")
    public Result<List<Ad>> getActiveAdsByPositionWithStrategy(
            @PathVariable String positionCode,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String region) {
        // 需要类型转换，因为 getActiveAdsByPositionWithStrategy 是内部方法
        List<Ad> ads = ((com.anime.ad.service.impl.AdServiceImpl) adService)
                .getActiveAdsByPositionWithStrategy(positionCode, deviceType, os, region);
        return Result.ok(ads);
    }

    @Operation(summary = "创建广告")
    @PostMapping
    public Result<Boolean> createAd(@RequestBody AdDTO adDTO) {
        boolean success = adService.createAd(adDTO);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("创建失败");
        }
    }

    @Operation(summary = "更新广告")
    @PutMapping("/{id}")
    public Result<Boolean> updateAd(@PathVariable Long id, @RequestBody AdDTO adDTO) {
        boolean success = adService.updateAd(id, adDTO);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "删除广告")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAd(@PathVariable Long id) {
        boolean success = adService.deleteAd(id);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "记录广告展示")
    @PostMapping("/{id}/impression")
    public Result<Void> recordImpression(@PathVariable Long id) {
        adService.recordImpression(id);
        return Result.ok(null);
    }

    @Operation(summary = "记录广告点击")
    @PostMapping("/{id}/click")
    public Result<Void> recordClick(@PathVariable Long id) {
        adService.recordClick(id);
        return Result.ok(null);
    }
}
