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
@TableName("cron_task")
public class CronTask implements Serializable {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String taskName;

    private Integer taskType;

    private Integer hour;

    private String cronExpression;

    private Boolean enabled;

    private String status;

    private Date lastExecuteTime;

    private Date nextExecuteTime;

    private Date createdAt;

    private Date updatedAt;
}
