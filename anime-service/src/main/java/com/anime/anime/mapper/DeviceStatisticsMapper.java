package com.anime.anime.mapper;

import com.anime.anime.entity.DeviceStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备统计 Mapper
 */
@Mapper
public interface DeviceStatisticsMapper extends BaseMapper<DeviceStatistics> {

    /**
     * 查询最近N天的设备统计趋势
     *
     * @param days 天数
     * @return 设备统计列表
     */
    List<DeviceStatistics> getDeviceTrend(@Param("days") int days);

    /**
     * 查询指定日期的设备统计
     *
     * @param date 日期 YYYYMMDD
     * @return 设备统计列表
     */
    List<DeviceStatistics> getDeviceByDate(@Param("date") Integer date);

    /**
     * 查询总访问人数（所有日期的累加）
     *
     * @return 总访问记录数
     */
    Long getTotalUserCount();
}
