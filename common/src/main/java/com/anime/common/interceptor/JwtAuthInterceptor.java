package com.anime.common.interceptor;

import com.anime.common.constant.RedisConstant;
import com.anime.common.constant.SystemConstant;
import com.anime.common.enums.ResultCodeEnum;
import com.anime.common.exception.AuthException;
import com.anime.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * JWT认证拦截器
 * 遵循阿里巴巴开发规范，统一处理Token校验、刷新
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取Token
        String token = request.getHeader(SystemConstant.HEADER_AUTHORIZATION);
        
        if (Objects.isNull(token) || !token.startsWith(SystemConstant.TOKEN_PREFIX)) {
            log.warn("请求未携带Token，IP: {}", getClientIp(request));
            throw new AuthException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 去除Bearer前缀
        token = token.substring(SystemConstant.TOKEN_PREFIX.length());

        try {
            // 验证Token有效性
            Long userId = JwtUtils.getUserIdFromToken(token);
            String username = JwtUtils.getUsernameFromToken(token);

            // 检查Redis中是否存在该Token（防止Token被注销后仍可使用）
            String redisKey = String.format(RedisConstant.USER_TOKEN_KEY, userId);
            String cachedToken = redisTemplate.opsForValue().get(redisKey);
            
            if (Objects.isNull(cachedToken) || !cachedToken.equals(token)) {
                log.warn("Token已失效，userId: {}", userId);
                throw new AuthException(ResultCodeEnum.TOKEN_INVALID);
            }

            // Token续期：如果Token剩余时间小于1天，则刷新
            long expiration = JwtUtils.getExpirationFromToken(token);
            long currentTimeMillis = System.currentTimeMillis();
            long oneDayMillis = 24 * 60 * 60 * 1000L;
            
            if (expiration - currentTimeMillis < oneDayMillis) {
                // 刷新Token过期时间
                redisTemplate.expire(redisKey, RedisConstant.TOKEN_EXPIRE, TimeUnit.SECONDS);
                log.debug("Token续期成功，userId: {}", userId);
            }

            // 将用户信息存入请求属性，供Controller使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            log.debug("Token验证通过，userId: {}, username: {}", userId, username);
            return true;

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token验证失败", e);
            throw new AuthException(ResultCodeEnum.TOKEN_EXPIRED);
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
