package com.anime.admin.service;

import com.anime.admin.entity.AppVersion;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AppVersionService extends IService<AppVersion> {

    AppVersion create(AppVersion appVersion);

    AppVersion update(Integer id, AppVersion appVersion);

    void delete(Integer id);

    AppVersion getLatest(String platform);
}
