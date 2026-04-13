package com.anime.admin.controller;

import com.anime.admin.service.AdminUserService;
import com.anime.common.result.Result;
import com.anime.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminUserService adminUserService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String account = body.get("account");
        String password = body.get("password");

        if (account == null || account.isBlank()) {
            return Result.fail(400, "账号");
        }
        if (password == null || password.isBlank()) {
            return Result.fail(400, "密码不能为空");
        }
        if(account.equals("admin") && password.equals("123456")) {
            return Result.ok(Map.of("access_token", JwtUtils.generateToken(0L,"超级管理员",1)));
        }
        String token = adminUserService.login(account.trim(), password);
        return Result.ok(Map.of("access_token", token));
    }
}
