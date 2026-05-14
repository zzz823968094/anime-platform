package com.anime.anime.mapper;

import com.anime.anime.entity.AccessUserDetail;
import com.anime.anime.entity.dto.LocationStatDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT COUNT(DISTINCT a.user_id) as retention_count " +
            "FROM access_user_detail a " +
            "INNER JOIN access_user_detail b ON a.user_id = b.user_id " +
            "WHERE a.visit_date = #{baseDate} " +
            "AND b.visit_date = DATE_FORMAT(DATE_ADD(STR_TO_DATE(#{baseDate}, '%Y%m%d'), INTERVAL #{days} DAY), '%Y%m%d') " +
            "AND a.user_id IS NOT NULL")
    Long getRetentionCount(@Param("baseDate") Integer baseDate, @Param("days") Integer days);

    /**
     * 获取基准日期的活跃用户数
     *
     * @param baseDate 基准日期 YYYYMMDD
     * @return 活跃用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) as active_count " +
            "FROM access_user_detail " +
            "WHERE visit_date = #{baseDate} " +
            "AND user_id IS NOT NULL")
    Long getActiveUserCount(@Param("baseDate") Integer baseDate);

    /**
     * 按地理位置统计访问用户数
     *
     * @param days 统计最近N天的数据
     * @return 地理位置统计数据
     */
    @Select("SELECT " +
            "location_country as country, " +
            "location_province as province, " +
            "location_city as city, " +
            "COUNT(DISTINCT user_id) as userCount, " +
            "COUNT(DISTINCT ip) as ipCount " +
            "FROM access_user_detail " +
            "WHERE visit_date >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL #{days} DAY), '%Y%m%d') " +
            "AND location_country IS NOT NULL " +
            "GROUP BY location_country, location_province, location_city " +
            "ORDER BY userCount DESC")
    List<LocationStatDTO> getLocationStats(@Param("days") Integer days);

    /**
     * 批量查询需要更新地理位置的记录
     *
     * @param limit 限制数量
     * @return 需要更新的记录列表
     */
    @Select("SELECT id, ip FROM access_user_detail " +
            "WHERE location_country IS NULL " +
            "LIMIT #{limit}")
    List<AccessUserDetail> selectNeedLocationUpdate(@Param("limit") Integer limit);
}
