package com.erp.ai.common.model;

import com.erp.ai.common.client.LlmToolCall;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 单条对话消息，对应 Chat Completions 的 messages 元素。
 * <p>
 * Day18 路径 A 扩展：
 * <ul>
 *   <li>{@code assistant} + {@link #toolCalls}：模型请求调工具</li>
 *   <li>{@code tool} + {@link #toolCallId}：工具执行结果回填</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    private String role;
    private String content;
    private List<LlmToolCall> toolCalls;
    private String toolCallId;
    private String name;

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static ChatMessage assistantToolCalls(List<LlmToolCall> toolCalls) {
        ChatMessage m = new ChatMessage();
        m.setRole("assistant");
        m.setContent(null);
        m.setToolCalls(toolCalls);
        return m;
    }

    public static ChatMessage toolResult(String toolCallId, String toolName, String contentJson) {
        ChatMessage m = new ChatMessage();
        m.setRole("tool");
        m.setToolCallId(toolCallId);
        m.setName(toolName);
        m.setContent(contentJson);
        return m;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<LlmToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<LlmToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
