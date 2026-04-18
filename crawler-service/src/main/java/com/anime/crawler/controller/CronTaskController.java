package com.anime.crawler.controller;

import cn.hutool.cron.CronUtil;
import com.anime.common.result.Result;
import com.anime.crawler.entity.CronTask;
import com.anime.crawler.entity.CronTaskLog;
import com.anime.crawler.service.CronTaskService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/crawler/tasks")
@RequiredArgsConstructor
public class CronTaskController {

    private final CronTaskService cronTaskService;

    /**
     * 获取所有定时任务列表
     */
    @GetMapping("/list")
    public Result<?> getTaskList() {
        List<CronTask> tasks = cronTaskService.getAllTasks();
        return Result.ok(tasks);
    }

    /**
     * 获取定时任务详情
     */
    @GetMapping("/{id}")
    public Result<?> getTaskDetail(@PathVariable("id") Long id) {
        CronTask task = cronTaskService.getTaskById(id);
        if (task == null) {
            return Result.fail("任务不存在");
        }
        return Result.ok(task);
    }

    /**
     * 获取任务执行记录
     */
    @GetMapping("/logs")
    public Result<?> getTaskLogs(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        Page<CronTaskLog> page = cronTaskService.getTaskLogs(pageNum, pageSize, taskId);
        return Result.ok(page);
    }

    /**
     * 创建定时任务
     */
    @PostMapping
    public Result<?> createTask(@RequestBody CronTask task) {
        CronTask created = cronTaskService.createTask(task);
        return Result.ok(created);
    }

    /**
     * 更新定时任务
     */
    @PutMapping("/{id}")
    public Result<?> updateTask(@PathVariable("id") Long id, @RequestBody CronTask task) {
        CronTask updated = cronTaskService.updateTask(id, task);
        return Result.ok(updated);
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteTask(@PathVariable("id") Long id) {
        cronTaskService.deleteTask(id);
        return Result.ok("删除成功");
    }

    /**
     * 启用/禁用定时任务
     */
    @PutMapping("/{id}/toggle")
    public Result<?> toggleTaskEnabled(@PathVariable("id") Long id, @RequestParam(value = "enabled") Boolean enabled) {
        cronTaskService.toggleTaskEnabled(id, enabled);
        return Result.ok(enabled ? "已启用" : "已禁用");
    }

    /**
     * 立即执行任务
     */
    @PostMapping("/{id}/execute")
    public Result<?> executeTask(@PathVariable("id") Long id) {
        cronTaskService.executeTaskNow(id);
        return Result.ok("任务已启动执行");
    }

    /**
     * 取消运行中的任务
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancelTask(@PathVariable("id") Long id) {
        cronTaskService.cancelTask(id);
        return Result.ok("任务已取消");
    }

    /**
     * 快速同步（不创建任务，直接执行）
     */
    @PostMapping("/sync/{type}")
    public Result<?> quickSync(@PathVariable("type") int type) {
        if (type != 25 && type != 26 && type != 24) {
            return Result.fail("不支持的分类 type，只允许 25/26/24");
        }

        cronTaskService.quickSync(type);
        String typeName = type == 25 ? "日本动漫" : type == 26 ? "欧美动漫" : "中国动漫";
        return Result.ok("已启动：" + typeName + " 快速同步");
    }
}
