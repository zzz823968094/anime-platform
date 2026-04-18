package com.anime.anime.controller;

import com.anime.anime.entity.AnimeTable;
import com.anime.anime.entity.SearchLog;
import com.anime.anime.entity.VisitLog;
import com.anime.anime.mapper.SearchLogMapper;
import com.anime.anime.mapper.VisitLogMapper;
import com.anime.anime.service.AnimeTableService;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
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
    private final SearchLogMapper searchLogMapper;
    private final VisitLogMapper visitLogMapper;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "24") int size,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<AnimeTable> result = animeService.listAnime(page, size, type, status, year, genre, sort, keyword);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable("id") Long id) {
        AnimeTable anime = animeService.getById(id);
        if (anime == null) return Result.fail(404, "番剧不存在");
        anime.setVodHits(anime.getVodHits() + 1);
        anime.setVodHitsDay(anime.getVodHitsDay() + 1);
        anime.setVodHitsWeek(anime.getVodHitsWeek() + 1);
        anime.setVodHitsMonth(anime.getVodHitsMonth() + 1);
        animeService.updateById(anime);
        return Result.ok(anime);
    }
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") Long id) {
        AnimeTable anime = animeService.getById(id);
        if (anime == null) return Result.fail(404, "番剧不存在");
        anime.setVodStatus(0);
        animeService.updateById(anime);
        return Result.ok("已下线");
    }

    @PutMapping("/{id}/online")
    public Result<?> online(
            @PathVariable("id") Long id) {
        AnimeTable anime = animeService.getById(id);
        if (anime == null) return Result.fail(404, "番剧不存在");
        anime.setVodStatus(1);
        animeService.updateById(anime);
        return Result.ok("已上线");
    }

    @GetMapping("/search")
    public Result<?> search(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "24") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {

        // 记录搜索日志（关键词不为空且长度合理）
        if (keyword != null && keyword.trim().length() >= 2) {
            try {
                SearchLog log = new SearchLog();
                log.setKeyword(keyword.trim());
                log.setUserId(userId);
                log.setIp(getClientIp(request));
                log.setCreatedAt(LocalDateTime.now());
                searchLogMapper.insert(log);
            } catch (Exception ignored) {}
        }

        return Result.ok(animeService.search(keyword, page, size));
    }

    /**
     * 搜索统计（管理端）
     * 返回热门关键词和最近7天搜索趋势
     */
    @GetMapping("/search/stats")
    public Result<?> searchStats(
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        Map<String, Object> data = new HashMap<>();
        data.put("hotKeywords", searchLogMapper.hotKeywords(limit));
        data.put("trend", searchLogMapper.searchTrend(days));
        data.put("totalSearches", searchLogMapper.selectCount(null));
        return Result.ok(data);
    }

    @GetMapping("/recommend/hot")
    public Result<?> hot(
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "limit", defaultValue = "0") int limit) {
        int count = limit > 0 ? limit : size;
        return Result.ok(animeService.getHotRecommend(count));
    }

    @GetMapping("/recommend/latest")
    public Result<?> latest(
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "limit", defaultValue = "0") int limit) {
        int count = limit > 0 ? limit : size;
        List<AnimeTable> list = animeService.lambdaQuery()
                .ne(AnimeTable::getVodStatus, 0)          // 过滤已下线
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
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();

        // 总番剧数（排除已下线）
        long totalAnime = animeService.lambdaQuery()
                .ne(AnimeTable::getVodStatus, 0)
                .count();
        data.put("totalAnime", totalAnime);

        // 总播放量（所有番剧 view_count 之和）
        List<AnimeTable> all = animeService.lambdaQuery()
                .select(AnimeTable::getVodHits)
                .list();
        long totalView = all.stream()
                .mapToLong(a -> a.getVodHits() == null ? 0 : a.getVodHits())
                .sum();
        data.put("totalView", totalView);

        // 各分类番剧数
        long jpCount = animeService.lambdaQuery().eq(AnimeTable::getTypeId, "25").ne(AnimeTable::getVodStatus, 0).count();
        long usCount = animeService.lambdaQuery().eq(AnimeTable::getTypeId, "26").ne(AnimeTable::getVodStatus, 0).count();
        long cnCount = animeService.lambdaQuery().eq(AnimeTable::getTypeId, "24").ne(AnimeTable::getVodStatus, 0).count();
        data.put("jpCount", jpCount);
        data.put("usCount", usCount);
        data.put("cnCount", cnCount);

        return Result.ok(data);
    }

    /**
     * 最近搜索记录（管理端）
     */
    @GetMapping("/search/recent")
    public Result<?> searchRecent(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<SearchLog> list = searchLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchLog>()
                        .orderByDesc(SearchLog::getCreatedAt)
                        .last("LIMIT " + limit)
        );
        return Result.ok(list);
    }

    /**
     * 访问上报（前端每次路由跳转时调用）
     * 同一 IP 同一页面同一天只记录一次，防刷
     */
    @PostMapping("/visit")
    public Result<?> reportVisit(@RequestBody Map<String, String> body,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                 HttpServletRequest request) {
        String page = body.get("page");
        if (page == null || page.isBlank()) return Result.ok("skip");

        String ip = getClientIp(request);

        // 同一 IP 同一页面今天已上报过则跳过
        if (visitLogMapper.checkDuplicate(ip, page) > 0) return Result.ok("dup");

        VisitLog log = new VisitLog();
        log.setPage(page);
        log.setIp(ip);
        log.setUserId(userId);
        log.setVisitDate(java.time.LocalDate.now());
        log.setCreatedAt(LocalDateTime.now());
        visitLogMapper.insert(log);
        return Result.ok("ok");
    }

    /**
     * 访问统计（管理端）
     * 返回今日UV、今日PV、最近7天趋势、热门页面
     */
    @GetMapping("/visit/stats")
    public Result<?> visitStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayUV",  visitLogMapper.todayUV());
        data.put("todayPV",  visitLogMapper.todayPV());
        data.put("trend",    visitLogMapper.dailyUV(7));
        data.put("hotPages", visitLogMapper.hotPages());
        return Result.ok(data);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return request.getRemoteAddr();
    }
}