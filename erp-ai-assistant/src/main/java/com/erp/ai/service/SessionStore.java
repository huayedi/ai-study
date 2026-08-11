package com.erp.ai.service;

import com.erp.ai.config.AiProperties;
import com.erp.ai.model.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 第 1–2 周使用内存会话。后续可替换为 Redis / DB 实现。
 */
@Component
public class SessionStore {

    private final Map<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();
    private final AiProperties properties;

    public SessionStore(AiProperties properties) {
        this.properties = properties;
    }

    public String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return new ArrayList<>(sessions.getOrDefault(sessionId, List.of()));
    }

    public synchronized void append(String sessionId, ChatMessage userMessage, ChatMessage assistantMessage) {
        List<ChatMessage> history = sessions.computeIfAbsent(sessionId, key -> new ArrayList<>());
        history.add(userMessage);
        history.add(assistantMessage);
        trim(history);
    }

    private void trim(List<ChatMessage> history) {
        int max = Math.max(2, properties.getSession().getMaxMessages());
        while (history.size() > max) {
            history.remove(0);
        }
    }
}
