package com.anime.video.mapper;

import com.anime.video.entity.Anime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AnimeMapper extends BaseMapper<Anime> {

    // 直接用 SQL 原子自增，避免并发时读取再写入的数据竞争
    @Update("UPDATE anime SET view_count = view_count + 1 WHERE id = #{animeId}")
    void incrementViewCount(Long animeId);
}
