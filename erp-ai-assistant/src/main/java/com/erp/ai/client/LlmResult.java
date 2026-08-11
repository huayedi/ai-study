package com.erp.ai.client;

/**
 * 单次 LLM 调用的原始结果（尚未解析为业务 JSON）。
 * <p>
 * 与 {@code AssistantReply} 的区别：
 * <ul>
 *   <li>{@code LlmResult.content}：模型返回的原始字符串（期望是 JSON 文本）</li>
 *   <li>{@code AssistantReply}：解析校验后的结构化业务对象</li>
 * </ul>
 */
public class LlmResult {

    /** 模型输出正文（本项目要求为 JSON 字符串） */
    private final String content;

    /** 实际命中的模型名（有些网关会回写） */
    private final String model;

    /** 提示词（输入）消耗的 Token 数 */
    private final int promptTokens;

    /** 生成（输出）消耗的 Token 数 */
    private final int completionTokens;

    public LlmResult(String content, String model, int promptTokens, int completionTokens) {
        this.content = content;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
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

    /** 输入 + 输出 Token 合计，用于成本粗算 */
    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }
}
