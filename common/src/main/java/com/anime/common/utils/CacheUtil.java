package com.anime.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存工具类
 * 遵循阿里巴巴开发规范，统一封装Redis缓存操作，支持缓存穿透保护
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Component
public class CacheUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheUtil(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取缓存，如果缓存不存在则从数据库加载并缓存
     * 防止缓存穿透
     *
     * @param key      缓存键
     * @param supplier 数据加载器
     * @param expire   过期时间（秒）
     * @param <T>      数据类型
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> supplier, long expire) {
        // 尝试从缓存获取
        String cached = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(cached)) {
            log.debug("缓存命中，key: {}", key);
            try {
                return (T) objectMapper.readValue(cached, Object.class);
            } catch (JsonProcessingException e) {
                log.error("缓存反序列化失败，key: {}", key, e);
                return null;
            }
        }

        // 缓存未命中，从数据库加载
        log.debug("缓存未命中，从数据库加载，key: {}", key);
        T data = supplier.get();

        // 如果数据不为空，则缓存
        if (Objects.nonNull(data)) {
            try {
                String json = objectMapper.writeValueAsString(data);
                redisTemplate.opsForValue().set(key, json, expire, TimeUnit.SECONDS);
                log.debug("数据已缓存，key: {}, expire: {}s", key, expire);
            } catch (JsonProcessingException e) {
                log.error("缓存序列化失败，key: {}", key, e);
            }
        } else {
            // 防止缓存穿透：缓存空值，设置较短过期时间
            redisTemplate.opsForValue().set(key, "", 60, TimeUnit.SECONDS);
            log.debug("数据为空，缓存空值防止穿透，key: {}", key);
        }

        return data;
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("缓存已删除，key: {}", key);
    }

    /**
     * 批量删除缓存（支持通配符）
     *
     * @param pattern 缓存键模式
     */
    public void deleteByPattern(String pattern) {
        redisTemplate.keys(pattern).forEach(redisTemplate::delete);
        log.debug("批量缓存已删除，pattern: {}", pattern);
    }

    /**
     * 设置缓存
     *
     * @param key    缓存键
     * @param value  缓存值
     * @param expire 过期时间（秒）
     */
    public void set(String key, Object value, long expire) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, expire, TimeUnit.SECONDS);
            log.debug("缓存已设置，key: {}, expire: {}s", key, expire);
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败，key: {}", key, e);
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @param <T> 数据类型
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (Objects.isNull(json) || json.isEmpty()) {
            return null;
        }
        try {
            return (T) objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.error("缓存反序列化失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
