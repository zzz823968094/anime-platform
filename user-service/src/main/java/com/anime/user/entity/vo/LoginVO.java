package com.anime.user.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应 VO
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String accessToken;
}
