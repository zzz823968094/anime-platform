package com.anime.crawler.mapper;

import com.anime.crawler.entity.Anime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnimeMapper extends BaseMapper<Anime> {
}