package com.anime.anime.service;

import com.anime.anime.entity.WatchHistory;
import com.anime.anime.entity.dto.WatchHistorySaveRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 观看历史 Service 接口
 */
public interface WatchHistoryService extends IService<WatchHistory> {

    /**
     * 保存或更新观看历史
     * @param request 用户
     */
    void saveOrUpdateHistory(WatchHistorySaveRequest request);

    /**
     * 获取用户的观看历史列表
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 观看历史列表
     */
    List<WatchHistory> getUserHistory(Long userId, int limit);

    /**
     * 删除用户的某条观看历史
     * @param userId 用户ID
     * @param animeId 番剧ID
     */
    void deleteHistory(Long userId, Long animeId);

    /**
     * 清空用户的所有观看历史
     * @param userId 用户ID
     */
    void clearUserHistory(Long userId);

    /**
     * 统计用户的观看历史数量
     * @param userId 用户ID
     * @return 数量
     */
    int countUserHistory(Long userId);
}
