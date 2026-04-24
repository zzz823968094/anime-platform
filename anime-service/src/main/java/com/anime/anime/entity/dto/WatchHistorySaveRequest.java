package com.anime.anime.entity.dto;

import lombok.Data;

/**
 * 观看历史保存请求 DTO
 */
@Data
public class WatchHistorySaveRequest {
    private Long userId;

    /**
     * 动漫ID
     */
    private Long animeId;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 集数
     */
    private Integer episode;

    /**
     * 观看进度（百分比）
     */
    private Double progress;

    /**
     * 观看时长（秒）
     */
    private Integer watchDuration;

    /**
     * 视频名称
     */
    private String vodName;

    /**
     * 视频封面
     */
    private String vodPic;

    /**
     * 视频总集数
     */
    private Integer vodTotal;
}
