package com.anime.admin.service.impl;

import com.anime.admin.entity.Carousel;
import com.anime.admin.mapper.CarouselMapper;
import com.anime.admin.service.CarouselService;
import com.anime.common.exception.BusinessException;
import com.anime.common.utils.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel> implements CarouselService {

    private static final int MAX_ENABLED_COUNT = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Carousel create(Carousel carousel) {
        if (carousel.getSortOrder() == null) {
            carousel.setSortOrder(0);
        }
        if (carousel.getStatus() == null) {
            carousel.setStatus("enabled");
        }
        
        // 如果创建时状态为启用，检查是否已达到最大限制
        if ("enabled".equals(carousel.getStatus())) {
            ensureMaxEnabledLimit();
        }
        
        carousel.setId(IdUtil.nextId());
        carousel.setCreateTime(LocalDateTime.now());
        carousel.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(carousel);
        return carousel;
    }

    @Override
    public Carousel update(Long id, Carousel carousel) {
        Carousel existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        if (carousel.getSortOrder() != null) existing.setSortOrder(carousel.getSortOrder());
        if (carousel.getVideoId() != null) existing.setVideoId(carousel.getVideoId());
        if (carousel.getType() != null) existing.setType(carousel.getType());
        if (carousel.getStatus() != null) existing.setStatus(carousel.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        if (baseMapper.selectById(id) == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        Carousel carousel = baseMapper.selectById(id);
        if (carousel == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        
        // 如果已经是启用状态，直接返回
        if ("enabled".equals(carousel.getStatus())) {
            return;
        }
        
        // 检查并管理最大启用数量限制
        ensureMaxEnabledLimit();
        
        // 启用当前轮播图
        carousel.setStatus("enabled");
        carousel.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(carousel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        Carousel carousel = baseMapper.selectById(id);
        if (carousel == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        
        // 如果已经是禁用状态，直接返回
        if ("disabled".equals(carousel.getStatus())) {
            return;
        }
        
        carousel.setStatus("disabled");
        carousel.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(carousel);
    }

    /**
     * 确保启用的轮播图数量不超过最大限制
     * 如果已达到限制，自动禁用 updateTime 最早的一个
     */
    private void ensureMaxEnabledLimit() {
        long enabledCount = lambdaQuery()
                .eq(Carousel::getStatus, "enabled")
                .count();
        
        if (enabledCount >= MAX_ENABLED_COUNT) {
            Carousel oldestEnabled = lambdaQuery()
                    .eq(Carousel::getStatus, "enabled")
                    .orderByAsc(Carousel::getUpdateTime)
                    .last("LIMIT 1")
                    .one();
            
            if (oldestEnabled != null) {
                oldestEnabled.setStatus("disabled");
                oldestEnabled.setUpdateTime(LocalDateTime.now());
                baseMapper.updateById(oldestEnabled);
            }
        }
    }
}
