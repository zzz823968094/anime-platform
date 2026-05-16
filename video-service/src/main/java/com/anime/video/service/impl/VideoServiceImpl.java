package com.anime.video.service.impl;

import com.anime.video.entity.Video;
import com.anime.video.mapper.VideoMapper;
import com.anime.video.service.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频服务实现类
 * 遵循阿里巴巴开发规范，业务逻辑下沉
 *
 * @author anime-platform
 * @date 2026-05-16
 */
@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {


}