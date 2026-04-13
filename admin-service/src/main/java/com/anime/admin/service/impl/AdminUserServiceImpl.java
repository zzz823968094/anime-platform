package com.anime.admin.service.impl;

import com.anime.admin.entity.AdminUser;
import com.anime.admin.mapper.AdminUserMapper;
import com.anime.admin.service.AdminUserService;
import com.anime.common.enums.UserStatusEnum;
import com.anime.common.exception.BusinessException;
import com.anime.common.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String account, String password) {
        AdminUser admin = baseMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getAccount, account)
        );
        if (admin == null ||!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(401, "手机号或密码错误");
        }
        if ("DISABLE".equals(admin.getStatus())) {
            throw new BusinessException(403, "账号已被禁用");
        }
        // role=1 表示管理员
        return JwtUtils.generateToken(admin.getId().longValue(), admin.getName(), 1);
    }

    @Override
    public AdminUser createAdmin(AdminUser adminUser) {
        // 检查手机号是否已存在
        if (phoneExists(adminUser.getPhone())) {
            throw new BusinessException(400, "手机号已存在");
        }
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setStatus(UserStatusEnum.NORMAL);
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(adminUser);
        adminUser.setPassword(null); // 不返回密码
        return adminUser;
    }

    @Override
    public AdminUser updateAdmin(Integer id, AdminUser adminUser) {
        AdminUser existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 如果修改了手机号，检查是否已被使用
        if (adminUser.getPhone() != null && !adminUser.getPhone().equals(existing.getPhone())) {
            if (phoneExists(adminUser.getPhone())) {
                throw new BusinessException(400, "手机号已存在");
            }
            existing.setPhone(adminUser.getPhone());
        }

        // 如果提供了新密码，则更新
        if (adminUser.getPassword() != null && !adminUser.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        }

        if (adminUser.getName() != null) {
            existing.setName(adminUser.getName());
        }
        if (adminUser.getStatus() != null) {
            existing.setStatus(adminUser.getStatus());
        }

        existing.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(existing);
        existing.setPassword(null); // 不返回密码
        return existing;
    }

    @Override
    public void deleteAdmin(Integer id) {
        AdminUser existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public Boolean phoneExists(String phone) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getPhone, phone)
        );
        return count > 0;
    }
}
