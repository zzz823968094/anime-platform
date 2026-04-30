package com.anime.crawler.entity.dto;

import lombok.Data;

/**
 * 爬虫请求参数DTO
 */
@Data
public class CrawlerRequestDTO {
    /**
     * 动漫类型: 25=日本动漫, 26=欧美动漫, 24=中国动漫
     */
    private Integer type;
    
    /**
     * 爬取最近N小时的数据,默认24小时
     */
    private Integer hour;
}
