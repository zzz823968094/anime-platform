/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : animedb

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 18/04/2026 22:51:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `status` enum('NORMAL','DISABLE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态\nNORMAL:正常\nDISABLE:禁用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for anime_table
-- ----------------------------
DROP TABLE IF EXISTS `anime_table`;
CREATE TABLE `anime_table`  (
  `id` bigint NOT NULL COMMENT '自增主键ID',
  `vod_id` int NULL DEFAULT NULL COMMENT '视频ID',
  `type_id` int NULL DEFAULT NULL COMMENT '主分类ID',
  `type_id_1` int NULL DEFAULT NULL COMMENT '一级分类ID',
  `group_id` int NULL DEFAULT 0 COMMENT '分组ID，0表示未分组',
  `vod_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频名称/标题',
  `vod_sub` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题/别名，包含多语言标题',
  `vod_en` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '英文名或拼音名',
  `vod_status` tinyint NULL DEFAULT 1 COMMENT '状态：1-已发布/正常',
  `vod_letter` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '首字母索引，用于字母检索',
  `vod_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色标记，用于特殊样式',
  `vod_tag` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签关键词，如：动作,动画,奇幻',
  `vod_class` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类标签，如：动作,动画,奇幻,中国动漫',
  `vod_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片地址',
  `vod_pic_thumb` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缩略图地址',
  `vod_pic_slide` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '幻灯片/滚动大图地址',
  `vod_pic_screenshot` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '截图地址',
  `vod_actor` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '演员/配音演员列表，多个用逗号分隔',
  `vod_director` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '导演',
  `vod_writer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编剧',
  `vod_behind` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '幕后人员/其他制作人员',
  `vod_blurb` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介/短描述',
  `vod_remarks` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息，如：第19集',
  `vod_pubdate` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上映/发布日期，格式：YYYY-MM-DD(地区)',
  `vod_total` smallint NULL DEFAULT NULL COMMENT '总集数',
  `vod_serial` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连载状态：0-连载中或未定义',
  `vod_tv` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电视台/播出平台',
  `vod_weekday` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '每周更新日',
  `vod_area` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '制片国家/地区',
  `vod_lang` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '语言，如：汉语普通话',
  `vod_year` char(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上映年份',
  `vod_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本，如：剧场版、OVA',
  `vod_state` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态描述，如：更新至XX集',
  `vod_author` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者',
  `vod_jumpurl` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转URL',
  `vod_tpl` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义模板',
  `vod_tpl_play` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播放页模板',
  `vod_tpl_down` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载页模板',
  `vod_isend` tinyint NULL DEFAULT 0 COMMENT '是否完结：0-未完结，1-已完结',
  `vod_lock` tinyint NULL DEFAULT 0 COMMENT '是否锁定：0-未锁定，1-锁定',
  `vod_level` tinyint NULL DEFAULT 0 COMMENT '访问所需用户等级，0表示不限等级',
  `vod_copyright` tinyint NULL DEFAULT 0 COMMENT '版权状态：0-无版权限制',
  `vod_points` int NULL DEFAULT 0 COMMENT '观看所需积分，0表示不需要',
  `vod_points_play` int NULL DEFAULT 0 COMMENT '播放所需积分',
  `vod_points_down` int NULL DEFAULT 0 COMMENT '下载所需积分',
  `vod_hits` int NULL DEFAULT 0 COMMENT '总点击/播放量',
  `vod_hits_day` int NULL DEFAULT 0 COMMENT '日点击量',
  `vod_hits_week` int NULL DEFAULT 0 COMMENT '周点击量',
  `vod_hits_month` int NULL DEFAULT 0 COMMENT '月点击量',
  `vod_duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单集时长，如：22分钟',
  `vod_up` int NULL DEFAULT 0 COMMENT '点赞/顶的数量',
  `vod_down` int NULL DEFAULT 0 COMMENT '点踩/踩的数量',
  `vod_score` decimal(3, 1) NULL DEFAULT NULL COMMENT '评分，如：4.0',
  `vod_score_all` int NULL DEFAULT 0 COMMENT '总分/总评分值',
  `vod_score_num` int NULL DEFAULT 0 COMMENT '评分人数',
  `vod_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  `vod_time_add` int NULL DEFAULT NULL COMMENT '添加时间（Unix时间戳）',
  `vod_time_hits` int NULL DEFAULT 0 COMMENT '最近点击时间戳，0表示无记录',
  `vod_time_make` int NULL DEFAULT 0 COMMENT '制作/创建时间戳，0表示未设置',
  `vod_trysee` smallint NULL DEFAULT 0 COMMENT '试看时长（秒），0表示不能试看或完整观看',
  `vod_douban_id` int NULL DEFAULT NULL COMMENT '豆瓣电影ID',
  `vod_douban_score` decimal(3, 1) NULL DEFAULT 0.0 COMMENT '豆瓣评分',
  `vod_reurl` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转/重定向URL',
  `vod_rel_vod` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的视频ID列表',
  `vod_rel_art` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的文章ID列表',
  `vod_pwd` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问密码',
  `vod_pwd_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码访问的链接',
  `vod_pwd_play` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '播放密码',
  `vod_pwd_play_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码播放链接',
  `vod_pwd_down` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载密码',
  `vod_pwd_down_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码下载链接',
  `vod_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细介绍/剧情简介（可能很长，用TEXT）',
  `vod_play_from` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播放器来源标识，如：hhm3u8',
  `vod_play_server` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播放服务器地址',
  `vod_play_note` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播放备注，如：提示：请切换线路',
  `vod_down_from` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载来源标识',
  `vod_down_server` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载服务器地址',
  `vod_down_note` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载备注',
  `vod_down_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '下载URL数据',
  `vod_plot` smallint NULL DEFAULT 0 COMMENT '剧情/分集数量，0表示未设置',
  `vod_plot_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '剧情/分集名称',
  `vod_plot_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '剧情/分集详情',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称（冗余字段，便于直接展示）',
  `create_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `update_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频通用信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for cron_task
-- ----------------------------
DROP TABLE IF EXISTS `cron_task`;
CREATE TABLE `cron_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `task_type` int NOT NULL COMMENT '任务类型(25=日本动漫, 26=欧美动漫, 24=中国动漫)',
  `cron_expression` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Cron表达式',
  `hour` int NOT NULL DEFAULT 1 COMMENT '处理时间几小时内的数据',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用(1=启用, 0=禁用)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '任务状态(PENDING=待执行, RUNNING=运行中, COMPLETED=已完成, FAILED=执行失败, CANCELLED=已取消)',
  `last_execute_time` datetime NULL DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime NULL DEFAULT NULL COMMENT '下次执行时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for cron_task_log
-- ----------------------------
DROP TABLE IF EXISTS `cron_task_log`;
CREATE TABLE `cron_task_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `task_type` int NOT NULL COMMENT '任务类型',
  `start_time` datetime NOT NULL COMMENT '开始执行时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束执行时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'RUNNING' COMMENT '执行状态(RUNNING=执行中, SUCCESS=执行成功, FAILED=执行失败, CANCELLED=已取消)',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '执行信息或错误信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务执行记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `time_point` float NOT NULL,
  `color` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#FFFFFF',
  `dm_type` tinyint NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_time`(`video_id` ASC, `time_point` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for search_log
-- ----------------------------
DROP TABLE IF EXISTS `search_log`;
CREATE TABLE `search_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '搜索关键词',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID，未登录为NULL',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '搜索IP',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_keyword`(`keyword` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '搜索记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `word` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `level` tinyint NULL DEFAULT 1,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` tinyint NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 0,
  `points` int NULL DEFAULT 10,
  `last_login_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `anime_id` bigint NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_anime`(`user_id` ASC, `anime_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_review
-- ----------------------------
DROP TABLE IF EXISTS `user_review`;
CREATE TABLE `user_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `anime_id` bigint NOT NULL,
  `rating` tinyint NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `like_count` int NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_anime`(`user_id` ASC, `anime_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint NOT NULL COMMENT '主键ID / 编号',
  `anime_id` bigint NOT NULL COMMENT '动漫ID / 作品ID',
  `episode` int NOT NULL COMMENT '集数 / 第几集',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题 / 剧集名称',
  `m3u8_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'M3U8播放地址 / 视频流地址',
  `duration` int NULL DEFAULT 0 COMMENT '时长 / 播放时长',
  `view_count` int NULL DEFAULT 0 COMMENT '播放量 / 观看次数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `m3u8_url2` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备用线路二(lzizy)',
  `m3u8_url3` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备用线路三(1080zyk3)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for visit_log
-- ----------------------------
DROP TABLE IF EXISTS `visit_log`;
CREATE TABLE `visit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `page` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问页面',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问IP',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `visit_date` date NULL DEFAULT NULL COMMENT '访问日期',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_visit_date`(`visit_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '访问日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for watch_history
-- ----------------------------
DROP TABLE IF EXISTS `watch_history`;
CREATE TABLE `watch_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `anime_id` bigint NOT NULL,
  `video_id` bigint NOT NULL,
  `episode` int NULL DEFAULT 1,
  `progress` float NULL DEFAULT 0,
  `duration` float NULL DEFAULT 0,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_anime`(`user_id` ASC, `anime_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
