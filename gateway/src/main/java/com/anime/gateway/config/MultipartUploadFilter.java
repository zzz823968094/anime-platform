package com.anime.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 文件上传请求处理过滤器
 * 解决Spring Cloud Gateway转发multipart/form-data请求的问题
 * 
 * 关键配置：
 * 1. 识别multipart请求
 * 2. 确保请求头正确传递
 * 3. 不进行任何请求体修改或缓冲
 *
 * @author anime-platform
 * @date 2026-05-17
 */
@Slf4j
@Component
public class MultipartUploadFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 只处理POST/PUT请求且是multipart类型
        if (isMultipartUploadRequest(request)) {
            String path = request.getURI().getPath();
            long contentLength = request.getHeaders().getContentLength();
            
            log.info("========== 文件上传请求 ==========");
            log.info("路径: {}", path);
            log.info("方法: {}", request.getMethod());
            log.info("Content-Type: {}", request.getHeaders().getContentType());
            log.info("Content-Length: {} bytes", contentLength);
            log.info("====================================");
            
            // 重要：不要对请求进行任何修改，直接传递
            // Spring Cloud Gateway 3.x+ 应该能正确处理multipart
            return chain.filter(exchange)
                .doOnSuccess(v -> log.info("文件上传请求处理成功: {}", path))
                .doOnError(e -> log.error("文件上传请求处理失败: {}, 错误: {}", path, e.getMessage()));
        }
        
        return chain.filter(exchange);
    }

    /**
     * 判断是否是文件上传请求
     */
    private boolean isMultipartUploadRequest(ServerHttpRequest request) {
        // 必须是POST或PUT方法
        HttpMethod method = request.getMethod();
        if (method != HttpMethod.POST && method != HttpMethod.PUT) {
            return false;
        }
        
        // 检查Content-Type是否为multipart
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null) {
            return false;
        }
        
        String contentTypeStr = contentType.toString().toLowerCase();
        return contentTypeStr.contains("multipart") || 
               contentTypeStr.contains("form-data");
    }

    @Override
    public int getOrder() {
        // 在高优先级执行，确保在其他过滤器之前处理
        return -100;
    }
}
