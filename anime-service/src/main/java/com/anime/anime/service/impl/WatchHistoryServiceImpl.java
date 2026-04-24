package com.anime.anime.service.impl;

import com.anime.anime.entity.WatchHistory;
import com.anime.anime.entity.dto.WatchHistorySaveRequest;
import com.anime.anime.mapper.WatchHistoryMapper;
import com.anime.anime.service.WatchHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 观看历史 Service 实现类
 */
@Service
public class WatchHistoryServiceImpl extends ServiceImpl<WatchHistoryMapper, WatchHistory> implements WatchHistoryService {

    @Override
    @Transactional
    public void saveOrUpdateHistory(WatchHistorySaveRequest request) {
        // 查询是否已存在该番剧的观看历史
        WatchHistory existing = baseMapper.selectByUserIdAndAnimeId(request.getUserId(), request.getAnimeId());
        
        LocalDateTime now = LocalDateTime.now();
        
        if (existing != null) {
            // 更新现有记录
            existing.setVideoId(request.getVideoId());
            existing.setEpisode(request.getEpisode());
            if (request.getProgress() != null) {
                existing.setProgress(request.getProgress());
            }
            if (request.getWatchDuration() != null) {
                existing.setWatchDuration(request.getWatchDuration());
            }
            existing.setVodName(request.getVodName());
            if (request.getVodPic() != null) {
                existing.setVodPic(request.getVodPic());
            }
            if (request.getVodTotal() != null) {
                existing.setVodTotal(request.getVodTotal());
            }
            existing.setLastWatchTime(now);
            existing.setUpdatedAt(now);
            baseMapper.updateById(existing);
        } else {
            // 创建新记录
            WatchHistory history = new WatchHistory();
            BeanUtils.copyProperties(request,history);
            history.setProgress(request.getProgress() != null ? request.getProgress() : 0.0);
            history.setWatchDuration(request.getWatchDuration() != null ? request.getWatchDuration() : 0);
            history.setLastWatchTime(now);
            history.setCreatedAt(now);
            history.setUpdatedAt(now);
            baseMapper.insert(history);
        }
    }

    @Override
    public List<WatchHistory> getUserHistory(Long userId, int limit) {
        return baseMapper.selectByUserId(userId, limit);
    }

    @Override
    @Transactional
    public void deleteHistory(Long userId, Long animeId) {
        baseMapper.deleteByUserIdAndAnimeId(userId, animeId);
    }

    @Override
    @Transactional
    public void clearUserHistory(Long userId) {
        baseMapper.deleteByUserId(userId);
    }

    @Override
    public int countUserHistory(Long userId) {
        return baseMapper.countByUserId(userId);
    }
}
