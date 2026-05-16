package com.anime.common.exception;

import com.anime.common.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 认证授权异常类
 * 用于处理登录、Token、权限相关的异常情况
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Getter
public class AuthException extends RuntimeException {

    private final Integer code;
    private final String message;

    public AuthException(String message) {
        super(message);
        this.code = ResultCodeEnum.UNAUTHORIZED.getCode();
        this.message = message;
    }

    public AuthException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AuthException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
