package com.anime.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private Integer role;
    private Integer status;
    private Integer points;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}