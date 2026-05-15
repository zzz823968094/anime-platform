package com.anime.common.result;

import com.anime.common.constant.CommonConstant;
import com.anime.common.enums.ResultCodeEnum;
import lombok.Data;

@Data
public class Result {
    private Integer code;
    private String message;
    private Object data;
    private Long timestamp;

    public static Result ok(Object data) {
        Result r = new Result();
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static Result ok() {
        return ok(ResultCodeEnum.SUCCESS);
    }

    public static Result fail(ResultCodeEnum resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }

    public static Result fail(int code, String message) {
        Result r = new Result();
        r.setCode(code);
        r.setMessage(message);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static Result fail(String message) {
        return fail(CommonConstant.HTTP_STATUS_SERVER_ERROR, message);
    }
}