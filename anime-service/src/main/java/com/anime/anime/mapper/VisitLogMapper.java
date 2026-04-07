package com.anime.anime.mapper;

import com.anime.anime.entity.VisitLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {

    // 最近N天每天的 UV（按 IP 去重）
    @Select("SELECT visit_date as date, COUNT(DISTINCT ip) as uv " +
            "FROM visit_log " +
            "WHERE visit_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY visit_date ORDER BY visit_date ASC")
    List<Map<String, Object>> dailyUV(int days);

    // 今日 UV
    @Select("SELECT COUNT(DISTINCT ip) FROM visit_log WHERE visit_date = CURDATE()")
    long todayUV();

    // 今日 PV（总访问次数）
    @Select("SELECT COUNT(*) FROM visit_log WHERE visit_date = CURDATE()")
    long todayPV();

    // 最热门页面 TOP10
    @Select("SELECT page, COUNT(*) as cnt FROM visit_log " +
            "WHERE visit_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY page ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> hotPages();

    // 检查同一 IP 今天是否已上报过同一页面（防重复）
    @Select("SELECT COUNT(*) FROM visit_log WHERE ip = #{ip} AND page = #{page} AND visit_date = CURDATE()")
    long checkDuplicate(String ip, String page);
}