package com.anime.ad.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdDTO {
    private Long id;

    private String positionCode;

    private String title;

    private String subtitle;

    private String imageUrl;

    private String videoUrl;

    private String htmlContent;

    private String linkType;

    private String linkValue;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String targetType;

    private Integer priority;

    private Integer status;

    private Integer sortOrder;

    private String extraData;
}
