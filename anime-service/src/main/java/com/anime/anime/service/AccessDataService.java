package com.anime.anime.service;

import com.anime.anime.entity.AccessData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AccessDataService extends IService<AccessData> {
    /**
     * 记录访问IP到Redis
     */
    void recordAccess(String ip);

    /**
     * 获取今日实时访问人数
     */
    Integer getTodayRealTimeUserCount();

    /**
     * 获取指定日期的访问数据
     */
    AccessData getAccessDataByDate(String date);

    /**
     * 获取最近N天的访问趋势
     */
    List<AccessData> getAccessTrend(int days);

    /**
     * 获取总访问人数
     */
    Long getTotalUserCount();
}