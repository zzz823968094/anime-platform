package com.anime.crawler.service;

import com.anime.crawler.entity.CronTask;
import com.anime.crawler.entity.CronTaskLog;
import com.anime.crawler.mapper.CronTaskLogMapper;
import com.anime.crawler.mapper.CronTaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CronTaskService {

    private final CronTaskMapper taskMapper;
    private final CronTaskLogMapper taskLogMapper;
    private final CrawlerService crawlerService;
    
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[定时任务] 初始化，加载所有启用的任务");
        List<CronTask> enabledTasks = taskMapper.selectList(
                new LambdaQueryWrapper<CronTask>().eq(CronTask::getEnabled, true)
        );
        for (CronTask task : enabledTasks) {
            scheduleTask(task);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("[定时任务] 关闭所有定时任务");
        for (ScheduledFuture<?> future : scheduledTasks.values()) {
            future.cancel(false);
        }
        scheduledTasks.clear();
    }

    /**
     * 获取所有任务列表
     */
    public List<CronTask> getAllTasks() {
        return taskMapper.selectList(null);
    }

    /**
     * 获取所有任务执行记录
     */
    public Page<CronTaskLog> getTaskLogs(int pageNum, int pageSize, Long taskId) {
        Page<CronTaskLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CronTaskLog> wrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            wrapper.eq(CronTaskLog::getTaskId, taskId);
        }
        wrapper.orderByDesc(CronTaskLog::getStartTime);
        return taskLogMapper.selectPage(page, wrapper);
    }

    /**
     * 获取单个任务
     */
    public CronTask getTaskById(Long id) {
        return taskMapper.selectById(id);
    }

    /**
     * 创建任务
     */
    @Transactional
    public CronTask createTask(CronTask task) {
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);
        
        // 如果启用，则调度
        if (Boolean.TRUE.equals(task.getEnabled())) {
            scheduleTask(task);
        }
        
        log.info("[定时任务] 创建任务: {}", task.getTaskName());
        return task;
    }

    /**
     * 更新任务
     */
    @Transactional
    public CronTask updateTask(Long id, CronTask task) {
        CronTask existing = taskMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 停止旧任务
        if (scheduledTasks.containsKey(id)) {
            stopTask(id);
        }
        
        existing.setTaskName(task.getTaskName());
        existing.setTaskType(task.getTaskType());
        existing.setCronExpression(task.getCronExpression());
        existing.setPages(task.getPages());
        existing.setEnabled(task.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        taskMapper.updateById(existing);
        
        // 如果启用，重新调度
        if (Boolean.TRUE.equals(existing.getEnabled())) {
            scheduleTask(existing);
        }
        
        log.info("[定时任务] 更新任务: {}", existing.getTaskName());
        return existing;
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long id) {
        CronTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 停止调度
        stopTask(id);
        
        taskMapper.deleteById(id);
        log.info("[定时任务] 删除任务: {}", task.getTaskName());
    }

    /**
     * 启用/禁用任务
     */
    @Transactional
    public void toggleTaskEnabled(Long id, Boolean enabled) {
        CronTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        task.setEnabled(enabled);
        task.setUpdatedAt(LocalDateTime.now());
        
        if (Boolean.TRUE.equals(enabled)) {
            scheduleTask(task);
        } else {
            stopTask(id);
        }
        
        taskMapper.updateById(task);
        log.info("[定时任务] {}任务: {}", enabled ? "启用" : "禁用", task.getTaskName());
    }

    /**
     * 立即执行任务
     */
    public void executeTaskNow(Long id) {
        CronTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 异步执行
        new Thread(() -> executeTask(task, false)).start();
        log.info("[定时任务] 手动执行任务: {}", task.getTaskName());
    }

    /**
     * 取消运行中的任务
     */
    public void cancelTask(Long id) {
        ScheduledFuture<?> future = scheduledTasks.get(id);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            log.info("[定时任务] 取消任务: {}", id);
        }
    }

    /**
     * 调度任务
     */
    private void scheduleTask(CronTask task) {
        if (scheduledTasks.containsKey(task.getId())) {
            scheduledTasks.get(task.getId()).cancel(false);
        }
        
        CronTrigger cronTrigger = new CronTrigger(task.getCronExpression());
        
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            try {
                log.info("[定时任务] 开始执行: {}", task.getTaskName());
                task.setLastExecuteTime(LocalDateTime.now());
                task.setStatus("RUNNING");
                taskMapper.updateById(task);
                
                executeTask(task, true);
                
                // 更新下次执行时间
                Date nextTime = cronTrigger.nextExecutionTime(new TriggerContext() {
                    @Override
                    public Instant lastScheduledExecution() {
                        return Instant.now();
                    }
                    @Override
                    public Instant lastActualExecution() {
                        return Instant.now();
                    }
                    @Override
                    public Instant lastCompletion() {
                        return Instant.now();
                    }
                });
                if (nextTime != null) {
                    task.setNextExecuteTime(nextTime.toInstant().atZone(
                            java.time.ZoneId.systemDefault()).toLocalDateTime());
                    taskMapper.updateById(task);
                }
                
            } catch (Exception e) {
                log.error("[定时任务] 执行失败: {}", task.getTaskName(), e);
                task.setStatus("FAILED");
                taskMapper.updateById(task);
            }
        }, cronTrigger);
        
        scheduledTasks.put(task.getId(), future);
        log.info("[定时任务] 已调度任务: {}, Cron: {}", task.getTaskName(), task.getCronExpression());
    }

    /**
     * 停止任务
     */
    private void stopTask(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
            log.info("[定时任务] 已停止任务: {}", taskId);
        }
    }

    /**
     * 执行任务核心逻辑
     */
    private void executeTask(CronTask task, boolean isScheduled) {
        CronTaskLog taskLog = new CronTaskLog();
        taskLog.setTaskId(task.getId());
        taskLog.setTaskName(task.getTaskName());
        taskLog.setTaskType(task.getTaskType());
        taskLog.setStartTime(LocalDateTime.now());
        taskLog.setStatus("RUNNING");
        taskLogMapper.insert(taskLog);
        
        try {
            long startTime = System.currentTimeMillis();
            int pagesCrawled = 0;
            
            // 根据任务类型执行对应的爬取方法
            if (task.getId() == -1L) {
                // 快速同步，直接执行
                crawlerService.crawlType(task.getTaskType(), task.getPages());
                pagesCrawled = task.getPages();
            } else {
                // 定时任务执行
                crawlerService.crawlType(task.getTaskType(), task.getPages());
                pagesCrawled = task.getPages();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 更新日志
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setDuration(duration);
            taskLog.setStatus("SUCCESS");
            taskLog.setPagesCrawled(pagesCrawled);
            taskLog.setMessage("执行成功，爬取" + pagesCrawled + "页");
            taskLogMapper.updateById(taskLog);
            
            // 更新任务状态
            CronTask currentTask = taskMapper.selectById(task.getId());
            if (currentTask != null) {
                currentTask.setStatus("COMPLETED");
                currentTask.setLastExecuteTime(LocalDateTime.now());
                taskMapper.updateById(currentTask);
            }
            
            log.info("[定时任务] 任务执行成功: {}, 耗时: {}ms, 爬取: {}页", 
                    task.getTaskName(), duration, pagesCrawled);
            
        } catch (Exception e) {
            log.error("[定时任务] 任务执行失败: {}", task.getTaskName(), e);
            
            // 更新日志
            taskLog.setEndTime(LocalDateTime.now());
            long duration = System.currentTimeMillis() - 
                    taskLog.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            taskLog.setDuration(duration);
            taskLog.setStatus("FAILED");
            taskLog.setMessage("执行失败: " + e.getMessage());
            taskLogMapper.updateById(taskLog);
            
            // 更新任务状态
            CronTask currentTask = taskMapper.selectById(task.getId());
            if (currentTask != null) {
                currentTask.setStatus("FAILED");
                taskMapper.updateById(currentTask);
            }
        }
    }

    /**
     * 快速同步（不创建任务，直接执行）
     */
    public void quickSync(Integer taskType, Integer pages) {
        CronTask tempTask = new CronTask();
        tempTask.setId(-1L);
        tempTask.setTaskName("快速同步");
        tempTask.setTaskType(taskType);
        tempTask.setPages(pages);
        
        String typeName = taskType == 25 ? "日本动漫" : taskType == 26 ? "欧美动漫" : "中国动漫";
        tempTask.setTaskName("快速同步-" + typeName);
        
        new Thread(() -> executeTask(tempTask, false)).start();
    }
}
