package com.anime.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static Result<?> ok() {
        return ok(null);
    }

    public static Result<?> fail(int code, String message) {
        Result<?> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static Result<?> fail(String message) {
        return fail(500, message);
    }
}