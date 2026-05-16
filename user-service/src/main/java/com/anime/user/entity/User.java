package com.anime.user.entity;

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
 * 用户实体类
 * 遵循阿里巴巴开发规范，继承BaseEntity
 * 注意：user表使用 created_at 而非 create_time
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码（BCrypt加密存储）
     */
    private String password;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色（0-普通用户，1-管理员）
     */
    private Integer role;

    /**
     * 状态（0-禁用，1-正常）
     */
    private Integer status;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 覆盖父类的createTime字段，映射到user表的created_at
     */
    @TableField("created_at")
    private LocalDateTime createTime;

    /**
     * user表没有updateTime字段，忽略该字段
     */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /**
     * user表没有createBy字段，忽略该字段
     */
    @TableField(exist = false)
    private Long createBy;

    /**
     * user表没有updateBy字段，忽略该字段
     */
    @TableField(exist = false)
    private Long updateBy;
}