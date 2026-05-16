package com.anime.admin.service.impl;

import com.anime.admin.entity.AdminUser;
import com.anime.admin.mapper.AdminUserMapper;
import com.anime.admin.service.AdminUserService;
import com.anime.common.constant.CommonConstant;
import com.anime.common.enums.UserStatusEnum;
import com.anime.common.exception.BusinessException;
import com.anime.common.enums.ResultCodeEnum;
import com.anime.common.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 管理员服务实现类
 * 遵循阿里巴巴开发规范，业务逻辑下沉，事务控制
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String account, String password) {
        log.info("管理员登录，account: {}", account);
        
        AdminUser admin = baseMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getAccount, account)
        );
        if (Objects.isNull(admin) || !passwordEncoder.matches(password, admin.getPassword())) {
            log.warn("管理员登录失败，账号或密码错误，account: {}", account);
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }
        if (CommonConstant.USER_STATUS_DISABLED.equals(admin.getStatus())) {
            log.warn("管理员账号已被禁用，account: {}", account);
            throw new BusinessException(ResultCodeEnum.FORBIDDEN);
        }
        
        String token = JwtUtils.generateToken(admin.getId(), admin.getName(), CommonConstant.ADMIN_ROLE_ID);
        log.info("管理员登录成功，account: {}", account);
        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUser createAdmin(AdminUser adminUser) {
        log.info("开始创建管理员，account: {}, phone: {}", adminUser.getAccount(), adminUser.getPhone());
        
        // 检查手机号是否已存在
        if (phoneExists(adminUser.getPhone())) {
            log.warn("手机号已存在，phone: {}", adminUser.getPhone());
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "手机号已存在");
        }
        
        // 设置默认密码和状态
        adminUser.setPassword(passwordEncoder.encode(CommonConstant.DEFAULT_PASSWORD));
        adminUser.setStatus(UserStatusEnum.NORMAL);
        
        baseMapper.insert(adminUser);
        log.info("管理员创建成功，id: {}, account: {}", adminUser.getId(), adminUser.getAccount());
        
        // 不返回密码
        adminUser.setPassword(null);
        return adminUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUser updateAdmin(Long id, AdminUser adminUser) {
        log.info("开始更新管理员，id: {}", id);
        
        AdminUser existing = baseMapper.selectById(id);
        if (Objects.isNull(existing)) {
            log.warn("管理员不存在，id: {}", id);
            throw new BusinessException(ResultCodeEnum.DATA_NOT_FOUND);
        }

        // 如果修改了手机号，检查是否已被使用
        if (adminUser.getPhone() != null && !adminUser.getPhone().equals(existing.getPhone())) {
            if (phoneExists(adminUser.getPhone())) {
                log.warn("手机号已存在，phone: {}", adminUser.getPhone());
                throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "手机号已存在");
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

        baseMapper.updateById(existing);
        log.info("管理员更新成功，id: {}", id);
        
        // 不返回密码
        existing.setPassword(null);
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Long id) {
        log.info("开始删除管理员，id: {}", id);
        
        AdminUser existing = baseMapper.selectById(id);
        if (Objects.isNull(existing)) {
            log.warn("管理员不存在，id: {}", id);
            throw new BusinessException(ResultCodeEnum.DATA_NOT_FOUND);
        }
        
        baseMapper.deleteById(id);
        log.info("管理员删除成功，id: {}", id);
    }

    @Override
    public Boolean phoneExists(String phone) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getPhone, phone)
        );
        return count > 0;
    }
}
