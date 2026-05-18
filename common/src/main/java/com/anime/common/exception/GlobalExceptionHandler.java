package com.anime.common.exception;

import com.anime.common.enums.ResultCodeEnum;
import com.anime.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一拦截异常并返回Result格式
 * 遵循阿里巴巴开发规范，提供统一的异常处理机制
 *
 * @author anime-platform
 * @date 2026-05-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }
    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public Result<?> handleSystemException(SystemException e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（自定义）
     */
    @ExceptionHandler(ParamException.class)
    public Result<?> handleParamException(ParamException e) {
        log.warn("参数异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证授权异常
     */
    @ExceptionHandler(AuthException.class)
    public Result<?> handleAuthException(AuthException e) {
        log.warn("认证授权异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid/@Validated）
     * 返回HTTP 200状态码，业务code为400
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常：{}", message);
        // 返回HTTP 200，但业务code为400
        return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "参数错误: " + message);
    }

    /**
     * 处理绑定异常
     * 返回HTTP 200状态码，业务code为400
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常：{}", message);
        // 返回HTTP 200，但业务code为400
        return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), "参数错误: " + message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = "缺少必要参数: " + e.getParameterName();
        log.warn("缺少请求参数：{}", message);
        return Result.fail(ResultCodeEnum.PARAM_MISSING.getCode(), message);
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源未找到：{}", e.getMessage());
        return Result.fail(ResultCodeEnum.NOT_FOUND.getCode(), ResultCodeEnum.NOT_FOUND.getMessage());
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(java.sql.SQLException.class)
    public Result<?> handleSQLException(java.sql.SQLException e) {
        log.error("SQL异常：{}", e.getMessage(), e);
        return Result.fail(ResultCodeEnum.OPERATION_FAILED.getCode(), "数据库操作失败，请联系管理员");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常：{}", e.getMessage(), e);
        return Result.fail(ResultCodeEnum.ERROR.getCode(), "系统异常，请联系管理员");
    }

    /**
     * 处理所有其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.fail(ResultCodeEnum.ERROR.getCode(), ResultCodeEnum.ERROR.getMessage());
    }
}