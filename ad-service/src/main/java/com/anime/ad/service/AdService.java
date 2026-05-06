package com.anime.ad.service;

import com.anime.ad.entity.Ad;
import com.anime.ad.entity.dto.AdDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdService extends IService<Ad> {

    Page<Ad> pageAds(Integer current, Integer size, String positionCode, Integer status);

    List<Ad> getActiveAdsByPosition(String positionCode);

    boolean createAd(AdDTO adDTO);

    boolean updateAd(Long id, AdDTO adDTO);

    boolean deleteAd(Long id);

    void recordImpression(Long adId);

    void recordClick(Long adId);
}
