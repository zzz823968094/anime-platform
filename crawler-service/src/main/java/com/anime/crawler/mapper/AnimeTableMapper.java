package com.anime.crawler.mapper;

import com.anime.crawler.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 动漫表数据访问层
 * 遵循阿里巴巴开发规范，继承BaseMapper提供基础CRUD操作
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {

    /**
     * 批量插入动漫数据，忽略重复记录
     *
     * @param list 动漫列表
     * @return 插入数量
     */
    int insertBatchIgnore(@Param("list") List<AnimeTable> list);

    /**
     * 根据vodId查询动漫信息
     *
     * @param vodId 视频ID
     * @return 动漫信息
     */
    AnimeTable selectByVodId(@Param("vodId") Integer vodId);

    /**
     * 批量根据vodId查询动漫信息
     *
     * @param vodIds 视频ID列表
     * @return 动漫列表
     */
    List<AnimeTable> selectByVodIds(@Param("vodIds") List<Integer> vodIds);

    /**
     * 批量更新动漫数据（根据ID）
     *
     * @param list 动漫列表
     * @return 更新数量
     */
    int updateBatchById(@Param("list") List<AnimeTable> list);

    /**
     * 清除所有视频的日播放量
     *
     * @return 更新数量
     */
    int clearDailyViewCount();

    /**
     * 清除所有视频的周播放量
     *
     * @return 更新数量
     */
    int clearWeeklyViewCount();

    /**
     * 清除所有视频的月播放量
     *
     * @return 更新数量
     */
    int clearMonthlyViewCount();
}