package com.anime.video.mapper;

import com.anime.video.entity.Video;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频数据访问层
 * 遵循阿里巴巴开发规范，继承BaseMapper提供基础CRUD操作
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}