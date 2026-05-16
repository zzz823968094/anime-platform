package com.anime.danmaku.entity;

import com.anime.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 弹幕实体类
 * 遵循阿里巴巴开发规范
 * 注意：danmaku表只有created_at字段
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("danmaku")
public class Danmaku extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 视频ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;
    
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    /**
     * 弹幕内容
     */
    private String content;
    
    /**
     * 时间点（秒）
     */
    private Float timePoint;
    
    /**
     * 颜色
     */
    private String color;
    
    /**
     * 弹幕类型：1-滚动，2-顶部，3-底部
     */
    private Integer dmType;
    
    /**
     * 状态：0-正常，1-屏蔽
     */
    private Integer status;

    /**
     * 覆盖父类的createTime，映射到danmaku表的created_at
     */
    @TableField("created_at")
    private LocalDateTime createTime;

    /**
     * danmaku表没有updateTime字段，忽略
     */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /**
     * danmaku表没有createBy字段，忽略
     */
    @TableField(exist = false)
    private Long createBy;

    /**
     * danmaku表没有updateBy字段，忽略
     */
    @TableField(exist = false)
    private Long updateBy;
}