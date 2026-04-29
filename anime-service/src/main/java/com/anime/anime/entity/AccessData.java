package com.anime.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 访问统计数据实体类
 * 按天聚合存储访问数据
 */
@Data
@TableName("access_data")
public class AccessData {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 日期 YYYYMMDD 例如：20240101
     */
    private Integer date;

    /**
     * 访问人数（去重IP数）
     */
    private Integer userCount;

    /**
     * IP集合（JSON格式存储）
     */
    private String ip;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
