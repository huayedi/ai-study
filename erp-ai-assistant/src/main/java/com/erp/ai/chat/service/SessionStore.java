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
 * Chat 域会话存储组件。
 * <p>
 * <b>职责</b>：解析 / 生成 sessionId、按会话读写历史消息、追加一轮 user+assistant，
 * 并在超过配置上限时删除最旧消息（滑动窗口）。
 * <p>
 * <b>在系统中的位置</b>：chat 域 service 辅助组件；被 {@link ChatService} 调用。
 * 持久化路径：本类 → {@link ChatSessionMessageMapper} → XML → 表 {@code chat_session_message}。
 * <p>
 * <b>对应学习 Day</b>：Week1 多轮上下文；配置项来自 {@code ai.session.max-messages}（经 {@link AiProperties}）。
 * <p>
 * <b>调用链 / 上下游</b>：
 * <ul>
 *   <li>{@link #resolveSessionId}：请求入口统一会话键</li>
 *   <li>{@link #getHistory}：拼 LLM messages 前加载历史</li>
 *   <li>{@link #append}：本轮成功后落库；内部 {@link #trim} 控窗口</li>
 * </ul>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>只写会话消息表，不写 ERP 业务库；与 draft「无 session / 不写库」形成对比</li>
 *   <li>{@link #append} 使用 {@code synchronized}，降低同 session 并发追加时 trim 竞态（学习环境简化锁）</li>
 *   <li>历史按 id 升序还原为 {@link ChatMessage}，role/content 原样传递</li>
 *   <li>maxMessages 下限钳制为至少 2，保证至少能留住一对 user/assistant</li>
 * </ul>
 *
 * @see ChatService
 * @see ChatSessionMessageMapper
 * @see ChatSessionMessage
 */
@Component
public class SessionStore {

    /** MyBatis Mapper：会话消息 CRUD / 自定义 SQL */
    private final ChatSessionMessageMapper messageMapper;

    /** AI 配置：读取 session.maxMessages 等 */
    private final AiProperties properties;

    /**
     * 构造注入 Mapper 与配置。
     *
     * @param messageMapper 会话消息 Mapper，不可为 null
     * @param properties    全局 AI 配置，不可为 null
     */
    public SessionStore(ChatSessionMessageMapper messageMapper, AiProperties properties) {
        this.messageMapper = messageMapper;
        this.properties = properties;
    }

    /**
     * 解析会话 ID：空则新建，非空则原样使用。
     * <p>
     * 业务含义：首轮客户端可不传 sessionId；服务端生成 UUID 后经响应返回，后续必须回传才能续上下文。
     * 边界：null 或空白（含仅空格）均视为新会话。
     *
     * @param sessionId 客户端传入的会话 ID，可为 null / blank
     * @return 非空会话 ID（新建时为随机 UUID 字符串）
     */
    public String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    /**
     * 读取某会话的全部历史消息，并转为 LLM 可用的 {@link ChatMessage} 列表。
     * <p>
     * 边界：无历史时返回空列表；顺序与库中 id ASC 一致，保证多轮先后正确。
     *
     * @param sessionId 会话 ID
     * @return 历史消息列表（可能为空，不为 null）
     */
    public List<ChatMessage> getHistory(String sessionId) {
        List<ChatSessionMessage> rows = messageMapper.selectBySessionId(sessionId);
        List<ChatMessage> history = new ArrayList<>(rows.size());
        for (ChatSessionMessage row : rows) {
            // 实体 → 领域消息：只取 role/content，忽略 id/时间等持久化字段
            history.add(new ChatMessage(row.getRole(), row.getContent()));
        }
        return history;
    }

    /**
     * 追加一轮对话（先 user 后 assistant），然后按上限裁剪最旧消息。
     * <p>
     * 为何同步：同一 session 并发两次成功回调时，count/selectOldest/delete 若不串行，
     * 可能删多或删少；学习项目用方法级锁简化，未做分布式锁。
     * <p>
     * 边界：调用方应在 LLM/解析成功后再调用，避免把失败轮次写入历史污染上下文。
     *
     * @param sessionId         会话 ID
     * @param userMessage       本轮用户消息（role/content）
     * @param assistantMessage  本轮助手原始输出（通常为模型 content 字符串）
     */
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

        // 插入后再 trim：保证本轮已落库，再删最旧，窗口含最新一轮
        trim(sessionId);
    }

    /**
     * 将某会话消息数裁剪到配置上限：删除最旧的 (count - max) 条。
     * <p>
     * 为何 max 至少为 2：避免配置误写成 0/1 导致无法保留完整一轮问答。
     *
     * @param sessionId 会话 ID
     */
    private void trim(String sessionId) {
        int max = Math.max(2, properties.getSession().getMaxMessages());
        int count = messageMapper.countBySessionId(sessionId);
        if (count <= max) {
            return;
        }
        // 只删超出部分的最旧 id，保留最近 max 条
        List<Long> ids = messageMapper.selectOldestIds(sessionId, count - max);
        for (Long id : ids) {
            messageMapper.deleteMessageById(id);
        }
    }
}
