package com.anime.ad.entity.dto;

import lombok.Data;

@Data
public class AdPositionDTO {
    private Long id;

    private String positionCode;

    private String positionName;

    private String description;

    private String displayType;

    private Integer width;

    private Integer height;

    private Integer maxCount;

    private Integer status;

    private Integer sortOrder;
}
