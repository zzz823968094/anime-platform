package com.anime.user.mapper;

import com.anime.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 遵循阿里巴巴开发规范，继承BaseMapper提供基础CRUD能力
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}