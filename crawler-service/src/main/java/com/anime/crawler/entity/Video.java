package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video")
public class Video {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long animeId;
    private Integer episode;
    private String title;
    private String m3u8Url;
    private Integer duration;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}