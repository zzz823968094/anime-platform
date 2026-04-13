package com.anime.admin.controller;

import com.anime.admin.entity.AdminUser;
import com.anime.admin.service.AdminUserService;
import com.anime.common.enums.UserStatusEnum;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 分页查询管理员列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        
        // 排除当前登录的管理员自己
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                Integer currentUserId = Integer.valueOf(userIdHeader);
                queryWrapper.ne(AdminUser::getId, currentUserId);
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        if (name != null && !name.isBlank()) {
            queryWrapper.like(AdminUser::getName, name);
        }
        if (status != null && !status.isBlank()) {
            queryWrapper.eq(AdminUser::getStatus, status);
        }

        queryWrapper.orderByDesc(AdminUser::getCreateTime);

        Page<AdminUser> page = new Page<>(pageNum, pageSize);
        adminUserService.page(page, queryWrapper);

        // 清除密码信息
        page.getRecords().forEach(user -> user.setPassword(null));

        return Result.ok(page);
    }

    /**
     * 获取管理员详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        AdminUser admin = adminUserService.getById(id);
        if (admin == null) {
            return Result.fail(404, "管理员不存在");
        }
        admin.setPassword(null);
        return Result.ok(admin);
    }

    /**
     * 创建管理员
     */
    @PostMapping
    public Result<?> create(@RequestBody AdminUser adminUser) {
        AdminUser created = adminUserService.createAdmin(adminUser);
        return Result.ok(created);
    }

    /**
     * 更新管理员
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody AdminUser adminUser) {
        AdminUser updated = adminUserService.updateAdmin(id, adminUser);
        return Result.ok(updated);
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        adminUserService.deleteAdmin(id);
        return Result.ok();
    }

    /**
     * 启用/禁用管理员
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Integer id, @RequestBody UserStatusEnum status) {
        AdminUser update = new AdminUser();
        update.setStatus(status);
        AdminUser updated = adminUserService.updateAdmin(id, update);
        return Result.ok(updated);
    }
}
