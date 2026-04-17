package com.anime.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
public class TaskSchedulerConfig implements AsyncConfigurer {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("cron-task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }

    /**
     * 配置@Async异步方法的线程池
     * 避免使用默认的SimpleAsyncTaskExecutor(每次创建新线程)
     * 使用有界线程池控制并发,防止资源耗尽影响其他服务
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数: 根据服务器CPU核心数设置,IO密集型任务可以适当增加
        executor.setCorePoolSize(3);
        // 最大线程数: 控制最大并发度,避免过多线程影响其他服务
        executor.setMaxPoolSize(5);
        // 队列容量: 超出核心线程数的任务进入队列等待
        executor.setQueueCapacity(10);
        // 线程名称前缀,便于日志追踪
        executor.setThreadNamePrefix("async-crawler-");
        // 拒绝策略: 队列满时由调用线程执行(背压机制)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 空闲线程存活时间(秒)
        executor.setKeepAliveSeconds(60);
        // 应用关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
