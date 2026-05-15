package com.anime.common.enums;

import lombok.Getter;

/**
 * 统一状态码枚举
 * 遵循阿里巴巴开发规范，所有业务状态码集中管理
 *
 * @author anime-platform
 * @date 2026-05-12
 */
@Getter
public enum ResultCodeEnum {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 服务器内部错误
     */
    ERROR(500, "服务器内部错误，请联系管理员"),

    /**
     * 参数校验失败
     */
    PARAM_ERROR(400, "参数校验失败"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "请求的资源不存在"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 业务异常 - 用户名已存在
     */
    USERNAME_EXISTS(430, "用户名已存在"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 描述信息
     */
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
