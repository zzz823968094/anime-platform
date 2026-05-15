package com.anime.video.mapper;

import com.anime.video.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnimeTableMapper extends BaseMapper<AnimeTable> {

    // 直接用 SQL 原子自增，避免并发时读取再写入的数据竞争
    void incrementViewCount(Long animeId);
}
