package com.anime.anime.service;

import com.anime.anime.entity.AnimeTable;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AnimeTableService extends IService<AnimeTable> {

    Page<AnimeTable> listAnime(int page, int size, String type, Integer status,
                          Integer year, String genre, String sort, String keyword);

    Page<AnimeTable> search(String keyword, int page, int size);

    List<AnimeTable> getHotRecommend(int count);
}