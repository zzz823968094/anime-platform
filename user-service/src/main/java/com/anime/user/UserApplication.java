package com.anime.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 * 遵循阿里巴巴开发规范，启用服务发现
 *
 * @author anime-platform
 * @date 2026-05-12
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserApplication {
    
    /**
     * 主方法，启动应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}