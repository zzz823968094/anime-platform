package com.anime.crawler.cron;

import com.anime.crawler.mapper.CronTaskLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CronTaskLog定时清理任务
 * 每天凌晨2:00清理超过48小时的任务日志数据
 */
@Slf4j
@Component
public class CronTaskLogCleanCron {

    @Resource
    private CronTaskLogMapper cronTaskLogMapper;

    /**
     * 每天凌晨2:00清理超过48小时的任务日志
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldTaskLogs() {
        log.info("开始执行CronTaskLog清理任务，清理超过48小时的数据");
        try {
            int deletedCount = cronTaskLogMapper.deleteLogsOlderThanHours(48);
            log.info("CronTaskLog清理任务执行完成，删除记录数: {}", deletedCount);
        } catch (Exception e) {
            log.error("CronTaskLog清理任务执行失败", e);
        }
    }
}
