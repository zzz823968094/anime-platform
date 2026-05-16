package com.anime.user.mapper;

import com.anime.user.entity.UserFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏Mapper接口
 * 遵循阿里巴巴开发规范，继承BaseMapper提供基础CRUD能力
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}