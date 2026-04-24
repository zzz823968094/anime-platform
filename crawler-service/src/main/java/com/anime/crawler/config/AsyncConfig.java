package com.anime.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 用于管理爬虫相关的异步任务执行
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 爬虫任务线程池
     * 核心参数说明:
     * - corePoolSize: 核心线程数,保持活跃的线程数量
     * - maxPoolSize: 最大线程数,高峰期允许的最大并发线程数
     * - queueCapacity: 队列容量,超过核心线程数后的任务放入队列等待
     * - keepAliveSeconds: 非核心线程的空闲存活时间
     */
    @Bean("crawlerTaskExecutor")
    public Executor crawlerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数: 根据CPU核心数设置,IO密集型任务可以适当增加
        executor.setCorePoolSize(5);
        
        // 最大线程数: 核心线程数的2倍
        executor.setMaxPoolSize(10);
        
        // 队列容量: 缓冲等待执行的任务
        executor.setQueueCapacity(100);
        
        // 线程名称前缀,便于日志追踪
        executor.setThreadNamePrefix("crawler-task-");
        
        // 非核心线程空闲存活时间(秒)
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略: 当队列满时,由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间(秒)
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
