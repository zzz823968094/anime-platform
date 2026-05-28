package com.anime.ad.service.impl;

import com.anime.ad.entity.AdPosition;
import com.anime.ad.entity.dto.AdPositionDTO;
import com.anime.ad.mapper.AdPositionMapper;
import com.anime.ad.service.AdPositionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AdPositionServiceImpl extends ServiceImpl<AdPositionMapper, AdPosition> implements AdPositionService {

    @Override
    public List<AdPosition> getAllActivePositions() {
        LambdaQueryWrapper<AdPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdPosition::getStatus, 1)
                .orderByAsc(AdPosition::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPosition(AdPositionDTO positionDTO) {
        AdPosition position = new AdPosition();
        BeanUtils.copyProperties(positionDTO, position);
        position.setStatus(position.getStatus() == null ? 1 : position.getStatus());
        return this.save(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePosition(Long id, AdPositionDTO positionDTO) {
        AdPosition position = this.getById(id);
        if (position == null) {
            log.warn("广告位不存在，id: {}", id);
            return false;
        }

        BeanUtils.copyProperties(positionDTO, position);
        position.setId(id);
        return this.updateById(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePosition(Long id) {
        return this.removeById(id);
    }
}
