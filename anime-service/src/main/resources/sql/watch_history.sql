-- 观看历史表
CREATE TABLE IF NOT EXISTS `watch_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `anime_id` BIGINT NOT NULL COMMENT '番剧ID',
    `video_id` BIGINT DEFAULT NULL COMMENT '视频ID',
    `episode` INT DEFAULT NULL COMMENT '当前集数',
    `progress` DOUBLE DEFAULT 0.0 COMMENT '观看进度（百分比）',
    `watch_duration` INT DEFAULT 0 COMMENT '观看时长（秒）',
    `vod_name` VARCHAR(255) DEFAULT NULL COMMENT '番剧名称（冗余字段）',
    `vod_pic` VARCHAR(500) DEFAULT NULL COMMENT '番剧封面（冗余字段）',
    `vod_total` INT DEFAULT NULL COMMENT '总集数（冗余字段）',
    `last_watch_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后观看时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_anime` (`user_id`, `anime_id`) COMMENT '用户+番剧唯一索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_last_watch_time` (`last_watch_time`) COMMENT '最后观看时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观看历史表';
