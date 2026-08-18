package com.erp.ai.client;

import java.util.List;

/**
 * 单次 LLM 调用的原始结果（尚未解析为业务 JSON）。
 * <p>
 * Day18 路径 A：若 {@link #toolCalls} 非空，表示模型要求先执行工具，此时 {@link #content} 可能为空。
 */
public class LlmResult {

    private final String content;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;
    private final List<LlmToolCall> toolCalls;

    public LlmResult(String content, String model, int promptTokens, int completionTokens) {
        this(content, model, promptTokens, completionTokens, List.of());
    }

    public LlmResult(String content,
                     String model,
                     int promptTokens,
                     int completionTokens,
                     List<LlmToolCall> toolCalls) {
        this.content = content;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public String getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    public List<LlmToolCall> getToolCalls() {
        return toolCalls;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
