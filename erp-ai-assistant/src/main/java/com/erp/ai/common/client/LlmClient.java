package com.erp.ai.common.client;

import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.tool.ToolDefinition;

import java.util.List;

/**
 * 大模型调用抽象。
 * <p>
 * Day18 路径 A：{@link #chat(List, List)} 携带只读工具定义，可能返回 {@link LlmToolCall}。
 */
public interface LlmClient {

    String providerName();

    /**
     * 无 tools 的普通对话（兼容旧调用方）。
     */
    default LlmResult chat(List<ChatMessage> messages) {
        return chat(messages, List.of());
    }

    /**
     * @param messages 完整消息列表（可含 assistant.tool_calls / role=tool）
     * @param tools    只读工具定义；空列表表示本轮不暴露 tools
     */
    LlmResult chat(List<ChatMessage> messages, List<ToolDefinition> tools);
}
