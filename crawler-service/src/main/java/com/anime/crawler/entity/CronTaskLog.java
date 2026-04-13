package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cron_task_log")
public class CronTaskLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    
    private String taskName;
    
    private Integer taskType;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long duration;
    
    private String status;
    
    private String message;
    
    private Integer pagesCrawled;
    
    private LocalDateTime createdAt;
}
