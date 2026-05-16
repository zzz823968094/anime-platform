package com.anime.admin.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员数据传输对象
 * 用于接收前端参数，包含校验规则
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
public class AdminUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号（4-16位，只能包含字母、数字和下划线）
     */
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,16}$", message = "账号格式不正确，4-16位字母、数字或下划线")
    private String account;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 密码（可选，更新时提供）
     */
    private String password;
}
