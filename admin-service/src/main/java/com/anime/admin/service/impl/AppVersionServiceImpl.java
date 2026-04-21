package com.anime.admin.service.impl;

import com.anime.admin.entity.AppVersion;
import com.anime.admin.mapper.AppVersionMapper;
import com.anime.admin.service.AppVersionService;
import com.anime.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url}")
    private String serverPort;

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

    @Override
    public Map<String, Object> uploadFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 验证文件类型（只允许 APK、IPA 等安装包）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名不能为空");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!(extension.equals(".apk") || extension.equals(".ipa") || extension.equals(".exe") || extension.equals(".dmg"))) {
            throw new BusinessException(400, "只支持上传 .apk, .ipa, .exe, .dmg 格式的文件");
        }

        // 生成唯一文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "_" + timestamp + extension;
        
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 保存文件
        Path filePath = uploadDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        // 返回文件信息
        Map<String, Object> result = new HashMap<>();
        // 生成可通过 /files/ 路径访问的 URL
        String fileUrl = serverPort + uniqueFilename;
        result.put("url", fileUrl);
        result.put("filename", originalFilename);
        result.put("size", file.getSize());
        result.put("extension", extension);
        
        return result;
    }
}
