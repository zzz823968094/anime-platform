package com.anime.common.exception;

import com.anime.common.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 业务异常类
 * 遵循阿里巴巴开发规范，用于处理业务逻辑中的异常情况
 *
 * @author anime-platform
 * @date 2026-05-12
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
