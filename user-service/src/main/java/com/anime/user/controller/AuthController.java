package com.anime.user.controller;

import com.anime.common.result.Result;
import com.anime.user.entity.User;
import com.anime.user.entity.UserFavorite;
import com.anime.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    // 同一 IP 24小时内最多注册 3 个账号
    private static final int MAX_REGISTER_PER_DAY = 3;

    /** 个人信息 */
    @GetMapping("/api/auth/info")
    public Result<?> info(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(userService.getById(userId));
    }


    @PostMapping("/api/auth/register")
    public Result<?> register(@RequestBody Map<String, String> body,
                              HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");

        // ── 参数校验 ──────────────────────────────────────────
        if (username == null || username.isBlank()) {
            return Result.fail(400, "用户名不能为空");
        }
        username = username.trim();
        if (username.length() < 4 || username.length() > 16) {
            return Result.fail(400, "用户名须为 4-16 位");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return Result.fail(400, "用户名只能包含字母、数字和下划线");
        }
        if (password == null || password.length() < 6) {
            return Result.fail(400, "密码至少 6 位");
        }
        if (password.length() > 64) {
            return Result.fail(400, "密码过长");
        }

        // ── IP 注册频率限制 ────────────────────────────────────
        String ip = getClientIp(request);
        String redisKey = "register:ip:" + ip;
        String countStr = redisTemplate.opsForValue().get(redisKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= MAX_REGISTER_PER_DAY) {
            return Result.fail(429, "今日注册次数已达上限，请明天再试");
        }
        Boolean isExit = userService.userNameIsExit(username);
        if(isExit){
            return Result.fail(430, "用户名已存在");
        }
        // ── 执行注册 ───────────────────────────────────────────
        String token = userService.register(username, password);

        // 注册成功，IP 计数 +1，24小时过期
        if (countStr == null) {
            redisTemplate.opsForValue().set(redisKey, "1", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(redisKey);
        }

        return Result.ok(Map.of("access_token", token));
    }

    @PostMapping("/api/auth/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank()) return Result.fail(400, "用户名不能为空");
        if (password == null || password.isBlank()) return Result.fail(400, "密码不能为空");

        String token = userService.login(username.trim(), password);
        return Result.ok(Map.of("access_token", token));
    }

    @GetMapping("/api/user/me")
    public Result<?> me(@RequestHeader("X-User-Id") Long userId) {
        return Result.ok(userService.getById(userId));
    }

    @GetMapping("/api/user/list")
    public Result<?> list(@RequestHeader(value = "X-User-Role", defaultValue = "0") Integer role) {
        if (role != 1) return Result.fail(403, "无权限");
        List<User> users = userService.list(
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt)
        );
        users.forEach(u -> u.setPassword(null));
        return Result.ok(users);
    }

    @GetMapping("/api/user/count")
    public Result<?> count() {
        return Result.ok(userService.count());
    }

    // ── 工具：获取真实客户端 IP ────────────────────────────────
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}