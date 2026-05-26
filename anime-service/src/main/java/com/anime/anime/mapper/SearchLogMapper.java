package com.anime.anime.mapper;

import com.anime.anime.entity.SearchLog;
import com.anime.anime.entity.vo.SearchListVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    // 热门搜索词统计（按搜索次数降序）
    List<Map<String, Object>> hotKeywords(@Param("limit") int limit, @Param("days") int days);

    // 最近N天的搜索趋势（按天统计）
    List<Map<String, Object>> searchTrend(@Param("days") int days);

    Long hotKeywordsCount();

    List<SearchListVO> keywordList(@Param("days") int days, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    Long keywordListCount(@Param("days") int days, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

}