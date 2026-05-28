package com.anime.anime.service.impl;

import com.anime.anime.entity.AnimeTable;
import com.anime.anime.mapper.AnimeTableMapper;
import com.anime.anime.service.AnimeTableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 动漫表服务实现类
 * 遵循阿里巴巴开发规范，业务逻辑下沉，事务控制
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Service
public class AnimeTableServiceImpl extends ServiceImpl<AnimeTableMapper, AnimeTable> implements AnimeTableService {

    @Override
    public Page<AnimeTable> listAnime(int page, int size, String type, Integer status,
                                      Integer year, String genre, String sort, String keyword) {
        log.info("分页查询动漫列表，page: {}, size: {}, keyword: {}", page, size, keyword);

        LambdaQueryWrapper<AnimeTable> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AnimeTable::getVodName, keyword));
        }
        if (StringUtils.hasText(type)) wrapper.eq(AnimeTable::getTypeId, type);
        if (year != null) wrapper.eq(AnimeTable::getVodYear, year);
        if (StringUtils.hasText(genre)) wrapper.like(AnimeTable::getVodClass, genre);
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(AnimeTable::getVodHits);
        } else {
            wrapper.orderByDesc(AnimeTable::getUpdateAt);
        }

        Page<AnimeTable> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        log.info("分页查询动漫列表完成，total: {}", result.getTotal());
        return result;
    }

    @Override
    public Page<AnimeTable> search(String keyword, int page, int size) {
        log.info("搜索动漫，keyword: {}, page: {}, size: {}", keyword, page, size);

        LambdaQueryWrapper<AnimeTable> wrapper = new LambdaQueryWrapper<>();
        if (null != keyword) {
            wrapper.and(w -> w.like(AnimeTable::getVodName, keyword));
        }
        wrapper.orderByDesc(AnimeTable::getUpdateAt);

        Page<AnimeTable> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        log.info("搜索动漫完成，total: {}", result.getTotal());
        return result;
    }

    @Override
    public List<AnimeTable> getHotRecommend(int count) {
        log.info("获取热门推荐，count: {}", count);

        // 检查是否有真实播放量数据
        long hasView = lambdaQuery()
                .gt(AnimeTable::getVodHits, 0)
                .count();

        LambdaQueryWrapper<AnimeTable> wrapper = new LambdaQueryWrapper<>();

        if (hasView > 0) {
            // 上线后有真实播放量：按播放量降序
            wrapper.orderByDesc(AnimeTable::getVodHits);
        } else {
            // 本地开发/冷启动阶段：连载中优先 + 评分降序 + 最近更新
            wrapper.orderByDesc(AnimeTable::getVodScore)
                    .orderByDesc(AnimeTable::getUpdateAt);
        }

        wrapper.last("LIMIT " + count);
        List<AnimeTable> result = list(wrapper);
        log.info("获取热门推荐完成，size: {}", result.size());
        return result;
    }
}