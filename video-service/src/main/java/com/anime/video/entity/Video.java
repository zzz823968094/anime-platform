package com.anime.video.entity;

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
 * 视频实体类
 * 遵循阿里巴巴开发规范
 * 注意：video表使用created_at和updated_at
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video")
public class Video extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 动漫ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long animeId;
    
    /**
     * 集数
     */
    private Integer episode;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * M3U8播放地址
     */
    private String m3u8Url;
    
    /**
     * 时长（秒）
     */
    private Integer duration;
    
    /**
     * 播放量
     */
    private Integer viewCount;
    
    /**
     * 状态：0-下线，1-上线
     */
    private Integer status;

    /**
     * 覆盖父类的createTime，映射到video表的created_at
     */
    @TableField("created_at")
    private LocalDateTime createTime;

    /**
     * 覆盖父类的updateTime，映射到video表的updated_at
     */
    @TableField("updated_at")
    private LocalDateTime updateTime;

    /**
     * video表没有createBy字段，忽略
     */
    @TableField(exist = false)
    private Long createBy;

    /**
     * video表没有updateBy字段，忽略
     */
    @TableField(exist = false)
    private Long updateBy;
}