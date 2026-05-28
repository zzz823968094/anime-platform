package com.anime.ad.entity;

import com.anime.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 广告实体类
 * 遵循阿里巴巴开发规范，继承BaseEntity统一管理通用字段
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad")
public class Ad extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 广告位编码
     */
    private String positionCode;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 广告副标题
     */
    private String subtitle;

    /**
     * 广告图片URL
     */
    private String imageUrl;

    /**
     * 广告视频URL
     */
    private String videoUrl;

    /**
     * HTML内容
     */
    private String htmlContent;

    /**
     * 链接类型（URL/ROUTE/WEBVIEW）
     */
    private String linkType;

    /**
     * 链接值
     */
    private String linkValue;

    /**
     * 开始时间
     */
    private java.time.LocalDateTime startTime;

    /**
     * 结束时间
     */
    private java.time.LocalDateTime endTime;

    /**
     * 目标类型（ALL/NEW_USER/OLD_USER）
     */
    private String targetType;

    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 点击次数
     */
    private Long clickCount;

    /**
     * 展示次数
     */
    private Long impressionCount;

    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 扩展数据（JSON格式）
     */
    private String extraData;
}
