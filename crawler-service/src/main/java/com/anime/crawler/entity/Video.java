package com.anime.crawler.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("video")
public class Video implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonProperty("anime_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long animeId;

    private Integer episode;

    private String title;

    @JsonProperty("m3u8_url")
    private String m3u8Url;

    private Integer duration;

    @JsonProperty("view_count")
    private Integer viewCount;

    private Integer status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}