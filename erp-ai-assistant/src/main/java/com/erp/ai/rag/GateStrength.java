package com.erp.ai.rag;

/**
 * 检索门控强度枚举：描述一次检索结果相对阈值的质量档位。
 * <p>
 * <b>职责</b>：作为 {@link GateDecision} 的核心标签，驱动
 * {@link com.erp.ai.rag.service.RagService} 是否跳过 LLM、是否使用弱命中提示词、
 * 以及是否强制 {@code need_human}。
 * <p>
 * <b>RAG 流水线位置</b>：{@code gate}（retrieve 之后、prompt/generate 之前）。
 * <p>
 * <b>对应 Day</b>：Day13（Retrieval Gate）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>由 {@link RetrievalGate#decide} 产出；</li>
 *   <li>被 {@link RagPromptBuilder#userPrompt} / {@code systemPromptWeak} 消费；</li>
 *   <li>序列化到 {@link com.erp.ai.rag.dto.RagAskResponse#getGate()}（枚举名字符串）。</li>
 * </ul>
 */
public enum GateStrength {
    /**
     * 空命中：检索列表为空（无任何 chunk）。
     * <p>
     * 典型原因：空语料、问题无法分词、所有 chunk 重叠/相似度为 0。
     * 默认策略可跳过 LLM，且 {@code sources} 必须为空，禁止伪造引用。
     */
    EMPTY,

    /**
     * 弱命中：有命中，但最高分低于 {@code minScore} 阈值。
     * <p>
     * 仍可能把片段送入提示词，但必须承认依据不足，并强制人工复核（{@code need_human=true}）。
     */
    WEAK,

    /**
     * 强命中：最高分达到或超过阈值，可按正常 RAG 路径生成回答。
     */
    STRONG
}
