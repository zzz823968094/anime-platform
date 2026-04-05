package com.anime.anime.mapper;

import com.anime.anime.entity.SearchLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    // 热门搜索词统计（按搜索次数降序）
    @Select("SELECT keyword, COUNT(*) as cnt FROM search_log " +
            "GROUP BY keyword ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> hotKeywords(int limit);

    // 最近N天的搜索趋势（按天统计）
    @Select("SELECT DATE(created_at) as date, COUNT(*) as cnt FROM search_log " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date ASC")
    List<Map<String, Object>> searchTrend(int days);
}