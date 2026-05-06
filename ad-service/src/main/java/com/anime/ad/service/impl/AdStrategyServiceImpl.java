package com.anime.ad.service.impl;

import com.anime.ad.entity.AdStrategy;
import com.anime.ad.mapper.AdStrategyMapper;
import com.anime.ad.service.AdStrategyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdStrategyServiceImpl extends ServiceImpl<AdStrategyMapper, AdStrategy> implements AdStrategyService {

    @Override
    public List<AdStrategy> getStrategiesByAdId(Long adId) {
        LambdaQueryWrapper<AdStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdStrategy::getAdId, adId)
               .eq(AdStrategy::getStatus, 1);
        return this.list(wrapper);
    }

    @Override
    public boolean matchesStrategy(AdStrategy strategy, String deviceType, String os, String region) {
        if (strategy == null || strategy.getStatus() != 1) {
            return false;
        }

        String strategyType = strategy.getStrategyType();
        String strategyValue = strategy.getStrategyValue();

        // 简单的策略匹配逻辑（实际项目中可以使用 JSON 解析库）
        switch (strategyType) {
            case "DEVICE":
                return strategyValue.contains(deviceType != null ? deviceType : "");
            case "OS":
                return strategyValue.contains(os != null ? os : "");
            case "REGION":
                return strategyValue.contains(region != null ? region : "");
            default:
                return true;
        }
    }
}
