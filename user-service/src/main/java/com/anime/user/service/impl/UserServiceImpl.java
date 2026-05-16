package com.anime.user.service.impl;

import com.anime.common.constant.CommonConstant;
import com.anime.common.enums.ResultCodeEnum;
import com.anime.common.exception.BusinessException;
import com.anime.common.utils.BeanCopyUtil;
import com.anime.common.utils.JwtUtils;
import com.anime.user.entity.User;
import com.anime.user.entity.vo.UserInfoVO;
import com.anime.user.mapper.UserMapper;
import com.anime.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 用户服务实现类
 * 遵循阿里巴巴开发规范，业务逻辑下沉，事务控制，常量使用
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String register(String username, String password) {
        log.info("开始注册用户，username: {}", username);

        // 业务前置校验：检查用户名是否已存在
        if (userNameIsExit(username)) {
            log.warn("用户名已存在，username: {}", username);
            throw new BusinessException(ResultCodeEnum.USERNAME_EXISTS);
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(CommonConstant.ZERO);
        user.setStatus(CommonConstant.ZERO);
        user.setPoints(10);

        // 插入数据库
        baseMapper.insert(user);
        log.info("用户注册成功，userId: {}, username: {}", user.getId(), username);

        // 生成Token
        return JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public String login(String username, String password) {
        log.info("开始用户登录，username: {}", username);

        // 查询用户
        User user = baseMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        // 业务前置校验：用户不存在或密码错误
        if (Objects.isNull(user) || !passwordEncoder.matches(password, user.getPassword())) {
            log.warn("用户名或密码错误，username: {}", username);
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        // 业务前置校验：账号状态
        if (CommonConstant.ONE == user.getStatus()) {
            log.warn("账号已被封禁，username: {}", username);
            throw new BusinessException(ResultCodeEnum.FORBIDDEN.getCode(), "账号已被封禁");
        }

        log.info("用户登录成功，userId: {}, username: {}", user.getId(), username);
        return JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public Boolean userNameIsExit(String username) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        return count > 0;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        log.info("获取用户信息，userId: {}", userId);

        // 查询用户
        User user = baseMapper.selectById(userId);
        if (Objects.isNull(user)) {
            log.warn("用户不存在，userId: {}", userId);
            throw new BusinessException(ResultCodeEnum.DATA_NOT_FOUND);
        }

        // Bean拷贝，排除密码字段
        UserInfoVO userInfoVO = BeanCopyUtil.copyObject(user, UserInfoVO.class);
        log.info("获取用户信息成功，userId: {}", userId);
        return userInfoVO;
    }
}