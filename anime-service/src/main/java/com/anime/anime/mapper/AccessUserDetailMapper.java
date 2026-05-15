package com.anime.anime.mapper;

import com.anime.anime.entity.AccessUserDetail;
import com.anime.anime.entity.dto.LocationStatDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 访问用户详情 Mapper
 *
 * @author anime-platform
 * @since 2026-05-14
 */
@Mapper
public interface AccessUserDetailMapper extends BaseMapper<AccessUserDetail> {

    /**
     * 计算留存率
     *
     * @param baseDate 基准日期 YYYYMMDD
     * @param days     留存天数（1、7、15、30、180）
     * @return 留存用户数
     */
    Long getRetentionCount(@Param("baseDate") Integer baseDate, @Param("days") Integer days);

    /**
     * 获取基准日期的活跃用户数
     *
     * @param baseDate 基准日期 YYYYMMDD
     * @return 活跃用户数
     */
    Long getActiveUserCount(@Param("baseDate") Integer baseDate);

    /**
     * 按地理位置统计访问用户数
     *
     * @param days 统计最近N天的数据
     * @return 地理位置统计数据
     */
    List<LocationStatDTO> getLocationStats(@Param("days") Integer days);

    /**
     * 批量查询需要更新地理位置的记录
     *
     * @param limit 限制数量
     * @return 需要更新的记录列表
     */
    List<AccessUserDetail> selectNeedLocationUpdate(@Param("limit") Integer limit);
}
