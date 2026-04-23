package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("carousel")
public class Carousel {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer sortOrder;

    private String videoId;

    private String type;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
