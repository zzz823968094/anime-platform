-- 为轮播图表添加视频名称和封面的字段
ALTER TABLE `carousel` 
ADD COLUMN `video_name` VARCHAR(200) DEFAULT NULL COMMENT '视频名称' AFTER `video_id`,
ADD COLUMN `video_cover` VARCHAR(500) DEFAULT NULL COMMENT '视频封面URL' AFTER `video_name`;
