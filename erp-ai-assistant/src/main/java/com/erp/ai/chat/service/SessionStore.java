package com.erp.ai.chat.service;

import com.erp.ai.chat.entity.ChatSessionMessage;
import com.erp.ai.chat.mapper.ChatSessionMessageMapper;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.model.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Chat 域会话存储：MyBatis-Plus XML → chat_session_message。
 */
@Component
public class SessionStore {

    private final ChatSessionMessageMapper messageMapper;
    private final AiProperties properties;

    public SessionStore(ChatSessionMessageMapper messageMapper, AiProperties properties) {
        this.messageMapper = messageMapper;
        this.properties = properties;
    }

    public String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    public List<ChatMessage> getHistory(String sessionId) {
        List<ChatSessionMessage> rows = messageMapper.selectBySessionId(sessionId);
        List<ChatMessage> history = new ArrayList<>(rows.size());
        for (ChatSessionMessage row : rows) {
            history.add(new ChatMessage(row.getRole(), row.getContent()));
        }
        return history;
    }

    public synchronized void append(String sessionId, ChatMessage userMessage, ChatMessage assistantMessage) {
        ChatSessionMessage user = new ChatSessionMessage();
        user.setSessionId(sessionId);
        user.setRole(userMessage.getRole());
        user.setContent(userMessage.getContent());
        messageMapper.insertMessage(user);

        ChatSessionMessage assistant = new ChatSessionMessage();
        assistant.setSessionId(sessionId);
        assistant.setRole(assistantMessage.getRole());
        assistant.setContent(assistantMessage.getContent());
        messageMapper.insertMessage(assistant);

        trim(sessionId);
    }

    private void trim(String sessionId) {
        int max = Math.max(2, properties.getSession().getMaxMessages());
        int count = messageMapper.countBySessionId(sessionId);
        if (count <= max) {
            return;
        }
        List<Long> ids = messageMapper.selectOldestIds(sessionId, count - max);
        for (Long id : ids) {
            messageMapper.deleteMessageById(id);
        }
    }
}
