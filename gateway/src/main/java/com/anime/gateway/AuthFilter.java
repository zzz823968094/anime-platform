package com.anime.gateway;

import com.anime.common.utils.JwtUtils;
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

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of(
            "/api/danmaku/*",
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
            "/api/anime/device/stats"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethod().name();

        // OPTIONS 预检请求直接放行（CORS）
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        // 白名单直接放行
        for (String white : WHITE_LIST) {
            if (path.startsWith(white)) {
                return chain.filter(exchange);
            }
        }

        // GET 请求的番剧详情、弹幕列表放行
        if ("GET".equals(method) && (
                path.startsWith("/api/anime/") ||
                        path.startsWith("/api/danmaku/") ||
                        path.startsWith("/api/video/") ||
                        path.startsWith("/ws/"))) {
            return chain.filter(exchange);
        }

        // 取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return unauthorized(exchange);
        }

        // 验证 Token
        boolean valid = JwtUtils.isValid(token);
        if (!valid) {
            return unauthorized(exchange);
        }

        // 解析用户信息，写入请求头传给下游
        Long userId = JwtUtils.getUserId(token);

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

    @Override
    public int getOrder() {
        return -1;
    }
}