package com.anime.common.utils;

import com.anime.common.result.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Objects;

/**
 * 分页工具类
 * 遵循阿里巴巴开发规范，统一封装分页转换方法
 *
 * @author anime-platform
 * @date 2026-05-16
 */
public class PageUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private PageUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将MyBatis-Plus的IPage转换为PageResult
     *
     * @param page MyBatis-Plus分页对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        if (Objects.isNull(page)) {
            return PageResult.empty(1L, 10L);
        }
        return PageResult.build(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    /**
     * 将MyBatis-Plus的IPage转换为PageResult（带数据转换）
     *
     * @param page     MyBatis-Plus分页对象
     * @param converter 数据转换器
     * @param <T>      源数据类型
     * @param <R>      目标数据类型
     * @return 分页结果
     */
    public static <T, R> PageResult<R> toPageResult(IPage<T> page, java.util.function.Function<T, R> converter) {
        if (Objects.isNull(page)) {
            return PageResult.empty(1L, 10L);
        }
        return PageResult.build(
                page.getRecords().stream().map(converter).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }
}
