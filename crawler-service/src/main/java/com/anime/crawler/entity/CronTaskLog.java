package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("cron_task_log")
public class CronTaskLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long taskId;
    
    private String taskName;
    
    private Integer taskType;
    
    private Date startTime;
    
    private Date endTime;
    
    private String status;
    
    private String message;

    private Date createdAt;
}
