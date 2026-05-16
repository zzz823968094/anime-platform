package com.anime.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 遵循阿里巴巴开发规范，统一配置线程池参数
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 20;

    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 200;

    /**
     * 线程空闲时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "anime-thread-";

    /**
     * 创建业务线程池
     * 用于处理一般业务逻辑
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean("businessTaskExecutor")
    public ThreadPoolTaskExecutor businessTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        
        // 最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        
        // 队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        
        // 线程名称前缀
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        
        // 拒绝策略：由调用线程处理（CallerRunsPolicy）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();
        
        log.info("业务线程池初始化完成，核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
                CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
        
        return executor;
    }

    /**
     * 创建异步线程池
     * 用于处理异步任务
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean("asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(30);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("anime-async-");
        
        // 拒绝策略：丢弃最老的任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(30);
        
        // 初始化
        executor.initialize();
        
        log.info("异步线程池初始化完成");
        
        return executor;
    }
}
