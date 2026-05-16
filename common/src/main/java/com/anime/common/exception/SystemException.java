package com.anime.common.exception;

import com.anime.common.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 系统异常类
 * 用于处理系统级别的异常情况
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Getter
public class SystemException extends RuntimeException {

    private final Integer code;
    private final String message;

    public SystemException(String message) {
        super(message);
        this.code = ResultCodeEnum.ERROR.getCode();
        this.message = message;
    }

    public SystemException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public SystemException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCodeEnum.ERROR.getCode();
        this.message = message;
    }
}
