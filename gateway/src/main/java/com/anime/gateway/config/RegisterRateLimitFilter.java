package com.anime.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 注册接口限流过滤器
 * 遵循阿里巴巴开发规范，防止恶意注册
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Component
public class RegisterRateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;

    public RegisterRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 每个 IP 每分钟最多请求注册接口 5 次
    private static final int MAX_PER_MINUTE = 5;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 只限制注册接口
        if (!path.equals("/api/auth/register")) {
            return chain.filter(exchange);
        }

        String ip = getIp(exchange);
        String key = "gw:register:" + ip;

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // 第一次请求，设置 1 分钟过期
                        log.info("注册限流：首次请求，ip: {}", ip);
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .then(chain.filter(exchange));
                    }
                    if (count > MAX_PER_MINUTE) {
                        log.warn("注册限流：超出限制，ip: {}, count: {}", ip, count);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    log.debug("注册限流：正常请求，ip: {}, count: {}", ip, count);
                    return chain.filter(exchange);
                });
    }

    private String getIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -100; // 优先级高，在其他过滤器之前执行
    }
}