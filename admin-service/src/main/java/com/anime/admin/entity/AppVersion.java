package com.anime.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("app_version")
public class AppVersion {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer versionCode;

    private String versionName;

    private String platform;

    private String downloadUrl;

    private String releaseNotes;

    private Boolean forceUpdate;

    private Long fileSize;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
