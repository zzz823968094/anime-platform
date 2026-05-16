package com.anime.user.controller;

import com.anime.common.constant.CommonConstant;
import com.anime.common.result.Result;
import com.anime.user.entity.User;
import com.anime.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 普通用户管理控制器
 * 遵循阿里巴巴开发规范，统一RESTful风格，Result返回
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询普通用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param username 用户名（模糊搜索）
     * @param status   状态
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        log.debug("分页查询普通用户列表，pageNum: {}, pageSize: {}", pageNum, pageSize);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            queryWrapper.like(User::getUsername, username);
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        Page<User> page = new Page<>(pageNum, pageSize);
        userService.page(page, queryWrapper);
        // 清除密码信息，防止敏感数据泄露
        if (page.getRecords() != null) {
            page.getRecords().forEach(user -> {
                if (user != null) {
                    user.setPassword(null);
                }
            });
        }

        log.debug("分页查询普通用户列表完成，total: {}", page.getTotal());
        return Result.ok(page);
    }

    /**
     * 获取普通用户总数
     *
     * @return 用户总数
     */
    @GetMapping("/count")
    public Result<Long> count() {
        log.debug("获取普通用户总数");
        long count = userService.count();
        log.debug("普通用户总数: {}", count);
        return Result.ok(count);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable("id") Long id) {
        log.debug("获取用户详情，id: {}", id);
        
        User user = userService.getById(id);
        if (user == null) {
            log.warn("用户不存在，id: {}", id);
            return Result.fail(CommonConstant.HTTP_STATUS_NOT_FOUND, "用户不存在");
        }
        
        // 清除密码信息，防止敏感数据泄露
        user.setPassword(null);
        
        return Result.ok(user);
    }

    /**
     * 启用/禁用用户
     *
     * @param id     用户ID
     * @param status 状态（0-禁用，1-正常）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam @NotNull(message = "状态不能为空") Integer status
    ) {
        // 校验状态值合法性
        if (status != CommonConstant.USER_STATUS_DISABLED_INT && status != CommonConstant.USER_STATUS_NORMAL_INT) {
            log.warn("无效的用户状态值: {}, id: {}", status, id);
            return Result.fail(CommonConstant.HTTP_STATUS_PARAM_ERROR, "状态值只能为0或1");
        }
        
        log.info("更新用户状态，id: {}, status: {}", id, status);
        
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        
        boolean success = userService.updateById(user);
        if (!success) {
            log.error("更新用户状态失败，id: {}", id);
            return Result.fail("更新失败");
        }
        
        log.info("用户状态更新成功，id: {}", id);
        return Result.ok();
    }
}
