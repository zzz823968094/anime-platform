package com.anime.user.controller;

import com.anime.common.constant.CommonConstant;
import com.anime.common.exception.BusinessException;
import com.anime.common.result.Result;
import com.anime.user.entity.dto.UserLoginDTO;
import com.anime.user.entity.dto.UserRegisterDTO;
import com.anime.user.entity.vo.LoginVO;
import com.anime.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
     * @param request     HTTP请求
     * @return 注册结果，包含access_token
     */
    @PostMapping("/register")
    public Result<Map<String, String>> register(@Validated @RequestBody UserRegisterDTO registerDTO,
                                                HttpServletRequest request) {
        String ip = getClientIp(request);
        log.info("用户注册请求，IP: {}, username: {}", ip, registerDTO.getUsername());

        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        // 执行注册
        String token;
        try {
            token = userService.register(username, password);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        }


        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(token);
        Map<String, String> map = new HashMap<>();
        map.put("access_token", token);
        log.info("用户注册成功，username: {}", username);
        return Result.ok(map);
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
    public Result<Map<String, String>> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录请求，username: {}", loginDTO.getUsername());
        String token = userService.login(loginDTO.getUsername().trim(), loginDTO.getPassword());
//        LoginVO loginVO = new LoginVO();
//        loginVO.setAccessToken(token);
        Map<String, String> map = new HashMap<>();
        map.put("access_token", token);
        log.info("用户登录成功，username: {}", loginDTO.getUsername());
        return Result.ok(map);
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