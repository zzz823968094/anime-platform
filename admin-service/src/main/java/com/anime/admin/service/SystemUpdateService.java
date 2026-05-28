package com.anime.admin.service;

import com.anime.common.constant.CommonConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 系统更新状态服务（基于Redis）
 * 用于管理系统的维护模式状态
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
     * 设置系统更新状态
     *
     * @param updating 是否正在更新
     */
    public void setUpdating(boolean updating) {
        try {
            if (updating) {
                // 设置为维护状态，并设置过期时间防止永久锁定
                stringRedisTemplate.opsForValue().set(
                        CommonConstant.REDIS_KEY_SYSTEM_UPDATE_STATUS,
                        "true",
                        CommonConstant.SYSTEM_MAINTENANCE_DEFAULT_EXPIRE_HOURS,
                        TimeUnit.HOURS
                );
                log.info("系统已设置为维护模式（{}小时后自动恢复）", CommonConstant.SYSTEM_MAINTENANCE_DEFAULT_EXPIRE_HOURS);
            } else {
                // 删除键，恢复正常状态
                stringRedisTemplate.delete(CommonConstant.REDIS_KEY_SYSTEM_UPDATE_STATUS);
                log.info("系统已恢复正常模式");
            }
        } catch (Exception e) {
            log.error("设置系统更新状态失败", e);
            throw new RuntimeException("设置系统更新状态失败", e);
        }
    }

    /**
     * 切换系统更新状态
     *
     * @return 切换后的状态
     */
    public boolean toggleUpdating() {
        boolean currentState = isUpdating();
        boolean newState = !currentState;
        setUpdating(newState);
        return newState;
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

    /**
     * 设置更新提示信息
     *
     * @param message 提示信息
     */
    public void setUpdateMessage(String message) {
        try {
            if (message != null && !message.isEmpty()) {
                stringRedisTemplate.opsForValue().set(
                        CommonConstant.REDIS_KEY_SYSTEM_UPDATE_MESSAGE,
                        message,
                        CommonConstant.SYSTEM_MAINTENANCE_DEFAULT_EXPIRE_HOURS,
                        TimeUnit.HOURS
                );
                log.info("更新提示信息已设置");
            }
        } catch (Exception e) {
            log.error("设置更新提示信息失败", e);
            throw new RuntimeException("设置更新提示信息失败", e);
        }
    }

    /**
     * 获取完整的系统更新状态信息
     *
     * @return 包含状态和消息的对象
     */
    public UpdateStatusInfo getStatusInfo() {
        UpdateStatusInfo info = new UpdateStatusInfo();
        info.setUpdating(isUpdating());
        info.setMessage(getUpdateMessage());
        return info;
    }

    /**
     * 系统更新状态信息类
     */
    public static class UpdateStatusInfo {
        private boolean updating;
        private String message;

        public boolean isUpdating() {
            return updating;
        }

        public void setUpdating(boolean updating) {
            this.updating = updating;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
