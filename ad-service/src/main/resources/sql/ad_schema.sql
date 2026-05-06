-- 广告位表（支持多种广告位置）
CREATE TABLE IF NOT EXISTS `ad_position` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `position_code` VARCHAR(50) NOT NULL COMMENT '广告位编码（如：SPLASH-开机广告，BANNER-横幅广告，INTERSTITIAL-插屏广告等）',
    `position_name` VARCHAR(100) NOT NULL COMMENT '广告位名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '广告位描述',
    `display_type` VARCHAR(20) NOT NULL DEFAULT 'IMAGE' COMMENT '展示类型：IMAGE-图片，VIDEO-视频，HTML-富媒体',
    `width` INT DEFAULT NULL COMMENT '建议宽度（像素）',
    `height` INT DEFAULT NULL COMMENT '建议高度（像素）',
    `max_count` INT NOT NULL DEFAULT 1 COMMENT '最大同时展示广告数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_position_code` (`position_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告位配置表';

-- 广告表
CREATE TABLE IF NOT EXISTS `ad` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `position_code` VARCHAR(50) NOT NULL COMMENT '广告位编码',
    `title` VARCHAR(200) NOT NULL COMMENT '广告标题',
    `subtitle` VARCHAR(500) DEFAULT NULL COMMENT '广告副标题',
    `image_url` VARCHAR(500) NOT NULL COMMENT '广告图片URL',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '广告视频URL（当display_type为VIDEO时使用）',
    `html_content` TEXT DEFAULT NULL COMMENT 'HTML内容（当display_type为HTML时使用）',
    `link_type` VARCHAR(20) NOT NULL DEFAULT 'URL' COMMENT '跳转类型：URL-外部链接，ANIME-动漫详情，SEARCH-搜索结果，NONE-无跳转',
    `link_value` VARCHAR(500) DEFAULT NULL COMMENT '跳转值（URL地址/动漫ID/搜索关键词）',
    `start_time` DATETIME NOT NULL COMMENT '广告开始时间',
    `end_time` DATETIME NOT NULL COMMENT '广告结束时间',
    `target_type` VARCHAR(20) DEFAULT 'ALL' COMMENT '目标用户类型：ALL-全部用户，NEW-新用户，VIP-VIP用户',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级，数值越大优先级越高',
    `click_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点击次数',
    `impression_count` BIGINT NOT NULL DEFAULT 0 COMMENT '展示次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '同一广告位内的排序',
    `extra_data` JSON DEFAULT NULL COMMENT '扩展数据（JSON格式，用于存储业务特定字段）',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_position_code` (`position_code`),
    KEY `idx_status` (`status`),
    KEY `idx_time_range` (`start_time`, `end_time`),
    KEY `idx_priority` (`priority` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告表';

-- 广告投放策略表（可选，用于更精细的投放控制）
CREATE TABLE IF NOT EXISTS `ad_strategy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ad_id` BIGINT NOT NULL COMMENT '广告ID',
    `strategy_type` VARCHAR(20) NOT NULL COMMENT '策略类型：DEVICE-设备类型，OS-操作系统，REGION-地区，TIME_SEGMENT-时间段',
    `strategy_value` VARCHAR(200) NOT NULL COMMENT '策略值（JSON格式存储具体条件）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ad_id` (`ad_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告投放策略表';

-- 插入默认广告位数据
INSERT INTO `ad_position` (`position_code`, `position_name`, `description`, `display_type`, `width`, `height`, `max_count`, `status`, `sort_order`) VALUES
('SPLASH', '开机广告', '应用启动时显示的全屏广告', 'IMAGE', 1080, 1920, 1, 1, 1),
('BANNER_HOME', '首页横幅', '首页顶部横幅广告', 'IMAGE', 750, 300, 3, 1, 2),
('BANNER_DETAIL', '详情页横幅', '动漫详情页横幅广告', 'IMAGE', 750, 200, 2, 1, 3),
('INTERSTITIAL', '插屏广告', '页面切换时的插屏广告', 'IMAGE', 1080, 1920, 1, 1, 4);
