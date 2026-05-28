-- 设备统计表
CREATE TABLE IF NOT EXISTS `device_statistics`
(
    `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `date`         INT          NOT NULL COMMENT '日期 YYYYMMDD 例如：20240101',
    `device_model` VARCHAR(100) NOT NULL COMMENT '设备型号',
    `os`           VARCHAR(50)  NOT NULL COMMENT '操作系统',
    `user_count`   INT      DEFAULT 0 COMMENT '访问人数（去重IP数）',
    `ip`           JSON COMMENT 'IP集合（JSON格式存储）',
    `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_device_os` (`date`, `device_model`, `os`) COMMENT '日期+设备型号+系统唯一索引',
    KEY `idx_date` (`date`) COMMENT '日期索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='设备统计表';
