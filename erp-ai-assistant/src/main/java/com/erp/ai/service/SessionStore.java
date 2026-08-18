package com.erp.ai.service;

import com.erp.ai.config.AiProperties;
import com.erp.ai.model.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 多轮会话存储器：默认持久化到学习库 MySQL（表 {@code chat_session_message}）。
 */
@Component
public class SessionStore {

    private final JdbcTemplate jdbc;
    private final AiProperties properties;

    public SessionStore(JdbcTemplate jdbc, AiProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    public String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return jdbc.query(
                """
                        SELECT role, content FROM chat_session_message
                        WHERE session_id = ?
                        ORDER BY id ASC
                        """,
                (rs, i) -> new ChatMessage(rs.getString("role"), rs.getString("content")),
                sessionId
        );
    }

    public synchronized void append(String sessionId, ChatMessage userMessage, ChatMessage assistantMessage) {
        jdbc.update(
                "INSERT INTO chat_session_message(session_id, role, content) VALUES (?,?,?)",
                sessionId, userMessage.getRole(), userMessage.getContent()
        );
        jdbc.update(
                "INSERT INTO chat_session_message(session_id, role, content) VALUES (?,?,?)",
                sessionId, assistantMessage.getRole(), assistantMessage.getContent()
        );
        trim(sessionId);
    }

    private void trim(String sessionId) {
        int max = Math.max(2, properties.getSession().getMaxMessages());
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM chat_session_message WHERE session_id = ?",
                Integer.class,
                sessionId
        );
        if (count == null || count <= max) {
            return;
        }
        int remove = count - max;
        // MySQL 5.7：按 id 删最旧 N 条
        List<Long> ids = jdbc.query(
                """
                        SELECT id FROM chat_session_message
                        WHERE session_id = ?
                        ORDER BY id ASC
                        LIMIT ?
                        """,
                (rs, i) -> rs.getLong("id"),
                sessionId,
                remove
        );
        for (Long id : ids) {
            jdbc.update("DELETE FROM chat_session_message WHERE id = ?", id);
        }
    }
}
