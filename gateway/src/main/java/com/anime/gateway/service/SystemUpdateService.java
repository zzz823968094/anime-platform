package com.anime.gateway.service;

import com.anime.common.constant.CommonConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 系统更新状态服务（基于Redis）
 * 用于检查系统的维护模式状态
 */
@Slf4j
@Service
public class SystemUpdateService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取系统是否处于更新状态
     *
     * @return true-维护中，false-正常运行
     */
    public boolean isUpdating() {
        try {
            String status = stringRedisTemplate.opsForValue().get(CommonConstant.REDIS_KEY_SYSTEM_UPDATE_STATUS);
            return "true".equals(status);
        } catch (Exception e) {
            log.error("获取系统更新状态失败", e);
            // 出错时默认返回false，避免影响正常服务
            return false;
        }
    }

    /**
     * 获取更新提示信息
     *
     * @return 提示信息
     */
    public String getUpdateMessage() {
        try {
            String message = stringRedisTemplate.opsForValue().get(CommonConstant.REDIS_KEY_SYSTEM_UPDATE_MESSAGE);
            return message != null ? message : CommonConstant.SYSTEM_MAINTENANCE_DEFAULT_MESSAGE;
        } catch (Exception e) {
            log.error("获取更新提示信息失败", e);
            return CommonConstant.SYSTEM_MAINTENANCE_DEFAULT_MESSAGE;
        }
    }
}
