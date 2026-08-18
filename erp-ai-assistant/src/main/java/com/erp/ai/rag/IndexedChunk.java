package com.erp.ai.rag;

/**
 * 带 Embedding 向量的索引条目（向量语料原子单位）。
 * <p>
 * <b>职责</b>：把 {@link TextChunk} 与其稠密向量绑定，供 {@link VectorRetriever} 做余弦打分。
 * <p>
 * <b>RAG 流水线位置</b>：索引阶段产物（load/chunk 之后、retrieve 之前）；
 * 由 {@link VectorCorpusIndex} 在启动时构建。
 * <p>
 * <b>对应 Day</b>：Day11（索引阶段）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>由 {@link VectorCorpusIndex#rebuild()} 创建；</li>
 *   <li>向量来自 {@code EmbeddingClient}；</li>
 *   <li>检索时与查询向量经 {@link CosineSimilarity} 比较。</li>
 * </ul>
 */
public class IndexedChunk {

    /** 原始文本块（含 id/docId/section/content） */
    private final TextChunk chunk;

    /** 该块 searchableText 的 Embedding 向量；维度由 Embedding 提供方决定 */
    private final float[] vector;

    /**
     * @param chunk  文本块，应非 {@code null}
     * @param vector Embedding 向量，应非 {@code null} 且维度固定
     */
    public IndexedChunk(TextChunk chunk, float[] vector) {
        this.chunk = chunk;
        this.vector = vector;
    }

    /**
     * @return 绑定的 {@link TextChunk}
     */
    public TextChunk getChunk() {
        return chunk;
    }

    /**
     * @return Embedding 向量（调用方勿随意修改数组内容，以免污染索引）
     */
    public float[] getVector() {
        return vector;
    }
}
