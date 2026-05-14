package com.anime.anime.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 地理位置统计DTO
 *
 * @author anime-platform
 * @since 2026-05-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStatDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 用户数量
     */
    private Long userCount;

    /**
     * IP数量
     */
    private Long ipCount;

    /**
     * 用户占比（百分比）
     */
    private Double userPercentage;
}
