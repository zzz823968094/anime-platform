package com.anime.anime.mapper;

import com.anime.anime.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {
    /**
     * 统计总播放量(使用MySQL SUM函数)
     */
    @Select("SELECT COALESCE(SUM(vod_hits), 0) FROM anime_table WHERE vod_status != 0")
    Long sumTotalViewCount();
    /**
     * 统计今日播放量(使用MySQL SUM函数)
     */
    @Select("SELECT COALESCE(SUM(vod_hits_day), 0) FROM anime_table WHERE vod_status != 0")
    Long sumTodayViewCount();
}