package com.anime.admin.controller;

import com.anime.admin.entity.AdminUser;
import com.anime.admin.service.AdminUserService;
import com.anime.common.constant.CommonConstant;
import com.anime.common.enums.UserStatusEnum;
import com.anime.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员管理控制器
 * 遵循阿里巴巴开发规范，统一RESTful风格，参数校验，Result返回
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 分页查询管理员列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param name     姓名（模糊搜索）
     * @param status   状态
     * @param request  HTTP请求
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<AdminUser>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status,
            HttpServletRequest request
    ) {
        log.info("分页查询管理员列表，pageNum: {}, pageSize: {}", pageNum, pageSize);

        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();

        // 排除当前登录的管理员自己
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                Long currentUserId = Long.valueOf(userIdHeader);
                queryWrapper.ne(AdminUser::getId, currentUserId);
            } catch (NumberFormatException e) {
                log.warn("用户ID解析失败，userIdHeader: {}", userIdHeader);
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

        log.info("分页查询管理员列表完成，total: {}", page.getTotal());
        return Result.ok(page);
    }

    /**
     * 获取管理员详情
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/{id}")
    public Result<AdminUser> getById(@PathVariable("id") Long id) {
        log.info("获取管理员详情，id: {}", id);
        AdminUser admin = adminUserService.getById(id);
        if (admin == null) {
            log.warn("管理员不存在，id: {}", id);
            return Result.fail(CommonConstant.HTTP_STATUS_NOT_FOUND, "管理员不存在");
        }
        admin.setPassword(null);
        return Result.ok(admin);
    }

    /**
     * 创建管理员
     *
     * @param adminUser 管理员信息
     * @return 创建结果
     */
    @PostMapping
    public Result<AdminUser> create(@RequestBody AdminUser adminUser) {
        log.info("开始创建管理员，account: {}", adminUser.getAccount());
        AdminUser created = adminUserService.createAdmin(adminUser);
        log.info("管理员创建成功，id: {}", created.getId());
        return Result.ok(created);
    }

    /**
     * 更新管理员
     *
     * @param id        管理员ID
     * @param adminUser 管理员信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<AdminUser> update(@PathVariable("id") Long id, @RequestBody AdminUser adminUser) {
        log.info("开始更新管理员，id: {}", id);
        AdminUser updated = adminUserService.updateAdmin(id, adminUser);
        log.info("管理员更新成功，id: {}", id);
        return Result.ok(updated);
    }

    /**
     * 删除管理员
     *
     * @param id 管理员ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        log.info("开始删除管理员，id: {}", id);
        adminUserService.deleteAdmin(id);
        log.info("管理员删除成功，id: {}", id);
        return Result.ok();
    }

    /**
     * 启用/禁用管理员
     */
    @PutMapping("/{id}/status")
    public Result<AdminUser> updateStatus(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> request) {
        String statusStr = request.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return Result.fail(CommonConstant.HTTP_STATUS_PARAM_ERROR, "状态参数不能为空");
        }

        UserStatusEnum status;
        try {
            status = UserStatusEnum.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return Result.fail(CommonConstant.HTTP_STATUS_PARAM_ERROR, "无效的状态值: " + statusStr);
        }

        AdminUser update = new AdminUser();
        update.setStatus(status);
        AdminUser updated = adminUserService.updateAdmin(id, update);
        return Result.ok(updated);
    }
}
