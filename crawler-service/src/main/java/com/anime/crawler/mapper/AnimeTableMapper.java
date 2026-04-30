package com.anime.crawler.mapper;

import com.anime.crawler.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {
    /**
     * 批量插入动漫数据,忽略重复记录
     */
    int insertBatchIgnore(@Param("list") List<AnimeTable> list);

    /**
     * 根据vodId查询动漫信息
     */
    AnimeTable selectByVodId(@Param("vodId") Integer vodId);

    /**
     * 批量根据vodId查询动漫信息
     */
    List<AnimeTable> selectByVodIds(@Param("vodIds") List<Integer> vodIds);

    /**
     * 批量更新动漫数据(根据ID)
     */
    int updateBatchById(@Param("list") List<AnimeTable> list);
    /**
     * 清除所有视频的日播放量
     */
    int clearDailyViewCount();

    /**
     * 清除所有视频的周播放量
     */
    int clearWeeklyViewCount();

    /**
     * 清除所有视频的月播放量
     */
    int clearMonthlyViewCount();
}