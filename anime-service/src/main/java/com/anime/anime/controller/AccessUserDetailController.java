package com.anime.anime.controller;

import com.anime.anime.entity.dto.LocationStatDTO;
import com.anime.anime.service.AccessUserDetailService;
import com.anime.common.constant.CommonConstant;
import com.anime.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 访问用户详情控制器
 */
@RestController
@RequestMapping("/api/access/user-detail")
@RequiredArgsConstructor
public class AccessUserDetailController {

    private final AccessUserDetailService accessUserDetailService;

    /**
     * 计算留存率
     *
     * @param baseDate 基准日期 YYYYMMDD，不传则使用昨天
     * @return 留存率数据
     */
    @GetMapping("/retention")
    public Result getRetentionRate(
            @RequestParam(required = false) Integer baseDate) {

        if (baseDate == null) {
            // 默认使用昨天的日期
            baseDate = Integer.parseInt(
                    java.time.LocalDate.now().minusDays(1)
                            .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
            );
        }

        // 留存天数：1、7、15、30、180天
        Integer[] days = {1, CommonConstant.DEFAULT_SEARCH_TREND_DAYS, 15, 30, 180};
        Map<Integer, Double> retentionMap = accessUserDetailService.calculateRetentionRate(baseDate, days);

        return Result.ok(retentionMap);
    }

    /**
     * 获取地理位置统计数据
     *
     * @param days 统计最近N天的数据，默认30天
     * @return 地理位置统计数据
     */
    @GetMapping("/location-stats")
    public Result getLocationStats(
            @RequestParam(defaultValue = "30") Integer days) {

        List<LocationStatDTO> stats = accessUserDetailService.getLocationStatistics(days);
        return Result.ok(stats);
    }

    /**
     * 手动触发IP地理位置更新
     *
     * @return 操作结果
     */
    @PostMapping("/update-location")
    public Result updateLocation() {
        accessUserDetailService.batchUpdateLocationInfo();
        return Result.ok("地理位置更新任务已启动");
    }
}
