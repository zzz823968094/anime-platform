-- 访问用户详情表
CREATE TABLE IF NOT EXISTS `access_user_detail`
(
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`           BIGINT               DEFAULT NULL COMMENT '用户ID，未登录用户为NULL',
    `ip`                VARCHAR(45) NOT NULL COMMENT '客户端IP地址',
    `sign`              VARCHAR(10) NOT NULL DEFAULT 'app' COMMENT '访问标识：app/web',
    `visit_date`        INT         NOT NULL COMMENT '访问日期 YYYYMMDD 例如：20240101',
    `first_visit_time`  DATETIME    NOT NULL COMMENT '首次访问时间',
    `last_visit_time`   DATETIME    NOT NULL COMMENT '最后访问时间',
    `visit_count`       INT         NOT NULL DEFAULT 1 COMMENT '访问次数',
    `location_country`  VARCHAR(50)          DEFAULT NULL COMMENT '国家',
    `location_province` VARCHAR(50)          DEFAULT NULL COMMENT '省份',
    `location_city`     VARCHAR(50)          DEFAULT NULL COMMENT '城市',
    `location_isp`      VARCHAR(100)         DEFAULT NULL COMMENT '运营商',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_ip_date_sign` (`user_id`, `ip`, `visit_date`, `sign`) COMMENT '用户IP日期标识唯一索引',
    KEY `idx_visit_date` (`visit_date`) COMMENT '访问日期索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_ip` (`ip`) COMMENT 'IP地址索引',
    KEY `idx_location` (`location_country`, `location_province`, `location_city`) COMMENT '地理位置索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='访问用户详情表';
