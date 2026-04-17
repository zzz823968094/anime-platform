package com.anime.user.controller;

import com.anime.common.result.Result;
import com.anime.user.entity.UserFavorite;
import com.anime.user.mapper.UserFavoriteMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserFavoriteMapper favoriteMapper;

    /** 收藏番剧 */
    @PostMapping("/{animeId}")
    public Result<?> add(@PathVariable("animeId") Long animeId,
                         @RequestHeader("X-User-Id") Long userId) {
        // 检查是否已收藏
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getAnimeId, animeId)
        );
        if (count > 0) return Result.ok("已经收藏过了");

        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setAnimeId(animeId);
        fav.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(fav);
        return Result.ok("收藏成功");
    }

    /** 取消收藏 */
    @DeleteMapping("/{animeId}")
    public Result<?> remove(@PathVariable("animeId") Long animeId,
                            @RequestHeader("X-User-Id") Long userId) {
        favoriteMapper.delete(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getAnimeId, animeId)
        );
        return Result.ok("已取消收藏");
    }

    /** 获取收藏列表（返回 animeId 列表） */
    @GetMapping("/list")
    public Result<?> list(@RequestHeader("X-User-Id") Long userId) {
        List<UserFavorite> favs = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreatedAt)
        );
        List<String> animeIds = new ArrayList<>();
        favs.forEach(item -> animeIds.add(String.valueOf(item.getAnimeId())));
        return Result.ok(animeIds);
    }

    /** 检查是否已收藏 */
    @GetMapping("/{animeId}/check")
    public Result<?> check(@PathVariable("animeId") Long animeId,
                           @RequestHeader("X-User-Id") Long userId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getAnimeId, animeId)
        );
        return Result.ok(count > 0);
    }
}