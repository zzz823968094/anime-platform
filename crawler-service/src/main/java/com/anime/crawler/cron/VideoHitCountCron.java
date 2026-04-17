package com.anime.crawler.cron;

import com.anime.crawler.mapper.AnimeTableMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 视频播放量定时清理任务
 * - 每日凌晨1:00清除日播放量
 * - 每周一凌晨1:00清除周播放量
 * - 每月1日凌晨1:00清除月播放量
 */
@Slf4j
@Component
public class VideoHitCountCron {

    @Resource
    private AnimeTableMapper animeMapper;

    /**
     * 每日凌晨1:00清除日播放量
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearDailyViewCount() {
        log.info("开始执行每日播放量清零任务");
        try {
            int affectedRows = animeMapper.clearDailyViewCount();
            log.info("每日播放量清零任务执行完成，影响记录数: {}", affectedRows);
        } catch (Exception e) {
            log.error("每日播放量清零任务执行失败", e);
        }
    }

    /**
     * 每周一凌晨1:00清除周播放量
     * cron表达式: 秒 分 时 日 月 周 (MON表示周一)
     */
    @Scheduled(cron = "0 0 1 ? * MON")
    public void clearWeeklyViewCount() {
        log.info("开始执行每周播放量清零任务");
        try {
            int affectedRows = animeMapper.clearWeeklyViewCount();
            log.info("每周播放量清零任务执行完成，影响记录数: {}", affectedRows);
        } catch (Exception e) {
            log.error("每周播放量清零任务执行失败", e);
        }
    }

    /**
     * 每月1日凌晨1:00清除月播放量
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void clearMonthlyViewCount() {
        log.info("开始执行每月播放量清零任务");
        try {
            int affectedRows = animeMapper.clearMonthlyViewCount();
            log.info("每月播放量清零任务执行完成，影响记录数: {}", affectedRows);
        } catch (Exception e) {
            log.error("每月播放量清零任务执行失败", e);
        }
    }
}
