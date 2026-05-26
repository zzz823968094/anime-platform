package com.anime.anime.controller;

import com.anime.anime.entity.AnimeTable;
import com.anime.anime.entity.SearchLog;
import com.anime.anime.entity.dto.AccessStatsDTO;
import com.anime.anime.entity.dto.DeviceStatsDTO;
import com.anime.anime.entity.vo.SearchListVO;
import com.anime.anime.mapper.AnimeTableMapper;
import com.anime.anime.mapper.SearchLogMapper;
import com.anime.anime.service.AccessDataService;
import com.anime.anime.service.AccessUserDetailService;
import com.anime.anime.service.AnimeTableService;
import com.anime.anime.service.DeviceStatisticsService;
import com.anime.common.constant.CommonConstant;
import com.anime.common.result.PageResult;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeTableController {

    private final AnimeTableService animeService;
    private final AnimeTableMapper animeTableMapper;
    private final SearchLogMapper searchLogMapper;
    private final AccessDataService accessDataService;
    private final AccessUserDetailService accessUserDetailService;
    private final DeviceStatisticsService deviceStatisticsService;

    @GetMapping("/list")
    public Result list(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sign", required = false) String sign,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Device-Model", required = false) String deviceModel,
            @RequestHeader(value = "X-OS", required = false) String os) {
        Page<AnimeTable> result = animeService.listAnime(page, size, type, status, year, genre, sort, keyword);
        String ip = getClientIp(request);
        accessDataService.recordAccess(ip, sign);
        accessUserDetailService.recordUserAccess(userId, ip, sign);
        // 记录设备信息
        if (deviceModel != null && os != null && !deviceModel.isEmpty() && !os.isEmpty()) {
            deviceStatisticsService.recordDevice(ip, deviceModel, os);
        }
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable("id") Long id) {
        AnimeTable anime = animeService.getById(id);
        if (anime == null) return Result.fail(CommonConstant.HTTP_STATUS_NOT_FOUND, "番剧不存在");
        anime.setVodHits(anime.getVodHits() + 1);
        anime.setVodHitsDay(anime.getVodHitsDay() + 1);
        anime.setVodHitsWeek(anime.getVodHitsWeek() + 1);
        anime.setVodHitsMonth(anime.getVodHitsMonth() + 1);
        animeService.updateById(anime);
        return Result.ok(anime);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        AnimeTable anime = animeService.getById(id);
        if (anime == null) return Result.fail(CommonConstant.HTTP_STATUS_NOT_FOUND, "番剧不存在");
        anime.setVodStatus(CommonConstant.ANIME_STATUS_OFFLINE);
        animeService.updateById(anime);
        return Result.ok("已下线");
    }

    @PutMapping("/{id}/online")
    public Result online(
            @PathVariable("id") Long id) {
        AnimeTable anime = animeService.getById(id);
        if (anime == null) return Result.fail(CommonConstant.HTTP_STATUS_NOT_FOUND, "番剧不存在");
        anime.setVodStatus(CommonConstant.ANIME_STATUS_PUBLISHED);
        animeService.updateById(anime);
        return Result.ok("已上线");
    }

    @GetMapping("/search")
    public Result search(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "24") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {

        // 记录搜索日志（关键词不为空且长度合理）
        if (keyword != null && keyword.trim().length() >= CommonConstant.SEARCH_KEYWORD_MIN_LENGTH) {
            try {
                SearchLog log = new SearchLog();
                log.setKeyword(keyword.trim());
                log.setUserId(userId);
                log.setIp(getClientIp(request));
                log.setCreatedAt(LocalDateTime.now());
                searchLogMapper.insert(log);
            } catch (Exception ignored) {
            }
        }

        return Result.ok(animeService.search(keyword, page, size));
    }

    /**
     * 搜索统计（管理端）
     * 返回热门关键词和最近7天搜索趋势
     */
    @GetMapping("/search/stats")
    public Result searchStats(
            @RequestParam(value = "days", defaultValue = "1") int days,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        PageResult<SearchListVO> pageResult = new PageResult<>();
        pageResult.setRecords(searchLogMapper.keywordList(days, (pageNum - 1) * pageSize, pageSize));
        pageResult.setTotal(searchLogMapper.keywordListCount(days, (pageNum - 1) * pageSize, pageSize));
        pageResult.setCurrent((long) pageNum);
        pageResult.setSize((long) pageSize);
        pageResult.setPages(pageResult.getTotal() % pageResult.getSize() == 0 ? pageResult.getTotal() / pageResult.getSize() : pageResult.getTotal() / pageResult.getSize() + 1);
        return Result.ok(pageResult);
    }

    @GetMapping("/recommend/hot")
    public Result hot(
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "limit", defaultValue = "0") int limit) {
        int count = limit > 0 ? limit : size;
        return Result.ok(animeService.getHotRecommend(count));
    }

    @GetMapping("/recommend/latest")
    public Result latest(
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "limit", defaultValue = "0") int limit) {
        int count = limit > 0 ? limit : size;
        List<AnimeTable> list = animeService.lambdaQuery()
                .orderByDesc(AnimeTable::getUpdateAt)
                .last("limit " + count)
                .list();
        return Result.ok(list);
    }

    /**
     * 全站统计数据接口
     * 返回：总番剧数、总播放量、各分类番剧数
     */
    @GetMapping("/stats")
    public Result stats() {
        Map<String, Object> data = new HashMap<>();

        // 总番剧数（排除已下线）
        long totalAnime = animeService.lambdaQuery()
                .count();
        data.put("totalAnime", totalAnime);

        // 总播放量（使用MySQL SUM函数统计，避免加载所有数据到内存）
        Long totalView = animeTableMapper.sumTotalViewCount();
        Long todayView = animeTableMapper.sumTodayViewCount();
        data.put("totalView", totalView);
        data.put("todayView", todayView);

        // 各分类番剧数
        long jpCount = animeService.lambdaQuery().eq(AnimeTable::getTypeId, CommonConstant.ANIME_TYPE_JAPAN).count();
        long usCount = animeService.lambdaQuery().eq(AnimeTable::getTypeId, CommonConstant.ANIME_TYPE_US).count();
        long cnCount = animeService.lambdaQuery().eq(AnimeTable::getTypeId, CommonConstant.ANIME_TYPE_CHINA).count();
        data.put("jpCount", jpCount);
        data.put("usCount", usCount);
        data.put("cnCount", cnCount);
        return Result.ok(data);
    }

    /**
     * 最近搜索记录（管理端）
     */
    @GetMapping("/search/recent")
    public Result searchRecent(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<SearchLog> list = searchLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchLog>()
                        .orderByDesc(SearchLog::getCreatedAt)
                        .last("LIMIT " + limit)
        );
        return Result.ok(list);
    }

    /**
     * 访问统计（管理端）
     * 返回今日UV、最近N天趋势、总访问量
     */
    @GetMapping("/access/stats")
    public Result accessStats(
            @RequestParam(value = "days", defaultValue = "7") int days) {
        AccessStatsDTO stats = new AccessStatsDTO();
        Integer todayAppRealTimeUserCount = accessDataService.getTodayAppRealTimeUserCount();
        // 今日实时访问人数（从Redis读取）
        stats.setTodayAppUV(todayAppRealTimeUserCount);

        Integer todayWebRealTimeUserCount = accessDataService.getTodayWebRealTimeUserCount();
        // 今日实时访问人数（从Redis读取）
        stats.setTodayWebUV(todayWebRealTimeUserCount);
        // 最近N天访问趋势
        stats.setTrend(accessDataService.getAccessTrend(days));
        // 总访问人数
        Long totalUserCount = accessDataService.getTotalUserCount();
        stats.setTotalUserCount(totalUserCount + todayAppRealTimeUserCount + todayWebRealTimeUserCount);

        return Result.ok(stats);
    }

    /**
     * 设备统计（管理端）
     * 返回指定日期的设备统计数据
     */
    @GetMapping("/device/stats")
    public Result deviceStats(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        DeviceStatsDTO stats = new DeviceStatsDTO();

        if (date != null && !date.isEmpty()) {
            // 查询指定日期的设备统计
            List<DeviceStatsDTO.DeviceDetailDTO> deviceList = deviceStatisticsService.getDeviceByDate(date).stream()
                    .map(device -> {
                        DeviceStatsDTO.DeviceDetailDTO dto = new DeviceStatsDTO.DeviceDetailDTO();
                        dto.setDate(device.getDate());
                        dto.setDeviceModel(device.getDeviceModel());
                        dto.setOs(device.getOs());
                        dto.setUserCount(device.getUserCount());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            stats.setDeviceList(deviceList);
        } else {
            // 查询最近N天的设备统计趋势
            List<DeviceStatsDTO.DeviceDetailDTO> trend = deviceStatisticsService.getDeviceTrend(days).stream()
                    .map(device -> {
                        DeviceStatsDTO.DeviceDetailDTO dto = new DeviceStatsDTO.DeviceDetailDTO();
                        dto.setDate(device.getDate());
                        dto.setDeviceModel(device.getDeviceModel());
                        dto.setOs(device.getOs());
                        dto.setUserCount(device.getUserCount());
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            stats.setTrend(trend);
        }

        // 总访问人数
        Long totalUserCount = deviceStatisticsService.getTotalUserCount();
        stats.setTotalUserCount(totalUserCount);

        return Result.ok(stats);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(CommonConstant.HEADER_X_FORWARDED_FOR);
        if (ip != null && !ip.isBlank()) return ip.split(CommonConstant.IP_ADDRESS_SEPARATOR)[0].trim();
        ip = request.getHeader(CommonConstant.HEADER_X_REAL_IP);
        if (ip != null && !ip.isBlank()) return ip;
        return request.getRemoteAddr();
    }
}