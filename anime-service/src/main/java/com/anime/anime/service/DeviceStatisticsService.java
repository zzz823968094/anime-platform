package com.anime.anime.service;

import com.anime.anime.entity.DeviceStatistics;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DeviceStatisticsService extends IService<DeviceStatistics> {
    /**
     * 记录设备信息到Redis
     */
    void recordDevice(String ip, String deviceModel, String os);

    /**
     * 获取指定日期的设备统计数据
     */
    List<DeviceStatistics> getDeviceByDate(String date);

    /**
     * 获取最近N天的设备统计趋势
     */
    List<DeviceStatistics> getDeviceTrend(int days);

    /**
     * 获取总访问人数
     */
    Long getTotalUserCount();

    /**
     * 聚合设备数据到数据库
     */
    void aggregateDeviceData();
}
