package com.anime.admin.entity;

import com.anime.common.enums.UserStatusEnum;
import com.baomidou.mybatisplus.annotation.*;
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
