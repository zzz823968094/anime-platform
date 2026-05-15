package com.anime.ad.controller;

import com.anime.ad.entity.AdStrategy;
import com.anime.ad.service.AdStrategyService;
import com.anime.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "广告投放策略管理")
@RestController
@RequestMapping("/api/ad-strategy")
@RequiredArgsConstructor
public class AdStrategyController {

    private final AdStrategyService adStrategyService;

    @Operation(summary = "查询广告的所有策略")
    @GetMapping("/ad/{adId}")
    public Result getStrategiesByAdId(@PathVariable Long adId) {
        List<AdStrategy> strategies = adStrategyService.getStrategiesByAdId(adId);
        return Result.ok(strategies);
    }

    @Operation(summary = "创建投放策略")
    @PostMapping
    public Result createStrategy(@RequestBody AdStrategy strategy) {
        boolean success = adStrategyService.save(strategy);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("创建失败");
        }
    }

    @Operation(summary = "更新投放策略")
    @PutMapping("/{id}")
    public Result updateStrategy(@PathVariable Long id, @RequestBody AdStrategy strategy) {
        strategy.setId(id);
        boolean success = adStrategyService.updateById(strategy);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "删除投放策略")
    @DeleteMapping("/{id}")
    public Result deleteStrategy(@PathVariable Long id) {
        boolean success = adStrategyService.removeById(id);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "批量删除广告的策略")
    @DeleteMapping("/ad/{adId}")
    public Result deleteStrategiesByAdId(@PathVariable Long adId) {
        boolean success = adStrategyService.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdStrategy>()
                        .eq(AdStrategy::getAdId, adId)
        );
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("删除失败");
        }
    }
}
