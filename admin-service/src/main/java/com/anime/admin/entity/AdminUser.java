package com.anime.admin.entity;

import com.anime.common.enums.UserStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_user")
public class AdminUser {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String account;
    
    private String name;
    
    private String phone;
    
    private String password;
    
    private UserStatusEnum status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
