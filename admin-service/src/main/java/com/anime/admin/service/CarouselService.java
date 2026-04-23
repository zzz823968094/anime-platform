package com.anime.admin.service;

import com.anime.admin.entity.Carousel;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CarouselService extends IService<Carousel> {

    Carousel create(Carousel carousel);

    Carousel update(Long id, Carousel carousel);

    void delete(Long id);

    /**
     * 启用轮播图（自动管理最多5个启用状态）
     */
    void enable(Long id);

    /**
     * 禁用轮播图
     */
    void disable(Long id);
}
