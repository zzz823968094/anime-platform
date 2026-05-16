package com.anime.common.aspect;

import com.anime.common.annotation.PreventRepeatSubmit;
import com.anime.common.constant.RedisConstant;
import com.anime.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 防重提交切面
 * 遵循阿里巴巴开发规范，使用Redis实现分布式锁防止重复提交
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PreventRepeatSubmitAspect {

    private final StringRedisTemplate redisTemplate;

    /**
     * 环绕通知，处理防重提交逻辑
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(preventRepeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, PreventRepeatSubmit preventRepeatSubmit) throws Throwable {
        // 获取请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 生成唯一键：IP + 方法签名
        String ip = getClientIp(request);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String key = String.format(RedisConstant.LOCK_KEY, 
                "repeat_submit:" + ip + ":" + method.getDeclaringClass().getName() + "." + method.getName());

        int lockTime = preventRepeatSubmit.lockTime();
        String message = preventRepeatSubmit.message();

        // 尝试获取锁
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", lockTime, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(success)) {
            log.warn("重复提交拦截，IP: {}, 方法: {}", ip, method.getName());
            throw new BusinessException(message);
        }

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } finally {
            // 不立即删除锁，让其自然过期，确保锁定时间内不能重复提交
            log.debug("防重提交锁将在{}秒后自动过期", lockTime);
        }
    }

    /**
     * 获取客户端IP
     *
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
