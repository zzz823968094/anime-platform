package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 观看历史实体类
 */
@Data
@TableName("watch_history")
public class WatchHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 番剧ID
     */
    private Long animeId;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 当前集数
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
     * 番剧名称（冗余字段，便于展示）
     */
    private String vodName;

    /**
     * 番剧封面（冗余字段，便于展示）
     */
    private String vodPic;

    /**
     * 总集数（冗余字段，便于展示）
     */
    private Integer vodTotal;

    /**
     * 最后观看时间
     */
    private LocalDateTime lastWatchTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
