package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 访问用户详情实体类
 * 存储每个用户的访问详情，用于留存率分析和地理位置统计
 *
 * @author anime-platform
 * @since 2026-05-14
 */
@Data
@TableName("access_user_detail")
public class AccessUserDetail implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户ID，未登录用户为NULL
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 客户端IP地址
     */
    private String ip;

    /**
     * 访问标识：app/web
     */
    private String sign;

    /**
     * 访问日期 YYYYMMDD 例如：20240101
     */
    private Integer visitDate;

    /**
     * 首次访问时间
     */
    private Date firstVisitTime;

    /**
     * 最后访问时间
     */
    private Date lastVisitTime;

    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 国家
     */
    private String locationCountry;

    /**
     * 省份
     */
    private String locationProvince;

    /**
     * 城市
     */
    private String locationCity;

    /**
     * 运营商
     */
    private String locationIsp;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
