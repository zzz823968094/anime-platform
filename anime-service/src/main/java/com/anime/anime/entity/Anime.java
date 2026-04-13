package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("anime")
public class Anime {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String titleOriginal;
    private String coverImage;
    private String synopsis;
    private String type;
    private String genre;
    private Integer status;
    private Integer episodeCount;
    private Integer currentEpisode;
    private Integer year;
    private String season;
    private Double rating;
    private Integer viewCount;
    private Integer favoriteCount;
    private String sourceId;
    private String bangumiId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}