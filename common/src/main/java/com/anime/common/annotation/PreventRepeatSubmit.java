package com.anime.common.annotation;

import java.lang.annotation.*;

/**
 * 防重提交注解
 * 遵循阿里巴巴开发规范，防止接口重复提交
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventRepeatSubmit {

    /**
     * 锁定时间（秒），默认5秒
     *
     * @return 锁定时间
     */
    int lockTime() default 5;

    /**
     * 提示信息
     *
     * @return 提示信息
     */
    String message() default "请勿重复提交";
}
