-- 轮播图表
CREATE TABLE IF NOT EXISTS `carousel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
    `video_id` VARCHAR(100) DEFAULT NULL COMMENT '视频ID（可为空）',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：video-视频，ad-广告',
    `status` VARCHAR(20) NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled-启用，disabled-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_sort_order` (`sort_order`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图管理表';
