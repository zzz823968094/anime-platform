package com.anime.ad.controller;

import com.anime.ad.entity.AdPosition;
import com.anime.ad.entity.dto.AdPositionDTO;
import com.anime.ad.service.AdPositionService;
import com.anime.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "广告位管理")
@RestController
@RequestMapping("/api/ad-position")
@RequiredArgsConstructor
public class AdPositionController {

    private final AdPositionService adPositionService;

    @Operation(summary = "获取所有启用的广告位")
    @GetMapping("/active")
    public Result getAllActivePositions() {
        List<AdPosition> positions = adPositionService.getAllActivePositions();
        return Result.ok(positions);
    }

    @Operation(summary = "获取所有广告位")
    @GetMapping
    public Result getAllPositions() {
        List<AdPosition> positions = adPositionService.list();
        return Result.ok(positions);
    }

    @Operation(summary = "创建广告位")
    @PostMapping
    public Result createPosition(@RequestBody AdPositionDTO positionDTO) {
        boolean success = adPositionService.createPosition(positionDTO);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("创建失败");
        }
    }

    @Operation(summary = "更新广告位")
    @PutMapping("/{id}")
    public Result updatePosition(@PathVariable Long id, @RequestBody AdPositionDTO positionDTO) {
        boolean success = adPositionService.updatePosition(id, positionDTO);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "删除广告位")
    @DeleteMapping("/{id}")
    public Result deletePosition(@PathVariable Long id) {
        boolean success = adPositionService.deletePosition(id);
        if (success) {
            return Result.ok(true);
        } else {
            return Result.fail("删除失败");
        }
    }
}
