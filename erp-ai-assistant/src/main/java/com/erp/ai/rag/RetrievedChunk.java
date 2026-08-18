package com.erp.ai.rag;

/**
 * 单次检索命中：文本块 + 相关分数。
 * <p>
 * <b>职责</b>：作为各 {@link RagRetriever} 的统一返回元素，并进入 Gate / Prompt / sources。
 * <p>
 * <b>RAG 流水线位置</b>：{@code retrieve} 输出；被 {@code gate} 与 {@code prompt} 消费。
 * <p>
 * <b>对应 Day</b>：Day11/12/13。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>包装 {@link TextChunk}；</li>
 *   <li>分数含义依赖检索器：keyword 为词项重叠比，vector 为余弦，hybrid 为 RRF 累加分；</li>
 *   <li>{@link RetrievalGate} 读取首条 score 与 {@code minScore} 比较。</li>
 * </ul>
 */
public class RetrievedChunk {

    /** 命中的教材文本块 */
    private final TextChunk chunk;

    /** 相关分数（量纲随检索器变化；越大通常表示越相关） */
    private final double score;

    /**
     * @param chunk 命中块，调用方应保证非 {@code null}
     * @param score 相关分数
     */
    public RetrievedChunk(TextChunk chunk, double score) {
        this.chunk = chunk;
        this.score = score;
    }

    /**
     * @return 命中的 {@link TextChunk}
     */
    public TextChunk getChunk() {
        return chunk;
    }

    /**
     * @return 相关分数
     */
    public double getScore() {
        return score;
    }
}
