package com.anime.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Bean拷贝工具类
 * 遵循阿里巴巴开发规范，统一封装Bean拷贝方法
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
public class BeanCopyUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private BeanCopyUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 单个对象拷贝
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        if (Objects.isNull(source) || Objects.isNull(target)) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 单个对象拷贝并返回新对象
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @param <T>         目标类型
     * @return 拷贝后的新对象
     */
    public static <T> T copyObject(Object source, Class<T> targetClass) {
        if (Objects.isNull(source)) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Bean拷贝失败", e);
            throw new RuntimeException("Bean拷贝失败", e);
        }
    }

    /**
     * 列表对象拷贝
     *
     * @param sourceList  源列表
     * @param targetClass 目标类
     * @param <T>         目标类型
     * @return 拷贝后的新列表
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (Objects.isNull(sourceList) || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            T target = copyObject(source, targetClass);
            if (Objects.nonNull(target)) {
                targetList.add(target);
            }
        }
        return targetList;
    }

    /**
     * 单个对象拷贝（使用Supplier创建目标对象）
     *
     * @param source         源对象
     * @param targetSupplier 目标对象供应商
     * @param <T>            目标类型
     * @return 拷贝后的新对象
     */
    public static <T> T copyObject(Object source, Supplier<T> targetSupplier) {
        if (Objects.isNull(source)) {
            return null;
        }
        try {
            T target = targetSupplier.get();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Bean拷贝失败", e);
            throw new RuntimeException("Bean拷贝失败", e);
        }
    }

    /**
     * 列表对象拷贝（使用Supplier创建目标对象）
     *
     * @param sourceList     源列表
     * @param targetSupplier 目标对象供应商
     * @param <T>            目标类型
     * @return 拷贝后的新列表
     */
    public static <T> List<T> copyList(List<?> sourceList, Supplier<T> targetSupplier) {
        if (Objects.isNull(sourceList) || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            T target = copyObject(source, targetSupplier);
            if (Objects.nonNull(target)) {
                targetList.add(target);
            }
        }
        return targetList;
    }
}
