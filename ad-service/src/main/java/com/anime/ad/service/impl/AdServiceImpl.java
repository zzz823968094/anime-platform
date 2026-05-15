package com.anime.ad.service.impl;

import com.anime.ad.entity.Ad;
import com.anime.ad.entity.AdStrategy;
import com.anime.ad.entity.dto.AdDTO;
import com.anime.ad.mapper.AdMapper;
import com.anime.ad.service.AdService;
import com.anime.ad.service.AdStrategyService;
import com.anime.common.constant.CommonConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements AdService {

    private final AdStrategyService adStrategyService;

    @Override
    public Page<Ad> pageAds(Integer current, Integer size, String positionCode, Integer status) {
        Page<Ad> page = new Page<>(current, size);
        LambdaQueryWrapper<Ad> wrapper = new LambdaQueryWrapper<>();
        
        if (positionCode != null && !positionCode.isEmpty()) {
            wrapper.eq(Ad::getPositionCode, positionCode);
        }
        if (status != null) {
            wrapper.eq(Ad::getStatus, status);
        }
        
        wrapper.orderByDesc(Ad::getPriority)
               .orderByAsc(Ad::getSortOrder)
               .orderByDesc(Ad::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<Ad> getActiveAdsByPosition(String positionCode) {
        return getActiveAdsByPositionWithStrategy(positionCode, null, null, null);
    }

    /**
     * 根据广告位获取有效广告（带策略过滤）
     */
    public List<Ad> getActiveAdsByPositionWithStrategy(String positionCode, String deviceType, String os, String region) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Ad> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Ad::getPositionCode, positionCode)
               .eq(Ad::getStatus, CommonConstant.AD_STATUS_ENABLED)
               .le(Ad::getStartTime, now)
               .ge(Ad::getEndTime, now)
               .orderByDesc(Ad::getPriority)
               .orderByAsc(Ad::getSortOrder);
        
        List<Ad> allAds = this.list(wrapper);
        
        // 如果没有策略参数，直接返回所有广告
        if (deviceType == null && os == null && region == null) {
            return allAds;
        }
        
        // 应用策略过滤
        return allAds.stream()
                .filter(ad -> {
                    List<AdStrategy> strategies = adStrategyService.getStrategiesByAdId(ad.getId());
                    // 如果没有策略，默认展示
                    if (strategies.isEmpty()) {
                        return true;
                    }
                    // 检查是否匹配所有策略
                    return strategies.stream()
                            .allMatch(strategy -> adStrategyService.matchesStrategy(strategy, deviceType, os, region));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createAd(AdDTO adDTO) {
        Ad ad = new Ad();
        BeanUtils.copyProperties(adDTO, ad);
        ad.setClickCount(0L);
        ad.setImpressionCount(0L);
        ad.setCreateTime(LocalDateTime.now());
        ad.setUpdateTime(LocalDateTime.now());
        return this.save(ad);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAd(Long id, AdDTO adDTO) {
        Ad ad = this.getById(id);
        if (ad == null) {
            log.warn("广告不存在，id: {}", id);
            return false;
        }
        
        BeanUtils.copyProperties(adDTO, ad);
        ad.setId(id);
        ad.setUpdateTime(LocalDateTime.now());
        return this.updateById(ad);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAd(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordImpression(Long adId) {
        Ad ad = this.getById(adId);
        if (ad != null) {
            ad.setImpressionCount(ad.getImpressionCount() + 1);
            this.updateById(ad);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordClick(Long adId) {
        Ad ad = this.getById(adId);
        if (ad != null) {
            ad.setClickCount(ad.getClickCount() + 1);
            this.updateById(ad);
        }
    }
}
