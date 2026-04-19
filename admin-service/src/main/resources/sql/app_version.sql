CREATE TABLE IF NOT EXISTS `app_version` (
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `version_code`  INT          NOT NULL COMMENT '版本号（整数，用于比较大小）',
    `version_name`  VARCHAR(32)  NOT NULL COMMENT '版本名称，如 1.0.0',
    `platform`      VARCHAR(16)  NOT NULL DEFAULT 'android' COMMENT '平台：android / ios / all',
    `download_url`  VARCHAR(512) NOT NULL COMMENT '下载地址',
    `release_notes` TEXT         COMMENT '更新说明',
    `force_update`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否强制更新',
    `file_size`     BIGINT       COMMENT '文件大小（字节）',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'active' COMMENT '状态：active / inactive',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_version_code` (`platform`, `version_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App 版本管理';
