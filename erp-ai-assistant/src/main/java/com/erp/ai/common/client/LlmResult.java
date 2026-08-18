package com.erp.ai.common.client;

import java.util.List;

/**
 * 单次 LLM 调用的原始结果载体（尚未解析为业务 JSON / {@code AssistantReply}）。
 *
 * <h2>职责</h2>
 * 封装模型返回的文本内容、用量统计、以及可选的 function calling（tool_calls）。
 *
 * <h2>为何独立于业务模型</h2>
 * 传输层结果与业务 Schema 解耦：解析失败可触发重试，而审计仍可记录 Token。
 *
 * <h2>Day18 路径 A</h2>
 * 若 {@link #toolCalls} 非空，表示模型要求先执行工具；此时 {@link #content} 可能为 {@code null}
 * 或空串，上层不得当作最终答案。
 *
 * <h2>学习要点</h2>
 * {@code promptTokens}/{@code completionTokens} 来自网关 usage，Mock 实现为估算值；
 * 成本估算应在上层结合 {@code AiProperties} 单价计算。
 */
public class LlmResult {

    /** 模型生成的文本内容；tool_calls 轮次可能为 {@code null} */
    private final String content;

    /** 实际响应中的模型名（网关可能回写；Mock 为固定学习名） */
    private final String model;

    /** 输入（prompt）侧 Token 数；未知时实现可填 0 或估算值 */
    private final int promptTokens;

    /** 输出（completion）侧 Token 数；未知时实现可填 0 或估算值 */
    private final int completionTokens;

    /**
     * 模型请求的工具调用列表；无工具调用时为空列表（永不 {@code null}）。
     * 不可变副本，防止外部修改污染结果。
     */
    private final List<LlmToolCall> toolCalls;

    /**
     * 无 tool_calls 的便捷构造：等价于传入空工具列表。
     *
     * @param content          文本内容，可为 {@code null}
     * @param model            模型名，可为 {@code null}（调用方应尽量填写）
     * @param promptTokens     输入 Token，{@code >= 0}
     * @param completionTokens 输出 Token，{@code >= 0}
     */
    public LlmResult(String content, String model, int promptTokens, int completionTokens) {
        this(content, model, promptTokens, completionTokens, List.of());
    }

    /**
     * 完整构造。
     *
     * @param content          文本内容；tool 轮次允许 {@code null}
     * @param model            模型名
     * @param promptTokens     输入 Token
     * @param completionTokens 输出 Token
     * @param toolCalls        工具调用；{@code null} 会被规范为空列表
     */
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

    /**
     * @return 文本内容；可能为 {@code null}
     */
    public String getContent() {
        return content;
    }

    /**
     * @return 模型名；可能为 {@code null}
     */
    public String getModel() {
        return model;
    }

    /**
     * @return 输入 Token 数
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * @return 输出 Token 数
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * 总 Token = 输入 + 输出（简单相加，不含缓存命中等进阶字段）。
     *
     * @return {@code promptTokens + completionTokens}
     */
    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    /**
     * @return 不可变的 tool_calls 列表；无调用时为空列表，不会为 {@code null}
     */
    public List<LlmToolCall> getToolCalls() {
        return toolCalls;
    }

    /**
     * 是否存在待执行的工具调用。
     *
     * @return {@code true} 表示上层应进入工具执行循环，而非直接解析 content
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
