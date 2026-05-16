package com.anime.video.mapper;

import com.anime.video.entity.AnimeTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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
     * 原子自增播放量（避免并发问题）
     *
     * @param animeId 动漫ID
     */
    void incrementViewCount(Long animeId);
}
