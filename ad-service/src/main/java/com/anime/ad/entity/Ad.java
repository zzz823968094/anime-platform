package com.anime.ad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ad")
public class Ad implements Serializable {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String positionCode;

    private String title;

    private String subtitle;

    private String imageUrl;

    private String videoUrl;

    private String htmlContent;

    private String linkType;

    private String linkValue;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String targetType;

    private Integer priority;

    private Long clickCount;

    private Long impressionCount;

    private Integer status;

    private Integer sortOrder;

    private String extraData;

    private Long createBy;

    private Long updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
