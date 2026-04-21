package com.anime.admin.controller;

import com.anime.admin.entity.AppVersion;
import com.anime.admin.service.AppVersionService;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/app-versions")
public class AppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status
    ) {
        LambdaQueryWrapper<AppVersion> wrapper = new LambdaQueryWrapper<>();
        if (platform != null && !platform.isBlank()) wrapper.eq(AppVersion::getPlatform, platform);
        if (status != null && !status.isBlank()) wrapper.eq(AppVersion::getStatus, status);
        wrapper.orderByDesc(AppVersion::getVersionCode);
        Page<AppVersion> page = new Page<>(pageNum, pageSize);
        appVersionService.page(page, wrapper);
        return Result.ok(page);
    }

    @GetMapping("/latest")
    public Result<?> latest(@RequestParam(required = false) String platform) {
        return Result.ok(appVersionService.getLatest(platform));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        AppVersion v = appVersionService.getById(id);
        if (v == null) return Result.fail(404, "版本不存在");
        return Result.ok(v);
    }

    @PostMapping
    public Result<?> create(@RequestBody AppVersion appVersion) {
        return Result.ok(appVersionService.create(appVersion));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody AppVersion appVersion) {
        return Result.ok(appVersionService.update(id, appVersion));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        appVersionService.delete(id);
        return Result.ok();
    }

    @PostMapping("/upload")
    public Result<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = appVersionService.uploadFile(file);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail(500, "文件上传失败: " + e.getMessage());
        }
    }
}
