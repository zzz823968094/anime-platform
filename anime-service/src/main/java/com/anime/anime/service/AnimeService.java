package com.anime.anime.service;

import com.anime.anime.entity.Anime;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AnimeService extends IService<Anime> {

    Page<Anime> listAnime(int page, int size, String type, Integer status,
                          Integer year, String genre, String sort, String keyword);

    Page<Anime> search(String keyword, int page, int size);

    List<Anime> getHotRecommend(int count);
}