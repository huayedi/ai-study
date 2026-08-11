package com.erp.ai.client;

import com.erp.ai.model.ChatMessage;

import java.util.List;

/**
 * 大模型调用抽象。
 * <p>
 * 业务层（{@code ChatService}）只依赖本接口，不关心底层是 mock 还是 DeepSeek。
 * 这是典型的“面向接口编程”，方便切换实现与单测打桩。
 */
public interface LlmClient {

    /**
     * @return 实现标识，写入响应和日志，例如 {@code mock} / {@code openai-compatible}
     */
    String providerName();

    /**
     * 发起一次 Chat Completions 风格调用。
     *
     * @param messages 完整消息列表（通常含 system + 历史 + 当前 user）
     * @return 模型原始文本内容及 Token 用量等
     */
    LlmResult chat(List<ChatMessage> messages);
}
