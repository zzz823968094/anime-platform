package com.anime.ad.controller;

import com.anime.ad.entity.Ad;
import com.anime.ad.entity.dto.AdDTO;
import com.anime.ad.service.AdService;
import com.anime.common.constant.CommonConstant;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告管理控制器
 * 遵循阿里巴巴开发规范，统一RESTful风格，参数校验，Result返回
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Tag(name = "广告管理")
@RestController
@RequestMapping("/api/ad")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    /**
     * 分页查询广告列表
     *
     * @param current      当前页码
     * @param size         每页大小
     * @param positionCode 广告位编码
     * @param status       状态
     * @return 分页结果
     */
    @Operation(summary = "分页查询广告列表")
    @GetMapping("/page")
    public Result<Page<Ad>> pageAds(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) String status) {
        log.info("分页查询广告列表，current: {}, size: {}", current, size);

        // 将字符串状态转换为整数
        Integer statusInt = null;
        if (status != null && !status.isEmpty()) {
            if (CommonConstant.AD_STATUS_STRING_ENABLED.equalsIgnoreCase(status) || CommonConstant.AD_STATUS_STRING_TRUE.equalsIgnoreCase(status)) {
                statusInt = CommonConstant.AD_STATUS_ENABLED;
            } else if (CommonConstant.AD_STATUS_STRING_DISABLED.equalsIgnoreCase(status) || CommonConstant.AD_STATUS_STRING_FALSE.equalsIgnoreCase(status)) {
                statusInt = CommonConstant.AD_STATUS_DISABLED;
            } else {
                try {
                    statusInt = Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    log.warn("状态参数格式错误，status: {}", status);
                    statusInt = null;
                }
            }
        }

        Page<Ad> page = adService.pageAds(current, size, positionCode, statusInt);
        log.info("分页查询广告列表完成，total: {}", page.getTotal());
        return Result.ok(page);
    }

    /**
     * 根据广告位获取有效广告
     *
     * @param positionCode 广告位编码
     * @return 广告列表
     */
    @Operation(summary = "根据广告位获取有效广告")
    @GetMapping("/position/{positionCode}")
    public Result<List<Ad>> getActiveAdsByPosition(@PathVariable String positionCode) {
        log.info("根据广告位获取有效广告，positionCode: {}", positionCode);
        List<Ad> ads = adService.getActiveAdsByPosition(positionCode);
        log.info("获取到{}个有效广告，positionCode: {}", ads.size(), positionCode);
        return Result.ok(ads);
    }

    @Operation(summary = "根据广告位获取有效广告（带策略过滤）")
    @GetMapping("/position/{positionCode}/with-strategy")
    public Result getActiveAdsByPositionWithStrategy(
            @PathVariable String positionCode,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String region) {
        // 需要类型转换，因为 getActiveAdsByPositionWithStrategy 是内部方法
        List<Ad> ads = ((com.anime.ad.service.impl.AdServiceImpl) adService)
                .getActiveAdsByPositionWithStrategy(positionCode, deviceType, os, region);
        return Result.ok(ads);
    }

    /**
     * 创建广告
     *
     * @param adDTO 广告DTO
     * @return 创建结果
     */
    @Operation(summary = "创建广告")
    @PostMapping
    public Result<Boolean> createAd(@Validated @RequestBody AdDTO adDTO) {
        log.info("开始创建广告，title: {}", adDTO.getTitle());
        boolean success = adService.createAd(adDTO);
        log.info("广告创建{}", success ? "成功" : "失败");
        return Result.ok(success);
    }

    /**
     * 更新广告
     *
     * @param id    广告ID
     * @param adDTO 广告DTO
     * @return 更新结果
     */
    @Operation(summary = "更新广告")
    @PutMapping("/{id}")
    public Result<Boolean> updateAd(@PathVariable Long id, @Validated @RequestBody AdDTO adDTO) {
        log.info("开始更新广告，id: {}", id);
        boolean success = adService.updateAd(id, adDTO);
        log.info("广告更新{}，id: {}", success ? "成功" : "失败", id);
        return Result.ok(success);
    }

    /**
     * 删除广告
     *
     * @param id 广告ID
     * @return 删除结果
     */
    @Operation(summary = "删除广告")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteAd(@PathVariable Long id) {
        log.info("开始删除广告，id: {}", id);
        boolean success = adService.deleteAd(id);
        log.info("广告删除{}，id: {}", success ? "成功" : "失败", id);
        return Result.ok(success);
    }

    /**
     * 记录广告展示
     *
     * @param id 广告ID
     * @return 操作结果
     */
    @Operation(summary = "记录广告展示")
    @PostMapping("/{id}/impression")
    public Result<Void> recordImpression(@PathVariable Long id) {
        log.debug("记录广告展示，id: {}", id);
        adService.recordImpression(id);
        return Result.ok(null);
    }

    /**
     * 记录广告点击
     *
     * @param id 广告ID
     * @return 操作结果
     */
    @Operation(summary = "记录广告点击")
    @PostMapping("/{id}/click")
    public Result<Void> recordClick(@PathVariable Long id) {
        log.debug("记录广告点击，id: {}", id);
        adService.recordClick(id);
        return Result.ok(null);
    }
}
