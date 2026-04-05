package com.anime.crawler.service;

import com.anime.crawler.entity.Anime;
import com.anime.crawler.entity.Video;
import com.anime.crawler.mapper.AnimeMapper;
import com.anime.crawler.mapper.VideoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final AnimeMapper animeMapper;
    private final VideoMapper videoMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String BASE_URL   = "https://huohuzy.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private static final int PAGES_JP = 44;
    private static final int PAGES_US = 9;
    private static final int PAGES_CN = 47;
    private static final int STOP_AFTER_UNCHANGED = 10;

    // 失败记录的 Redis Set key 前缀
    private static final String FAILED_KEY_PREFIX = "crawler:failed:";

    // ─── 定时任务 ───────────────────────────────────────────────────────

    @Scheduled(cron = "0 0 */3 * * *")
    public void crawlLatestJP() {
        log.info("[爬虫] 定时：增量爬取日本动漫...");
        crawlIncremental(25);
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void crawlLatestOther() {
        log.info("[爬虫] 定时：增量爬取欧美/中国动漫...");
        crawlIncremental(26);
        crawlIncremental(24);
    }

    // ─── 手动触发 ────────────────────────────────────────────────────────

    public void crawlLatest() {
        log.info("[爬虫] 手动增量爬取所有分类...");
        crawlIncremental(25);
        crawlIncremental(26);
        crawlIncremental(24);
        log.info("[爬虫] 增量爬取完成");
    }

    public void crawlAll() {
        log.info("[爬虫] 全量爬取开始...");
        crawlType(25, PAGES_JP);
        crawlType(26, PAGES_US);
        crawlType(24, PAGES_CN);
        log.info("[爬虫] 全量爬取完成");
    }

    public void crawlType(int type, int maxPages) {
        log.info("[爬虫] 全量爬取 type={}, 共{}页", type, maxPages);
        for (int page = 1; page <= maxPages; page++) {
            crawlPage(type, page, false);
            sleep(2000);
        }
        log.info("[爬虫] type={} 全量完成", type);
    }

    public void crawlIncremental(int type) {
        int maxPages = type == 25 ? PAGES_JP : type == 26 ? PAGES_US : PAGES_CN;
        int unchangedCount = 0;

        for (int page = 1; page <= maxPages; page++) {
            int unchanged = crawlPage(type, page, true);
            unchangedCount += unchanged;
            log.info("[增量] type={} 第{}页，无变化累计{}个", type, page, unchangedCount);

            if (unchangedCount >= STOP_AFTER_UNCHANGED) {
                log.info("[增量] type={} 连续{}个无变化，停止于第{}页", type, unchangedCount, page);
                break;
            }
            sleep(2000);
        }
    }

    /**
     * 重试所有失败记录（接口 /crawler/retry/{type}）
     * 从 Redis 取出失败的 sourceId 列表，逐个重新爬取
     */
    public int retryFailed(int type) {
        String key = FAILED_KEY_PREFIX + type;
        Set<String> failedIds = redisTemplate.opsForSet().members(key);

        if (failedIds == null || failedIds.isEmpty()) {
            log.info("[重试] type={} 没有失败记录", type);
            return 0;
        }

        log.info("[重试] type={} 开始重试，共{}条", type, failedIds.size());
        int successCount = 0;

        for (String sourceId : failedIds) {
            try {
                sleep(1000);
                String url = BASE_URL + "/index.php/vod/detail/id/" + sourceId + ".html";
                boolean changed = crawlDetail(url, sourceId, type);
                if (changed) {
                    redisTemplate.opsForSet().remove(key, sourceId);
                    successCount++;
                    log.info("[重试] sourceId={} 成功", sourceId);
                }
            } catch (Exception e) {
                log.error("[重试] sourceId={} 仍失败: {}", sourceId, e.getMessage());
            }
        }

        log.info("[重试] type={} 完成，成功{}/{}条", type, successCount, failedIds.size());
        return successCount;
    }

    /** 查询失败记录数量 */
    public long getFailedCount(int type) {
        Long size = redisTemplate.opsForSet().size(FAILED_KEY_PREFIX + type);
        return size == null ? 0 : size;
    }

    // ─── 核心爬取 ─────────────────────────────────────────────────────────

    public int crawlPage(int type, int pageNum, boolean returnUnchanged) {
        int unchanged = 0;
        try {
            String listUrl = BASE_URL + "/index.php/vod/type/id/" + type + "/page/" + pageNum + ".html";
            Document listDoc = Jsoup.connect(listUrl)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .get();

            Elements links = listDoc.select("a[href*=/vod/detail/id/]");
            log.info("[爬虫] type={} 第{}页，找到{}个番剧", type, pageNum, links.size());

            for (Element link : links) {
                String href = link.attr("href");
                if (!href.contains("/vod/detail/id/")) continue;
                String sourceId = extractSourceId(href);
                if (sourceId == null) continue;

                try {
                    sleep(1000);
                    boolean changed = crawlDetail(BASE_URL + href, sourceId, type);
                    if (!changed) unchanged++;
                } catch (Exception e) {
                    log.error("[爬虫] 详情页失败: {}, 错误: {}", href, e.getMessage());
                    // 记录失败，等待后续重试
                    recordFailed(type, sourceId);
                }
            }
        } catch (Exception e) {
            log.error("[爬虫] 列表页失败: type={}, page={}, 错误: {}", type, pageNum, e.getMessage());
        }
        return unchanged;
    }

    private void recordFailed(int type, String sourceId) {
        try {
            redisTemplate.opsForSet().add(FAILED_KEY_PREFIX + type, sourceId);
        } catch (Exception e) {
            log.warn("[爬虫] 记录失败 sourceId 出错: {}", e.getMessage());
        }
    }

    private boolean crawlDetail(String url, String sourceId, int type) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(15000)
                .get();

        // 标题
        String title = "";
        Element titleEl = doc.selectFirst("h2");
        if (titleEl != null) title = titleEl.text().trim();
        if (title.isEmpty()) return false;

        // 封面
        String coverImage = "";
        Element coverEl = doc.selectFirst("div.vod-img img");
        if (coverEl != null) {
            coverImage = coverEl.attr("src");
            if (coverImage.isEmpty()) coverImage = coverEl.attr("data-original");
        }

        // 简介
        String synopsis = "";
        Element synopsisEl = doc.selectFirst("div.vod-introduce p");
        if (synopsisEl != null) {
            synopsis = synopsisEl.text().trim();
        }

        // 完结状态 & 评分
        int status = 1;
        double rating = 0.0;

        Element vodTitle = doc.selectFirst("div.vod-title");
        if (vodTitle != null) {
            Element statusSpan = vodTitle.selectFirst("span");
            if (statusSpan != null) {
                String spanText = statusSpan.text();
                if (spanText.contains("完结") || spanText.contains("全集")) {
                    status = 2;
                }
            }
            Element ratingLabel = vodTitle.selectFirst("label");
            if (ratingLabel != null) {
                try {
                    rating = Double.parseDouble(ratingLabel.text().trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        // 集数解析
        List<Video> episodes = parseEpisodes(doc, sourceId);
        if (episodes.isEmpty()) return false;

        // 番剧入库或更新
        Anime existing = animeMapper.selectOne(
                new LambdaQueryWrapper<Anime>().eq(Anime::getSourceId, sourceId)
        );

        Long animeId;
        boolean changed;

        if (existing == null) {
            Anime anime = new Anime();
            anime.setTitle(title);
            anime.setCoverImage(coverImage);
            anime.setSynopsis(synopsis);
            anime.setSourceId(sourceId);
            anime.setType(String.valueOf(type));
            anime.setStatus(status);
            anime.setCurrentEpisode(episodes.size());
            anime.setViewCount(0);
            anime.setFavoriteCount(0);
            if (rating > 0) anime.setRating(rating);
            animeMapper.insert(anime);
            animeId = anime.getId();
            changed = true;
            log.info("[爬虫] 新番剧: {} (type={}, status={})", title, type, status);
        } else {
            animeId = existing.getId();
            changed = existing.getCurrentEpisode() == null
                    || existing.getCurrentEpisode() != episodes.size()
                    || existing.getStatus() != status;
            existing.setCurrentEpisode(episodes.size());
            existing.setStatus(status);
            if (existing.getType() == null || existing.getType().isEmpty()) {
                existing.setType(String.valueOf(type));
            }
            if ((existing.getSynopsis() == null || existing.getSynopsis().isBlank())
                    && !synopsis.isBlank()) {
                existing.setSynopsis(synopsis);
                changed = true;
            }
            if (rating > 0 && (existing.getRating() == null
                    || existing.getRating().doubleValue() == 0)) {
                existing.setRating(rating);
            }
            animeMapper.updateById(existing);
        }

        if (changed) {
            for (Video ep : episodes) {
                ep.setAnimeId(animeId);
                Video existingEp = videoMapper.selectOne(
                        new LambdaQueryWrapper<Video>()
                                .eq(Video::getAnimeId, animeId)
                                .eq(Video::getEpisode, ep.getEpisode())
                );
                if (existingEp == null) {
                    ep.setViewCount(0);
                    ep.setStatus(1);
                    videoMapper.insert(ep);
                } else {
                    existingEp.setM3u8Url(ep.getM3u8Url());
                    existingEp.setTitle(ep.getTitle());
                    videoMapper.updateById(existingEp);
                }
            }
        }

        return changed;
    }

    // ─── 解析集数 ────────────────────────────────────────────────────────

    private List<Video> parseEpisodes(Document doc, String sourceId) {
        List<Video> episodes = new ArrayList<>();
        boolean inM3u8Block = false;

        for (Element el : doc.getAllElements()) {
            String text = el.ownText();
            if (text.contains("hhm3u8") || text.contains("豪华m3u8")) {
                inM3u8Block = true;
                continue;
            }
            if (inM3u8Block && text.contains("$")) {
                String[] parts = text.split("\\$");
                if (parts.length == 2) {
                    String epTitle = parts[0].trim();
                    String m3u8Url = parts[1].trim();
                    if (m3u8Url.contains("index.m3u8")) {
                        Video video = new Video();
                        video.setEpisode(parseEpisodeNumber(epTitle));
                        video.setTitle(epTitle);
                        video.setM3u8Url(m3u8Url);
                        video.setDuration(0);
                        episodes.add(video);
                    }
                }
            }
            if (inM3u8Block && (text.contains("hhyun") || text.contains("豪华云"))) {
                break;
            }
        }

        if (episodes.isEmpty()) {
            Elements links = doc.select("a[href*=index.m3u8]");
            for (Element link : links) {
                String href    = link.attr("href");
                String epTitle = link.text().split("\\$")[0].trim();
                Video video    = new Video();
                video.setEpisode(parseEpisodeNumber(epTitle));
                video.setTitle(epTitle);
                video.setM3u8Url(href);
                video.setDuration(0);
                episodes.add(video);
            }
        }

        return episodes;
    }

    // ─── 工具方法 ────────────────────────────────────────────────────────

    private String extractSourceId(String href) {
        try {
            String[] parts = href.split("/id/");
            if (parts.length < 2) return null;
            return parts[1].split("\\.")[0].split("\\?")[0].trim();
        } catch (Exception e) {
            return null;
        }
    }

    private int parseEpisodeNumber(String title) {
        try {
            String num = title.replaceAll("[^0-9]", "");
            return num.isEmpty() ? 1 : Integer.parseInt(num);
        } catch (Exception e) {
            return 1;
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}