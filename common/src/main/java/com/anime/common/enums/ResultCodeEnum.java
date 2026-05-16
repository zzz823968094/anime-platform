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
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(404, "数据不存在"),

    /**
     * 业务逻辑错误
     */
    BUSINESS_ERROR(501, "业务逻辑错误"),

    /**
     * 数据已存在
     */
    DATA_EXISTS(409, "数据已存在"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(500, "操作失败"),

    /**
     * 参数缺失
     */
    PARAM_MISSING(400, "参数缺失"),

    /**
     * 参数格式错误
     */
    PARAM_FORMAT_ERROR(400, "参数格式错误"),

    /**
     * Token过期
     */
    TOKEN_EXPIRED(401, "Token已过期，请重新登录"),

    /**
     * Token无效
     */
    TOKEN_INVALID(401, "Token无效"),

    /**
     * 权限不足
     */
    PERMISSION_DENIED(403, "权限不足，无法执行此操作"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时，请稍后重试"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务暂时不可用，请稍后重试");

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
