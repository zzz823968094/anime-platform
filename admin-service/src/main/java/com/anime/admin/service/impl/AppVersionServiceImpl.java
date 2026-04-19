package com.anime.admin.service.impl;

import com.anime.admin.entity.AppVersion;
import com.anime.admin.mapper.AppVersionMapper;
import com.anime.admin.service.AppVersionService;
import com.anime.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    @Override
    public AppVersion create(AppVersion appVersion) {
        if (versionCodeExists(appVersion.getPlatform(), appVersion.getVersionCode())) {
            throw new BusinessException(400, "该平台版本号已存在");
        }
        appVersion.setStatus("active");
        appVersion.setForceUpdate(appVersion.getForceUpdate() != null && appVersion.getForceUpdate());
        appVersion.setCreateTime(LocalDateTime.now());
        appVersion.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(appVersion);
        return appVersion;
    }

    @Override
    public AppVersion update(Integer id, AppVersion appVersion) {
        AppVersion existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "版本不存在");
        }
        if (appVersion.getVersionCode() != null
                && !appVersion.getVersionCode().equals(existing.getVersionCode())
                && versionCodeExists(existing.getPlatform(), appVersion.getVersionCode())) {
            throw new BusinessException(400, "该平台版本号已存在");
        }
        if (appVersion.getVersionCode() != null) existing.setVersionCode(appVersion.getVersionCode());
        if (appVersion.getVersionName() != null) existing.setVersionName(appVersion.getVersionName());
        if (appVersion.getPlatform() != null) existing.setPlatform(appVersion.getPlatform());
        if (appVersion.getDownloadUrl() != null) existing.setDownloadUrl(appVersion.getDownloadUrl());
        if (appVersion.getReleaseNotes() != null) existing.setReleaseNotes(appVersion.getReleaseNotes());
        if (appVersion.getForceUpdate() != null) existing.setForceUpdate(appVersion.getForceUpdate());
        if (appVersion.getFileSize() != null) existing.setFileSize(appVersion.getFileSize());
        if (appVersion.getStatus() != null) existing.setStatus(appVersion.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Integer id) {
        if (baseMapper.selectById(id) == null) {
            throw new BusinessException(404, "版本不存在");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public AppVersion getLatest(String platform) {
        LambdaQueryWrapper<AppVersion> wrapper = new LambdaQueryWrapper<AppVersion>()
                .eq(AppVersion::getStatus, "active")
                .orderByDesc(AppVersion::getVersionCode)
                .last("LIMIT 1");
        if (platform != null && !platform.isBlank()) {
            wrapper.and(w -> w.eq(AppVersion::getPlatform, platform).or().eq(AppVersion::getPlatform, "all"));
        }
        return baseMapper.selectOne(wrapper);
    }

    private boolean versionCodeExists(String platform, Integer versionCode) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<AppVersion>()
                        .eq(AppVersion::getPlatform, platform)
                        .eq(AppVersion::getVersionCode, versionCode)
        ) > 0;
    }
}
