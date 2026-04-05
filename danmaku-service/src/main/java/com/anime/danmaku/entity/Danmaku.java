package com.anime.danmaku.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("danmaku")
public class Danmaku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long videoId;
    private Long userId;
    private String content;
    private Float timePoint;
    private String color;
    private Integer dmType;
    private Integer status;
    private LocalDateTime createdAt;
}