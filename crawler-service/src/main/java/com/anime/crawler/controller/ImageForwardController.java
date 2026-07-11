package com.anime.crawler.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageForwardController {

    private static final int TIMEOUT = 15000;

    /**
     * 流式转发远程图片，不占用大量内存
     *
     * @param url 远程图片地址
     */
    @GetMapping("/forward")
    public ResponseEntity<InputStreamResource> forward(@RequestParam("url") String url) {
        try {
            HttpResponse response = HttpRequest.get(url)
                    .timeout(TIMEOUT)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", url)
                    .execute();

            if (!response.isOk()) {
                response.close();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            // 从远程响应中获取 Content-Type
            String contentType = response.header("Content-Type");
            if (contentType == null) {
                contentType = "image/jpeg";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("max-age=86400");

            // 使用流式传输，避免将整个图片加载到内存
            InputStreamResource body = new InputStreamResource(response.bodyStream());

            return new ResponseEntity<>(body, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("图片转发失败, url: {}", url, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
