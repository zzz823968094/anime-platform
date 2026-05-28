package com.anime.ad.service.impl;

import com.anime.ad.entity.Ad;
import com.anime.ad.entity.AdStrategy;
import com.anime.ad.entity.dto.AdDTO;
import com.anime.ad.mapper.AdMapper;
import com.anime.ad.service.AdService;
import com.anime.ad.service.AdStrategyService;
import com.anime.common.constant.CommonConstant;
import com.anime.common.enums.ResultCodeEnum;
import com.anime.common.exception.BusinessException;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 广告服务实现类
 * 遵循阿里巴巴开发规范，业务逻辑下沉，事务控制
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements AdService {

    private final AdStrategyService adStrategyService;

    @Override
    public Page<Ad> pageAds(Integer current, Integer size, String positionCode, Integer status) {
        log.info("分页查询广告列表，current: {}, size: {}, positionCode: {}, status: {}",
                current, size, positionCode, status);

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

        Page<Ad> result = this.page(page, wrapper);
        log.info("分页查询广告列表完成，total: {}", result.getTotal());
        return result;
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
        log.info("开始创建广告，title: {}", adDTO.getTitle());

        // 业务前置校验
        if (Objects.isNull(adDTO.getPositionCode()) || adDTO.getPositionCode().isEmpty()) {
            log.warn("广告位编码不能为空");
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "广告位编码不能为空");
        }

        Ad ad = new Ad();
        BeanUtils.copyProperties(adDTO, ad);
        ad.setClickCount(0L);
        ad.setImpressionCount(0L);
        ad.setStatus(CommonConstant.ONE);  // 默认启用

        boolean result = this.save(ad);
        log.info("广告创建{}，id: {}", result ? "成功" : "失败", ad.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAd(Long id, AdDTO adDTO) {
        log.info("开始更新广告，id: {}", id);

        Ad ad = this.getById(id);
        if (Objects.isNull(ad)) {
            log.warn("广告不存在，id: {}", id);
            throw new BusinessException(ResultCodeEnum.DATA_NOT_FOUND);
        }

        BeanUtils.copyProperties(adDTO, ad);
        ad.setId(id);

        boolean result = this.updateById(ad);
        log.info("广告更新{}，id: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAd(Long id) {
        log.info("开始删除广告，id: {}", id);

        Ad ad = this.getById(id);
        if (Objects.isNull(ad)) {
            log.warn("广告不存在，id: {}", id);
            throw new BusinessException(ResultCodeEnum.DATA_NOT_FOUND);
        }

        boolean result = this.removeById(id);
        log.info("广告删除{}，id: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordImpression(Long adId) {
        log.debug("记录广告展示，adId: {}", adId);

        Ad ad = this.getById(adId);
        if (Objects.nonNull(ad)) {
            ad.setImpressionCount(ad.getImpressionCount() + 1);
            this.updateById(ad);
            log.debug("广告展示记录成功，adId: {}, impressionCount: {}", adId, ad.getImpressionCount());
        } else {
            log.warn("广告不存在，无法记录展示，adId: {}", adId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordClick(Long adId) {
        log.debug("记录广告点击，adId: {}", adId);

        Ad ad = this.getById(adId);
        if (Objects.nonNull(ad)) {
            ad.setClickCount(ad.getClickCount() + 1);
            this.updateById(ad);
            log.debug("广告点击记录成功，adId: {}, clickCount: {}", adId, ad.getClickCount());
        } else {
            log.warn("广告不存在，无法记录点击，adId: {}", adId);
        }
    }
}
