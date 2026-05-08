package com.anime.anime.mapper;

import com.anime.anime.entity.DeviceStatistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT date, device_model, os, user_count FROM device_statistics " +
            "WHERE date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL #{days} DAY), '%Y%m%d') " +
            "ORDER BY date ASC, user_count DESC")
    @Results({
            @Result(property = "date", column = "date"),
            @Result(property = "deviceModel", column = "device_model"),
            @Result(property = "os", column = "os"),
            @Result(property = "userCount", column = "user_count")
    })
    List<DeviceStatistics> getDeviceTrend(@Param("days") int days);

    /**
     * 查询指定日期的设备统计
     *
     * @param date 日期 YYYYMMDD
     * @return 设备统计列表
     */
    @Select("SELECT date, device_model, os, user_count FROM device_statistics " +
            "WHERE date = #{date} " +
            "ORDER BY user_count DESC")
    @Results({
            @Result(property = "date", column = "date"),
            @Result(property = "deviceModel", column = "device_model"),
            @Result(property = "os", column = "os"),
            @Result(property = "userCount", column = "user_count")
    })
    List<DeviceStatistics> getDeviceByDate(@Param("date") Integer date);

    /**
     * 查询总访问人数（所有日期的累加）
     *
     * @return 总访问记录数
     */
    @Select("SELECT SUM(user_count) AS total_user_count FROM device_statistics")
    Long getTotalUserCount();
}
