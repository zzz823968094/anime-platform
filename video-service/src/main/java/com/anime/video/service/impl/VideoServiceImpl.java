package com.anime.video.service.impl;

import com.anime.video.entity.Video;
import com.anime.video.mapper.VideoMapper;
import com.anime.video.service.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {


}