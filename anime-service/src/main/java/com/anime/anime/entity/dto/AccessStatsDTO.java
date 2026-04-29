package com.anime.anime.entity.dto;

import com.anime.anime.entity.AccessData;
import lombok.Data;

import java.util.List;

/**
 * 访问统计响应数据传输对象
 */
@Data
public class AccessStatsDTO {

    /**
     * 今日实时访问人数（UV）
     */
    private Integer todayUV;

    /**
     * 总访问人数
     */
    private Long totalUserCount;

    /**
     * 最近N天访问趋势
     */
    private List<AccessData> trend;
}
