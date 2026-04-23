package com.anime.anime.service.impl;

import com.anime.anime.entity.AnimeTable;
import com.anime.anime.mapper.AnimeTableMapper;
import com.anime.anime.service.AnimeTableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AnimeTableServiceImpl extends ServiceImpl<AnimeTableMapper, AnimeTable> implements AnimeTableService {

    @Override
    public Page<AnimeTable> listAnime(int page, int size, String type, Integer status,
                                 Integer year, String genre, String sort, String keyword) {
        LambdaQueryWrapper<AnimeTable> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AnimeTable::getVodName, keyword));
        }
        if (status != null) {
            wrapper.eq(AnimeTable::getVodStatus, status);
        } else {
            wrapper.ne(AnimeTable::getVodStatus, 0);
        }
        if (StringUtils.hasText(type)) wrapper.eq(AnimeTable::getTypeId, type);
        if (year != null) wrapper.eq(AnimeTable::getVodYear, year);
        if (StringUtils.hasText(genre)) wrapper.like(AnimeTable::getVodClass, genre);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(AnimeTable::getVodHits);
        } else {
            wrapper.orderByDesc(AnimeTable::getUpdateAt);
        }
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<AnimeTable> search(String keyword, int page, int size) {
        LambdaQueryWrapper<AnimeTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(AnimeTable::getVodStatus, 0);
        if(null != keyword){
            wrapper.and(w -> w.like(AnimeTable::getVodName, keyword));
        }
        wrapper.orderByDesc(AnimeTable::getUpdateAt);
        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<AnimeTable> getHotRecommend(int count) {
        // 检查是否有真实播放量数据
        long hasView = lambdaQuery()
                .ne(AnimeTable::getVodStatus, 0)
                .gt(AnimeTable::getVodHits, 0)
                .count();

        LambdaQueryWrapper<AnimeTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(AnimeTable::getVodStatus, 0); // 过滤已下线

        if (hasView > 0) {
            // 上线后有真实播放量：按播放量降序
            wrapper.orderByDesc(AnimeTable::getVodHits);
        } else {
            // 本地开发/冷启动阶段：连载中优先 + 评分降序 + 最近更新
            wrapper.orderByDesc(AnimeTable::getVodStatus)    // status=1(连载) > status=2(完结)
                    .orderByDesc(AnimeTable::getVodScore)
                    .orderByDesc(AnimeTable::getUpdateAt);
        }

        wrapper.last("LIMIT " + count);
        return list(wrapper);
    }
}