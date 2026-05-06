package com.anime.ad.service;

import com.anime.ad.entity.AdStrategy;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdStrategyService extends IService<AdStrategy> {

    List<AdStrategy> getStrategiesByAdId(Long adId);

    boolean matchesStrategy(AdStrategy strategy, String deviceType, String os, String region);
}
