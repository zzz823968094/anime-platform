package com.anime.user.service;

import com.anime.user.entity.User;
import com.anime.user.entity.vo.UserInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 * 遵循阿里巴巴开发规范，定义用户相关业务方法
 *
 * @author anime-platform
 * @date 2026-05-16
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT Token
     */
    String register(String username, String password);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT Token
     */
    String login(String username, String password);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    Boolean userNameIsExit(String username);

    /**
     * 获取用户信息（不包含密码）
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserInfoVO getUserInfo(Long userId);
}