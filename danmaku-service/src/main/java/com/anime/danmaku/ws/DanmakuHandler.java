package com.anime.danmaku.ws;

import com.anime.danmaku.entity.Danmaku;
import com.anime.danmaku.mapper.DanmakuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DanmakuHandler extends TextWebSocketHandler {

    private final ConnectionManager connectionManager;
    private final DanmakuMapper danmakuMapper;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long videoId = getVideoId(session);
        connectionManager.join(videoId, session);
        log.info("[弹幕] 用户加入房间 video_{}", videoId);

        // 推送历史弹幕
        List<Danmaku> history = danmakuMapper.selectList(
                new LambdaQueryWrapper<Danmaku>()
                        .eq(Danmaku::getVideoId, videoId)
                        .eq(Danmaku::getStatus, 0)
                        .orderByAsc(Danmaku::getTimePoint)
                        .last("limit 200")
        );
        Map<String, Object> historyMsg = new HashMap<>();
        historyMsg.put("type", "history");
        historyMsg.put("data", history);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(historyMsg)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long videoId = getVideoId(session);
        Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) data.get("type");

        if ("ping".equals(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
            return;
        }

        if ("danmaku".equals(type)) {
            String content = (String) data.get("content");
            if (content == null || content.trim().isEmpty()) return;

            // 存数据库
            Danmaku danmaku = new Danmaku();
            danmaku.setVideoId(videoId);
            danmaku.setContent(content.trim());
            danmaku.setTimePoint(((Number) data.getOrDefault("timePoint", 0)).floatValue());
            danmaku.setColor((String) data.getOrDefault("color", "#FFFFFF"));
            danmaku.setDmType(((Number) data.getOrDefault("dmType", 0)).intValue());
            danmaku.setStatus(0);
            danmakuMapper.insert(danmaku);

            // 广播给其他人
            Map<String, Object> broadcast = new HashMap<>();
            broadcast.put("type", "danmaku");
            broadcast.put("id", danmaku.getId());
            broadcast.put("content", danmaku.getContent());
            broadcast.put("timePoint", danmaku.getTimePoint());
            broadcast.put("color", danmaku.getColor());
            broadcast.put("dmType", danmaku.getDmType());
            broadcast.put("username", data.getOrDefault("username", "匿名"));
            String json = objectMapper.writeValueAsString(broadcast);
            connectionManager.broadcast(videoId, json, session);

            // 回复发送者
            broadcast.put("self", true);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(broadcast)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long videoId = getVideoId(session);
        connectionManager.leave(videoId, session);
        log.info("[弹幕] 用户离开房间 video_{}", videoId);
    }

    private Long getVideoId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
}