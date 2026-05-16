package com.anime.user.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 * 遵循阿里巴巴开发规范，对外暴露的用户信息（不包含敏感字段如密码）
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
