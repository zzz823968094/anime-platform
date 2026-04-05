package com.anime.crawler.service;

import com.anime.crawler.entity.Anime;
import com.anime.crawler.mapper.AnimeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BangumiService {

    private final AnimeMapper animeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * 批量补全所有封面为空的番剧
     */
    public void fillMissingCovers() {
        // 查找封面为空的番剧
        List<Anime> animes = animeMapper.selectList(
                new LambdaQueryWrapper<Anime>()
                        .isNull(Anime::getCoverImage)
                        .or()
                        .eq(Anime::getCoverImage, "")
        );
        log.info("[Bangumi] 共{}部番剧缺少封面，开始补全...", animes.size());

        int success = 0;
        int fail = 0;
        for (Anime anime : animes) {
            try {
                Thread.sleep(1000); // 每次请求间隔1秒
                boolean ok = fillCover(anime);
                if (ok) success++;
                else fail++;
            } catch (Exception e) {
                log.error("[Bangumi] 处理番剧失败: {}, 错误: {}", anime.getTitle(), e.getMessage());
                fail++;
            }
        }
        log.info("[Bangumi] 补全完成，成功{}部，失败{}部", success, fail);
    }

    /**
     * 补全单部番剧的封面和简介
     */
    public boolean fillCover(Anime anime) throws Exception {
        String title = anime.getTitle()
                .replaceAll("第[一二三四五六七八九十百千万\\d]+季", "")
                .replaceAll("\\s*\\(\\d{4}\\)\\s*", "")
                .replaceAll("\\s*(日语版|普通话版|国语版)\\s*", "")
                .trim();

        String url = "https://api.bgm.tv/search/subject/" +
                java.net.URLEncoder.encode(title, "UTF-8") +
                "?type=2&responseGroup=small&max_results=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "anime-platform/1.0 (https://github.com/anime-platform)")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.debug("[Bangumi] 搜索失败: {}, 状态码: {}", title, response.statusCode());
            return false;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode list = root.path("list");
        if (list.isEmpty() || !list.isArray() || list.size() == 0) {
            log.debug("[Bangumi] 未找到: {}", title);
            return false;
        }

        JsonNode item = list.get(0);

        // 封面图
        String cover = item.path("images").path("common").asText("");
        if (cover.isEmpty()) cover = item.path("images").path("medium").asText("");
        if (cover.isEmpty()) cover = item.path("image").asText("");

        // 简介
        String synopsis = item.path("summary").asText("");

        // 评分
        double rating = item.path("rating").path("score").asDouble(0);

        // Bangumi ID
        String bangumiId = item.path("id").asText("");

        if (cover.isEmpty()) {
            log.debug("[Bangumi] 找到但无封面: {}", title);
            return false;
        }

        // 更新数据库
        anime.setCoverImage(cover);
        if (synopsis != null && !synopsis.isEmpty()) anime.setSynopsis(synopsis);
        if (rating > 0) anime.setRating(rating);
        if (!bangumiId.isEmpty()) anime.setBangumiId(bangumiId);
        animeMapper.updateById(anime);

        log.debug("[Bangumi] 补全成功: {} -> {}", anime.getTitle(), cover);
        return true;
    }
}