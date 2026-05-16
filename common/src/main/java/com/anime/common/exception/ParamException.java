package com.anime.common.exception;

import com.anime.common.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 参数校验异常类
 * 用于处理参数校验失败的异常情况
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Getter
public class ParamException extends RuntimeException {

    private final Integer code;
    private final String message;

    public ParamException(String message) {
        super(message);
        this.code = ResultCodeEnum.PARAM_ERROR.getCode();
        this.message = message;
    }

    public ParamException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ParamException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
