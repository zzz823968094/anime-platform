package com.anime.danmaku.config;

import com.anime.danmaku.ws.DanmakuHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DanmakuHandler danmakuHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(danmakuHandler, "/ws/danmaku/{videoId}")
                .setAllowedOrigins("*");
    }
}