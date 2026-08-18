package com.erp.ai.common.model;

import com.erp.ai.common.client.LlmToolCall;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 单条对话消息，对应 Chat Completions 的 {@code messages[]} 元素。
 *
 * <h2>职责</h2>
 * 在业务层与 LLM 客户端之间传递 role/content，以及 Day18 路径 A 所需的 tool 相关字段。
 *
 * <h2>Day18 路径 A 扩展</h2>
 * <ul>
 *   <li>{@code assistant} + {@link #toolCalls}：模型请求调工具</li>
 *   <li>{@code tool} + {@link #toolCallId}：工具执行结果回填</li>
 * </ul>
 *
 * <h2>与序列化的关系</h2>
 * {@code @JsonInclude(NON_NULL)}：序列化时省略 null 字段，避免把空 tool_calls 发给网关。
 *
 * <h2>学习要点</h2>
 * role 取值通常为 system/user/assistant/tool；错误 role 会导致网关 400。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    /**
     * 消息角色：{@code system} / {@code user} / {@code assistant} / {@code tool}。
     */
    private String role;

    /**
     * 文本内容；assistant 发起 tool_calls 时可能为 {@code null}。
     */
    private String content;

    /**
     * assistant 请求的工具调用列表；仅路径 A 多轮续聊时使用。
     */
    private List<LlmToolCall> toolCalls;

    /**
     * role=tool 时必填：对应某次 tool_call 的 id。
     */
    private String toolCallId;

    /**
     * 工具名（部分网关在 tool 消息中需要 name 字段）。
     */
    private String name;

    /**
     * Jackson / 框架用的无参构造。
     */
    public ChatMessage() {
    }

    /**
     * 构造普通文本消息。
     *
     * @param role    角色
     * @param content 文本内容
     */
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 工厂：assistant 发起 tool_calls（content 置 null）。
     *
     * @param toolCalls 模型返回的调用列表；不应为空
     * @return 新消息实例
     */
    public static ChatMessage assistantToolCalls(List<LlmToolCall> toolCalls) {
        ChatMessage m = new ChatMessage();
        m.setRole("assistant");
        m.setContent(null);
        m.setToolCalls(toolCalls);
        return m;
    }

    /**
     * 工厂：工具执行结果回填。
     *
     * @param toolCallId  必须与模型 tool_call.id 一致
     * @param toolName    工具名
     * @param contentJson 工具输出 JSON 字符串
     * @return role=tool 的消息
     */
    public static ChatMessage toolResult(String toolCallId, String toolName, String contentJson) {
        ChatMessage m = new ChatMessage();
        m.setRole("tool");
        m.setToolCallId(toolCallId);
        m.setName(toolName);
        m.setContent(contentJson);
        return m;
    }

    /** @return 角色 */
    public String getRole() {
        return role;
    }

    /** @param role 角色 */
    public void setRole(String role) {
        this.role = role;
    }

    /** @return 文本内容，可能为 null */
    public String getContent() {
        return content;
    }

    /** @param content 文本内容 */
    public void setContent(String content) {
        this.content = content;
    }

    /** @return tool_calls，可能为 null */
    public List<LlmToolCall> getToolCalls() {
        return toolCalls;
    }

    /** @param toolCalls 工具调用列表 */
    public void setToolCalls(List<LlmToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    /** @return tool_call_id */
    public String getToolCallId() {
        return toolCallId;
    }

    /** @param toolCallId 回填用的调用 ID */
    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    /** @return 工具名 */
    public String getName() {
        return name;
    }

    /** @param name 工具名 */
    public void setName(String name) {
        this.name = name;
    }
}
