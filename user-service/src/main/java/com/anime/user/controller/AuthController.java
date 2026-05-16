package com.anime.user.controller;

import com.anime.common.constant.CommonConstant;
import com.anime.common.constant.RedisConstant;
import com.anime.common.enums.ResultCodeEnum;
import com.anime.common.result.Result;
import com.anime.user.entity.dto.UserLoginDTO;
import com.anime.user.entity.dto.UserRegisterDTO;
import com.anime.user.entity.vo.LoginVO;
import com.anime.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 提供用户注册、登录等认证相关接口
 * 遵循阿里巴巴开发规范，统一参数校验、返回格式、注释规范
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取个人信息
     *
     * @param userId 用户ID（从请求头获取）
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<com.anime.user.entity.vo.UserInfoVO> getInfo(@RequestHeader("X-User-Id") Long userId) {
        log.info("获取用户信息，userId: {}", userId);
        com.anime.user.entity.vo.UserInfoVO userInfoVO = userService.getUserInfo(userId);
        return Result.ok(userInfoVO);
    }

    /**
     * 用户注册
     * 功能：注册用户并返回Token
     * 入参：UserRegisterDTO（用户名、密码）
     * 出参：LoginVO（access_token）
     *
     * @param registerDTO 注册请求参数
     * @param request HTTP请求
     * @return 注册结果，包含access_token
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Validated @RequestBody UserRegisterDTO registerDTO,
                                    HttpServletRequest request) {
        String ip = getClientIp(request);
        log.info("用户注册请求，IP: {}, username: {}", ip, registerDTO.getUsername());

        // IP注册频率限制
        String redisKey = String.format(RedisConstant.IP_REGISTER_COUNT_KEY, ip);
        String countStr = redisTemplate.opsForValue().get(redisKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= CommonConstant.MAX_REGISTER_PER_IP_PER_DAY) {
            log.warn("IP注册次数超限，IP: {}", ip);
            return Result.fail(ResultCodeEnum.TOO_MANY_REQUESTS);
        }

        // 执行注册
        String token = userService.register(registerDTO.getUsername(), registerDTO.getPassword());

        // 注册成功，IP计数+1，24小时过期
        if (countStr == null) {
            redisTemplate.opsForValue().set(redisKey, "1", RedisConstant.IP_REGISTER_EXPIRE, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().increment(redisKey);
        }

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(token);
        log.info("用户注册成功，username: {}", registerDTO.getUsername());
        return Result.ok(loginVO);
    }

    /**
     * 用户登录
     * 功能：用户登录并返回Token
     * 入参：UserLoginDTO（用户名、密码）
     * 出参：LoginVO（access_token）
     *
     * @param loginDTO 登录请求参数
     * @return 登录结果，包含access_token
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录请求，username: {}", loginDTO.getUsername());
        String token = userService.login(loginDTO.getUsername().trim(), loginDTO.getPassword());
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(token);
        log.info("用户登录成功，username: {}", loginDTO.getUsername());
        return Result.ok(loginVO);
    }

    /**
     * 获取真实客户端IP
     * 遵循阿里巴巴开发规范，优先从代理头中获取真实IP
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(CommonConstant.HEADER_X_FORWARDED_FOR);
        if (ip != null && !ip.isBlank()) {
            return ip.split(CommonConstant.IP_ADDRESS_SEPARATOR)[0].trim();
        }
        ip = request.getHeader(CommonConstant.HEADER_X_REAL_IP);
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}