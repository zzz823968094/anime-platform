-- 定时任务管理表
-- 创建时间: 2026-04-13
CREATE TABLE `admin_user` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
    `account` varchar(64) NOT NULL COMMENT '账号',
    `password` varchar(64) NOT NULL COMMENT '密码',
    `name` varchar(64) NOT NULL COMMENT '姓名',
    `phone` varchar(11) NOT NULL COMMENT '手机号',
    `status` enum('NORMAL','DISABLE') NOT NULL COMMENT '状态\r\nNORMAL:正常\r\nDISABLE:禁用',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
PRIMARY KEY (`id`)                           
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
-- 定时任务配置表
CREATE TABLE IF NOT EXISTS `cron_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `task_type` INT NOT NULL COMMENT '任务类型(25=日本动漫, 26=欧美动漫, 24=中国动漫)',
  `cron_expression` VARCHAR(50) NOT NULL COMMENT 'Cron表达式',
  `pages` INT NOT NULL DEFAULT 1 COMMENT '爬取页数',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用(1=启用, 0=禁用)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态(PENDING=待执行, RUNNING=运行中, COMPLETED=已完成, FAILED=执行失败, CANCELLED=已取消)',
  `last_execute_time` DATETIME DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` DATETIME DEFAULT NULL COMMENT '下次执行时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_task_type` (`task_type`),
  INDEX `idx_enabled` (`enabled`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务配置表';

-- 定时任务执行记录表
CREATE TABLE IF NOT EXISTS `cron_task_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `task_type` INT NOT NULL COMMENT '任务类型',
  `start_time` DATETIME NOT NULL COMMENT '开始执行时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束执行时间',
  `duration` BIGINT DEFAULT NULL COMMENT '执行时长(毫秒)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '执行状态(RUNNING=执行中, SUCCESS=执行成功, FAILED=执行失败, CANCELLED=已取消)',
  `message` TEXT COMMENT '执行信息或错误信息',
  `pages_crawled` INT DEFAULT 0 COMMENT '实际爬取页数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行记录表';

-- 插入默认的定时任务
INSERT INTO `cron_task` (`task_name`, `task_type`, `cron_expression`, `pages`, `enabled`, `status`) VALUES
('日本动漫每3小时同步', 25, '0 0 */3 * * ?', 44, 1, 'PENDING'),
('欧美动漫每6小时同步', 26, '0 0 */6 * * ?', 9, 1, 'PENDING'),
('中国动漫每6小时同步', 24, '0 0 */6 * * ?', 47, 1, 'PENDING');
