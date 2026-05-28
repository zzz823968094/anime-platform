package com.anime.anime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 动漫服务启动类
 * 遵循阿里巴巴开发规范，启用服务发现、Feign客户端和定时任务
 *
 * @author anime-platform
 * @date 2026-05-12
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class AnimeApplication {

    /**
     * 主方法，启动应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AnimeApplication.class, args);
    }
}