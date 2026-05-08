package com.anime.anime.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备统计DTO
 */
@Data
public class DeviceStatsDTO {
    
    /**
     * 总访问人数
     */
    private Long totalUserCount;
    
    /**
     * 设备列表（指定日期查询时返回）
     */
    private List<DeviceDetailDTO> deviceList;
    
    /**
     * 趋势数据（最近N天查询时返回）
     */
    private List<DeviceDetailDTO> trend;
    
    /**
     * 设备详情DTO
     */
    @Data
    public static class DeviceDetailDTO {
        /**
         * 日期 YYYYMMDD
         */
        private Integer date;
        
        /**
         * 设备型号
         */
        private String deviceModel;
        
        /**
         * 操作系统
         */
        private String os;
        
        /**
         * 访问人数
         */
        private Integer userCount;
    }
}
