package com.anime.danmaku.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ConnectionManager {

    // videoId -> 连接列表
    private final Map<Long, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public void join(Long videoId, WebSocketSession session) {
        rooms.computeIfAbsent(videoId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void leave(Long videoId, WebSocketSession session) {
        List<WebSocketSession> sessions = rooms.get(videoId);
        if (sessions != null) sessions.remove(session);
    }

    public void broadcast(Long videoId, String message, WebSocketSession sender) {
        List<WebSocketSession> sessions = rooms.get(videoId);
        if (sessions == null) return;
        List<WebSocketSession> dead = new CopyOnWriteArrayList<>();
        for (WebSocketSession session : sessions) {
            if (session.equals(sender)) continue;
            try {
                if (session.isOpen()) {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                } else {
                    dead.add(session);
                }
            } catch (IOException e) {
                dead.add(session);
            }
        }
        sessions.removeAll(dead);
    }
}