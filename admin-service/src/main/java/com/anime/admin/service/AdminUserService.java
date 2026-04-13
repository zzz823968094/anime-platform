package com.anime.admin.service;

import com.anime.admin.entity.AdminUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AdminUserService extends IService<AdminUser> {
    
    /**
     * 管理员登录
     */
    String login(String phone, String password);
    
    /**
     * 创建管理员
     */
    AdminUser createAdmin(AdminUser adminUser);
    
    /**
     * 更新管理员
     */
    AdminUser updateAdmin(Integer id, AdminUser adminUser);
    
    /**
     * 删除管理员
     */
    void deleteAdmin(Integer id);
    
    /**
     * 检查手机号是否存在
     */
    Boolean phoneExists(String phone);
}
