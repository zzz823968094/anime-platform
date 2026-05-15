package com.anime.user.mapper;

import com.anime.user.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {
    /**
     * 统计总播放量(使用MySQL SUM函数)
     */
    Long sumTotalViewCount();
    /**
     * 统计今日播放量(使用MySQL SUM函数)
     */
    Long sumTodayViewCount();
}