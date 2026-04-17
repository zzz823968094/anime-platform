package com.anime.video.mapper;

import com.anime.video.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {

    // 直接用 SQL 原子自增，避免并发时读取再写入的数据竞争
    @Update("UPDATE anime_table SET vod_hits = vod_hits + 1,vod_hits_day = vod_hits_day+1,vod_hits_week = vod_hits_week+1,vod_hits_month = vod_hits_month + 1 WHERE id = #{animeId}")
    void incrementViewCount(Long animeId);
}
