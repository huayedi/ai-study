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
 * 多轮会话存储器（第 1–2 周：进程内内存实现）。
 * <p>
 * 特点：
 * <ul>
 *   <li>重启进程后历史丢失</li>
 *   <li>适合本地学习，后续可替换为 Redis / DB</li>
 * </ul>
 * 并发说明：{@link ConcurrentHashMap} 保证不同 session 的 map 操作线程安全；
 * {@link #append} 使用 synchronized，避免同一 session 并发写导致列表损坏。
 */
@Component
public class SessionStore {

    /** sessionId → 历史消息（user/assistant 交替） */
    private final Map<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();
    private final AiProperties properties;

    public SessionStore(AiProperties properties) {
        this.properties = properties;
    }

    /**
     * 若客户端未传 sessionId，则新建一个；否则复用。
     */
    public String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    /**
     * 返回历史副本，避免外部修改内部列表。
     */
    public List<ChatMessage> getHistory(String sessionId) {
        return new ArrayList<>(sessions.getOrDefault(sessionId, List.of()));
    }

    /**
     * 追加一轮成功对话，并按配置裁剪过长历史，控制 Token 成本。
     */
    public synchronized void append(String sessionId, ChatMessage userMessage, ChatMessage assistantMessage) {
        List<ChatMessage> history = sessions.computeIfAbsent(sessionId, key -> new ArrayList<>());
        history.add(userMessage);
        history.add(assistantMessage);
        trim(history);
    }

    /**
     * 从队头删除最旧消息，直到不超过 maxMessages。
     */
    private void trim(List<ChatMessage> history) {
        int max = Math.max(2, properties.getSession().getMaxMessages());
        while (history.size() > max) {
            history.remove(0);
        }
    }
}
