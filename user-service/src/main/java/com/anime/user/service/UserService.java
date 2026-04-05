package com.anime.user.service;

import com.anime.user.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

public interface UserService extends IService<User> {
    String register(String username, String password);
    String login(String username, String password);

    Boolean userNameIsExit(@Param("userName") String username);
}