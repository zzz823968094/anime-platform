package com.anime.crawler.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 爬虫任务进度信息DTO
 */
@Data
public class CrawlerProgressInfo implements Serializable {

    private String taskKey;

    /**
     * 任务类型：66=中国动漫, 67=日本动漫, 68=欧美动漫等
     */
    private Integer taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 当前已处理页数
     */
    private Integer processedPages;

    /**
     * 总数据量
     */
    private Integer totalItems;

    /**
     * 已处理数据量
     */
    private Integer processedItems;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 进度百分比 (0-100)
     */
    private Integer progressPercent;

    /**
     * 状态：RUNNING, COMPLETED, FAILED, CANCELLED
     */
    private String status;

    /**
     * 当前处理的页码
     */
    private Integer currentPage;

    /**
     * 最后更新时间
     */
    private String updateTime;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    public CrawlerProgressInfo() {
    }

    public CrawlerProgressInfo(String taskKey, Integer taskType, String taskName) {
        this.taskKey = taskKey;
        this.taskType = taskType;
        this.taskName = taskName;
        this.processedPages = 0;
        this.totalItems = 0;
        this.processedItems = 0;
        this.successCount = 0;
        this.failCount = 0;
        this.progressPercent = 0;
        this.status = "RUNNING";
        this.currentPage = 0;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.startTime = now.format(formatter);
        this.updateTime = this.startTime;
    }
}
