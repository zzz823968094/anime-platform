package com.anime.gateway;

import com.anime.common.constant.CommonConstant;
import com.anime.common.utils.JwtUtils;
import com.anime.gateway.service.SystemUpdateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 认证过滤器
 * 遵循阿里巴巴开发规范，统一JWT验证、维护模式检查
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of(
            "/api/image/forward",
            "/api/danmaku/",
            "/api/user/list",
            "/api/user/count",
            "/api/auth/login",
            "/api/auth/register",
            "/api/anime/list",
            "/api/recommend/hot",
            "/api/recommend/latest",
            "/api/search",
            "/api/crawler",
            "/api/admin/login",
            "/api/anime/carousel/list",
            "/api/admin/app-versions/latest",
            "/api/access/data/init",
            // 广告查询接口（公开访问）
            "/api/ad/position/",
            "/api/ad-position/active",
            // 设备统计接口（管理端访问）
            "/api/anime/device/stats",
            // 系统管理接口（管理端专用，维护模式下也可访问）
            "/api/admin/system/"
    );
    @Resource
    private SystemUpdateService systemUpdateService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethod().name();

        // OPTIONS 预检请求直接放行（CORS）
        if ("OPTIONS".equals(method)) {
            log.debug("OPTIONS预检请求，直接放行，path: {}", path);
            return chain.filter(exchange);
        }

        // 白名单直接放行
        for (String white : WHITE_LIST) {
            if (path.startsWith(white)) {
                log.debug("白名单路径，直接放行，path: {}", path);
                return chain.filter(exchange);
            }
        }

        // 如果系统正在维护，返回 code=999
        if (systemUpdateService.isUpdating()) {
            log.warn("系统维护模式，拦截请求，path: {}", path);
            return maintenanceMode(exchange);
        }

        // GET 请求的番剧详情、弹幕列表放行
        if ("GET".equals(method) && (
                path.startsWith("/api/anime/") ||
                        path.startsWith("/api/danmaku/") ||
                        path.startsWith("/api/video/") ||
                        path.startsWith("/ws/"))) {
            log.debug("GET请求公开资源，直接放行，path: {}", path);
            return chain.filter(exchange);
        }

        // 取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            log.warn("缺少Token，拒绝访问，path: {}", path);
            return unauthorized(exchange);
        }

        // 验证 Token
        boolean valid = JwtUtils.isValid(token);
        if (!valid) {
            log.warn("Token无效，拒绝访问，path: {}", path);
            return unauthorized(exchange);
        }

        // 解析用户信息，写入请求头传给下游
        Long userId = JwtUtils.getUserId(token);
        log.debug("Token验证成功，userId: {}, path: {}", userId, path);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId.toString())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    /**
     * 返回维护模式响应（code=999）
     */
    private Mono<Void> maintenanceMode(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK); // HTTP状态码200，但业务code为999
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String message = systemUpdateService.getUpdateMessage();
        String json = "{\"code\":" + CommonConstant.GATEWAY_MAINTENANCE_CODE + ",\"message\":\"" + message + "\",\"data\":null}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}