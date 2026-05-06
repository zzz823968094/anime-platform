package com.anime.ad.service;

import com.anime.ad.entity.AdPosition;
import com.anime.ad.entity.dto.AdPositionDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdPositionService extends IService<AdPosition> {

    List<AdPosition> getAllActivePositions();

    boolean createPosition(AdPositionDTO positionDTO);

    boolean updatePosition(Long id, AdPositionDTO positionDTO);

    boolean deletePosition(Long id);
}
