package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cron_task")
public class CronTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskName;
    
    private Integer taskType;
    
    private String cronExpression;
    
    private Integer pages;
    
    private Boolean enabled;
    
    private String status;
    
    private LocalDateTime lastExecuteTime;
    
    private LocalDateTime nextExecuteTime;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
