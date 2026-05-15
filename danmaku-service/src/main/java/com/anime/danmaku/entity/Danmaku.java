package com.anime.danmaku.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("danmaku")
public class Danmaku implements Serializable {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String content;
    private Float timePoint;
    private String color;
    private Integer dmType;
    private Integer status;
    private LocalDateTime createdAt;
}