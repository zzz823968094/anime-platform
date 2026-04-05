package com.anime.user.service.impl;

import com.anime.common.exception.BusinessException;
import com.anime.common.utils.JwtUtils;
import com.anime.user.entity.User;
import com.anime.user.mapper.UserMapper;
import com.anime.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String register(String username, String password) {
        // 检查用户名是否已存在
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(0);
        user.setStatus(0);
        user.setPoints(10);
        baseMapper.insert(user);

        return JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public String login(String username, String password) {
        User user = baseMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException(403, "账号已被封禁");
        }

        return JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public Boolean userNameIsExit(String username) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return count > 0;
    }
}