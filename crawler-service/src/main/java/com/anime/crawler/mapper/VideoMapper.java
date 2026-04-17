package com.anime.crawler.mapper;

import com.anime.crawler.entity.Video;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    /**
     * 批量插入视频数据,忽略重复记录
     */
    int insertBatchIgnore(@Param("list") List<Video> list);

    /**
     * 统计指定动漫的视频集数
     */
    int countByAnimeId(@Param("animeId") Long animeId);
}