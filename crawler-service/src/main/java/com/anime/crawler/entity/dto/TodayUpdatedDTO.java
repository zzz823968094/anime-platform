package com.anime.crawler.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * 今日已更新的动漫数据
 */
@Data
public class TodayUpdatedDTO {
    /**
     * 动漫ID
     */
    private String vodId;
    /**
     * 动漫名称
     */
    private String vodName;

    /**
     * 封面图片地址
     */
    private String vodPic;

    /**
     * 简介/短描述
     */
    private String vodBlurb;
    /**
     * 总集数
     */
    private Integer vodTotal;

    /**
     * 主分类ID
     */
    private Integer typeId;

    /**
     * 一级分类ID
     */
    private Integer typeId1;

    /**
     * 分组ID，0表示未分组
     */
    private Integer groupId;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 状态 0:新增 1:更新
     */
    private Integer status;
}
