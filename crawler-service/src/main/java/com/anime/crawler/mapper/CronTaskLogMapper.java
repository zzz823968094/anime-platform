package com.anime.crawler.mapper;

import com.anime.crawler.entity.CronTaskLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CronTaskLogMapper extends BaseMapper<CronTaskLog> {
    
    /**
     * 删除超过指定小时数的任务日志
     * @param hours 小时数
     * @return 删除的记录数
     */
    @Delete("DELETE FROM cron_task_log WHERE created_at < DATE_SUB(NOW(), INTERVAL #{hours} HOUR)")
    int deleteLogsOlderThanHours(int hours);
}
