package com.anime.admin.entity;

import com.anime.common.base.BaseEntity;
import com.anime.common.enums.UserStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员实体类
 * 遵循阿里巴巴开发规范
 * 注意：admin_user表只有create_time和update_time，没有create_by和update_by
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user")
public class AdminUser extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码（不对外暴露）
     */
    @JsonIgnore
    private String password;

    /**
     * 状态
     */
    private UserStatusEnum status;

    /**
     * admin_user表没有createBy字段，忽略该字段
     */
    @TableField(exist = false)
    private Long createBy;

    /**
     * admin_user表没有updateBy字段，忽略该字段
     */
    @TableField(exist = false)
    private Long updateBy;
}
