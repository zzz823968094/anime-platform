package com.anime.user.mapper;

import com.anime.user.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动漫表Mapper接口
 * 遵循阿里巴巴开发规范，继承BaseMapper提供基础CRUD能力
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {

    /**
     * 统计总播放量（使用MySQL SUM函数）
     *
     * @return 总播放量
     */
    Long sumTotalViewCount();

    /**
     * 统计今日播放量（使用MySQL SUM函数）
     *
     * @return 今日播放量
     */
    Long sumTodayViewCount();
}