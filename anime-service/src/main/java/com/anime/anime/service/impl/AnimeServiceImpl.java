package com.anime.anime.service.impl;

import com.anime.anime.entity.Anime;
import com.anime.anime.mapper.AnimeMapper;
import com.anime.anime.service.AnimeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AnimeServiceImpl extends ServiceImpl<AnimeMapper, Anime> implements AnimeService {

    @Override
    public Page<Anime> listAnime(int page, int size, String type, Integer status,
                                 Integer year, String genre, String sort, String keyword) {
        LambdaQueryWrapper<Anime> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Anime::getTitle, keyword)
                    .or()
                    .like(Anime::getTitleOriginal, keyword));
        }
        if (status != null) {
            wrapper.eq(Anime::getStatus, status);
        } else {
            wrapper.ne(Anime::getStatus, 0);
        }
        if (StringUtils.hasText(type)) wrapper.eq(Anime::getType, type);
        if (year != null) wrapper.eq(Anime::getYear, year);
        if (StringUtils.hasText(genre)) wrapper.like(Anime::getGenre, genre);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Anime::getViewCount);
        } else {
            wrapper.orderByDesc(Anime::getUpdatedAt);
        }
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Anime> search(String keyword, int page, int size) {
        LambdaQueryWrapper<Anime> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Anime::getStatus, 0);
        wrapper.and(w -> w.like(Anime::getTitle, keyword)
                .or()
                .like(Anime::getTitleOriginal, keyword));
        wrapper.orderByDesc(Anime::getUpdatedAt);
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Anime> getHotRecommend(int count) {
        // 检查是否有真实播放量数据
        long hasView = lambdaQuery()
                .ne(Anime::getStatus, 0)
                .gt(Anime::getViewCount, 0)
                .count();

        LambdaQueryWrapper<Anime> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Anime::getStatus, 0); // 过滤已下线

        if (hasView > 0) {
            // 上线后有真实播放量：按播放量降序
            wrapper.orderByDesc(Anime::getViewCount);
        } else {
            // 本地开发/冷启动阶段：连载中优先 + 评分降序 + 最近更新
            wrapper.orderByDesc(Anime::getStatus)    // status=1(连载) > status=2(完结)
                    .orderByDesc(Anime::getRating)
                    .orderByDesc(Anime::getUpdatedAt);
        }

        wrapper.last("LIMIT " + count);
        return list(wrapper);
    }
}