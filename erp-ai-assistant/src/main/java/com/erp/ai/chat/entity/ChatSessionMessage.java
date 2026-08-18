package com.erp.ai.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 聊天会话消息表实体（chat 域持久化模型）。
 * <p>
 * <b>职责</b>：映射表 {@code chat_session_message} 的一行，保存单条 user/assistant（等）消息，
 * 供多轮对话历史读取与追加。
 * <p>
 * <b>在系统中的位置</b>：entity 层，由 {@link com.erp.ai.chat.mapper.ChatSessionMessageMapper}
 * 与 XML {@code mapper/chat/ChatSessionMessageMapper.xml} 访问；
 * 业务侧通过 {@link com.erp.ai.chat.service.SessionStore} 间接使用，不直接暴露给 HTTP。
 * <p>
 * <b>对应学习 Day</b>：Week1 多轮会话落库；与 Day18 工具能力解耦——本表只存对话文本，不存 tool_calls 中间态。
 * <p>
 * <b>调用链 / 上下游</b>：
 * <ul>
 *   <li>写入：{@code SessionStore.append} → {@code insertMessage}</li>
 *   <li>读取：{@code SessionStore.getHistory} → {@code selectBySessionId} → 转为 {@code ChatMessage}</li>
 *   <li>裁剪：超限时 {@code selectOldestIds} + {@code deleteMessageById}</li>
 * </ul>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>仅会话消息持久化，不是业务单据库；与 draft「不写业务库」不同——chat 会写本会话表</li>
 *   <li>{@code role}/{@code content} 与 LLM 消息角色对齐（如 user、assistant）</li>
 *   <li>主键自增；插入时 {@code created_at} 可由库默认填充（XML insert 未显式写该列）</li>
 * </ul>
 *
 * @see com.erp.ai.chat.mapper.ChatSessionMessageMapper
 * @see com.erp.ai.chat.service.SessionStore
 */
@TableName("chat_session_message")
public class ChatSessionMessage {

    /** 主键，数据库自增；删除最旧消息时按此 id 定位 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话 ID，与客户端续聊回传的 sessionId 一致 */
    private String sessionId;

    /** 消息角色，如 user / assistant；拼进 LLM 上下文时原样使用 */
    private String role;

    /** 消息正文；assistant 侧通常存模型原始 content（含 JSON 字符串） */
    private String content;

    /** 行创建时间；查询结果映射自 {@code created_at}，插入 XML 未写该列时依赖库默认值 */
    private LocalDateTime createdAt;

    /**
     * @return 主键 id，未持久化前可能为 null
     */
    public Long getId() { return id; }

    /**
     * @param id 主键 id
     */
    public void setId(Long id) { this.id = id; }

    /**
     * @return 会话 ID
     */
    public String getSessionId() { return sessionId; }

    /**
     * @param sessionId 会话 ID，append 时必填
     */
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    /**
     * @return 消息角色
     */
    public String getRole() { return role; }

    /**
     * @param role 消息角色（user/assistant 等）
     */
    public void setRole(String role) { this.role = role; }

    /**
     * @return 消息正文
     */
    public String getContent() { return content; }

    /**
     * @param content 消息正文，可较长文本
     */
    public void setContent(String content) { this.content = content; }

    /**
     * @return 创建时间；视库表与查询是否带回而定，可能为 null
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
