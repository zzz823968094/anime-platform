package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("cron_task_log")
public class CronTaskLog implements Serializable {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;
    
    private String taskName;
    
    private Integer taskType;
    
    private Date startTime;
    
    private Date endTime;
    
    private String status;
    
    private String message;

    private Date createdAt;
}
