package com.anime.crawler.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.anime.crawler.entity.CrawlerProgressInfo;
import com.anime.crawler.entity.dto.TodayUpdatedDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于Redis的爬虫任务进度跟踪服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerProgressService {

    // Redis key前缀
    private static final String PROGRESS_KEY_PREFIX = "crawler:progress:";
    private static final String RECENT_TASKS_KEY = "crawler:recent_tasks";
    private static final String TODAY_UPDATED = "anime:today:updated:";
    // 过期时间：1天
    private static final long EXPIRE_DAYS = 1;
    private final StringRedisTemplate redisTemplate;

    /**
     * 插入今日已更新的动漫
     */
    public void todayUpdated(List<TodayUpdatedDTO> todayUpdatedList){
        String key = TODAY_UPDATED + DateUtil.today();
        // 先判断Redis中是否已存在今日更新数据
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            // 已存在则读取原有数据，根据vodId合并去重后覆盖
            String existingJson = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(existingJson)) {
                List<TodayUpdatedDTO> existingList = JSONUtil.toList(existingJson, TodayUpdatedDTO.class);
                // 以vodId为key构建Map，新数据覆盖旧数据中相同vodId的记录
                Map<String, TodayUpdatedDTO> map = existingList.stream()
                        .collect(Collectors.toMap(TodayUpdatedDTO::getVodId, dto -> dto, (old, newVal) -> newVal, LinkedHashMap::new));
                for (TodayUpdatedDTO dto : todayUpdatedList) {
                    map.put(dto.getVodId(), dto);
                }
                todayUpdatedList = new ArrayList<>(map.values());
                log.info("Redis中已存在今日更新数据，根据vodId合并去重后覆盖写入");
            }
        }
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(todayUpdatedList), EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取今日已更新的动漫列表
     */
    public List<TodayUpdatedDTO> getTodayUpdated() {
        String key = TODAY_UPDATED + DateUtil.today();
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return new ArrayList<>();
        }
        return JSONUtil.toList(json, TodayUpdatedDTO.class);
    }

    /**
     * 创建新的进度跟踪记录
     */
    public String createProgress(Integer taskType, String taskName, Integer totalPages) {
        String taskKey = generateTaskKey(taskType);

        CrawlerProgressInfo progress = new CrawlerProgressInfo(taskKey, taskType, taskName);
        progress.setTotalPages(totalPages);

        // 存储到Redis
        String key = PROGRESS_KEY_PREFIX + taskKey;
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(progress), EXPIRE_DAYS, TimeUnit.DAYS);

        // 添加到最近任务列表（使用ZSet，按时间排序）
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(RECENT_TASKS_KEY, taskKey, score);

        // 限制最近任务列表大小，只保留最近100个
        Long size = redisTemplate.opsForZSet().size(RECENT_TASKS_KEY);
        if (size != null && size > 100) {
            redisTemplate.opsForZSet().removeRange(RECENT_TASKS_KEY, 0, size - 101);
        }

        log.info("创建爬取任务进度跟踪: taskKey={}, taskName={}", taskKey, taskName);

        return taskKey;
    }

    /**
     * 更新进度 - 页面处理完成
     */
    public void updatePageProgress(String taskKey, int currentPage, int totalPages,
                                   int itemsInPage, int successCount, int failCount) {
        CrawlerProgressInfo progress = getProgress(taskKey);
        if (progress == null) {
            log.warn("未找到任务进度记录: taskKey={}", taskKey);
            return;
        }

        progress.setCurrentPage(currentPage);
        progress.setProcessedPages(progress.getProcessedPages() + 1);
        progress.setTotalItems(progress.getTotalItems() + itemsInPage);
        progress.setProcessedItems(progress.getProcessedItems() + itemsInPage);
        progress.setSuccessCount(progress.getSuccessCount() + successCount);
        progress.setFailCount(progress.getFailCount() + failCount);
        progress.setTotalPages(totalPages);

        // 计算进度百分比
        if (totalPages > 0) {
            int percent = (int) ((currentPage * 100.0) / totalPages);
            progress.setProgressPercent(Math.min(percent, 100));
        } else if (progress.getTotalPages() != null && progress.getTotalPages() > 0) {
            int percent = (int) ((progress.getProcessedPages() * 100.0) / progress.getTotalPages());
            progress.setProgressPercent(Math.min(percent, 100));
        }

        progress.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 更新到Redis
        String key = PROGRESS_KEY_PREFIX + taskKey;
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(progress), EXPIRE_DAYS, TimeUnit.DAYS);

        log.debug("更新任务进度: taskKey={}, page={}/{}, progress={}%",
                taskKey, currentPage, totalPages, progress.getProgressPercent());
    }

    /**
     * 标记任务完成
     */
    public void markCompleted(String taskKey) {
        CrawlerProgressInfo progress = getProgress(taskKey);
        if (progress == null) {
            return;
        }

        progress.setStatus("COMPLETED");
        progress.setProgressPercent(100);
        progress.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        progress.setUpdateTime(progress.getEndTime());

        String key = PROGRESS_KEY_PREFIX + taskKey;
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(progress), EXPIRE_DAYS, TimeUnit.DAYS);

        log.info("任务已完成: taskKey={}, 总处理={}", taskKey, progress.getProcessedItems());
    }

    /**
     * 标记任务失败
     */
    public void markFailed(String taskKey, String errorMessage) {
        CrawlerProgressInfo progress = getProgress(taskKey);
        if (progress == null) {
            return;
        }

        progress.setStatus("FAILED");
        progress.setErrorMessage(errorMessage);
        progress.setEndTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        progress.setUpdateTime(progress.getEndTime());

        String key = PROGRESS_KEY_PREFIX + taskKey;
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(progress), EXPIRE_DAYS, TimeUnit.DAYS);

        log.error("任务失败: taskKey={}, error={}", taskKey, errorMessage);
    }

    /**
     * 获取任务进度
     */
    public CrawlerProgressInfo getProgress(String taskKey) {
        if (StrUtil.isBlank(taskKey)) {
            return null;
        }

        String key = PROGRESS_KEY_PREFIX + taskKey;
        String json = redisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(json)) {
            return null;
        }

        return JSONUtil.toBean(json, CrawlerProgressInfo.class);
    }

    /**
     * 获取最近的任务进度列表
     */
    public List<CrawlerProgressInfo> getRecentProgress(int limit) {
        // 从ZSet中获取最近的taskKey
        Set<String> taskKeys = redisTemplate.opsForZSet()
                .reverseRange(RECENT_TASKS_KEY, 0, limit - 1);

        if (taskKeys == null || taskKeys.isEmpty()) {
            return new ArrayList<>();
        }

        List<CrawlerProgressInfo> result = new ArrayList<>();
        for (String taskKey : taskKeys) {
            CrawlerProgressInfo progress = getProgress(taskKey);
            if (progress != null) {
                result.add(progress);
            }
        }

        return result;
    }

    /**
     * 根据任务类型获取运行中的任务
     */
    public CrawlerProgressInfo getRunningProgress(Integer taskType) {
        // 获取所有最近任务
        Set<String> taskKeys = redisTemplate.opsForZSet()
                .reverseRange(RECENT_TASKS_KEY, 0, -1);

        if (taskKeys == null || taskKeys.isEmpty()) {
            return null;
        }

        for (String taskKey : taskKeys) {
            CrawlerProgressInfo progress = getProgress(taskKey);
            if (progress != null &&
                    "RUNNING".equals(progress.getStatus()) &&
                    taskType.equals(progress.getTaskType())) {
                return progress;
            }
        }

        return null;
    }

    /**
     * 清理过期的进度记录（Redis会自动过期，此方法用于手动清理ZSet中的无效引用）
     */
    public void cleanExpiredProgress() {
        Set<String> taskKeys = redisTemplate.opsForZSet().range(RECENT_TASKS_KEY, 0, -1);

        if (taskKeys == null || taskKeys.isEmpty()) {
            return;
        }

        int cleaned = 0;
        for (String taskKey : taskKeys) {
            String key = PROGRESS_KEY_PREFIX + taskKey;
            Boolean exists = redisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(exists)) {
                redisTemplate.opsForZSet().remove(RECENT_TASKS_KEY, taskKey);
                cleaned++;
            }
        }

        if (cleaned > 0) {
            log.info("清理ZSet中过期的任务引用: {} 条", cleaned);
        }
    }

    /**
     * 生成任务唯一标识
     */
    private String generateTaskKey(Integer taskType) {
        return "crawler_" + taskType + "_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 获取类型名称（公开方法，供Controller使用）
     */
    public String getTypeName(Integer type) {
        if (type == null) {
            return "未知类型";
        }
        switch (type) {
            case 66:
                return "中国动漫";
            case 67:
                return "日本动漫";
            case 68:
                return "欧美动漫";
            case 69:
                return "港台动漫";
            case 70:
                return "海外动漫";
            default:
                return "类型" + type;
        }
    }
}
