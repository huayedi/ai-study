package com.erp.ai.rag;

import com.erp.ai.common.client.EmbeddingClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 内存向量语料索引：启动时对每个教材 chunk 做 Embedding。
 * <p>
 * <b>职责</b>：把 {@link RagCorpusIndex} 的文本块升级为 {@link IndexedChunk} 列表，
 * 供 {@link VectorRetriever} O(n) 扫描打分（学习版暴力检索）。
 * <p>
 * <b>RAG 流水线位置</b>：chunk 之后的索引阶段；支撑后续 {@code retrieve}（vector/hybrid）。
 * <p>
 * <b>对应 Day</b>：Day11 索引阶段。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>依赖 {@link RagCorpusIndex}（须先于本类完成文本索引）；</li>
 *   <li>依赖 {@link EmbeddingClient}；</li>
 *   <li>被 {@link VectorRetriever} 只读消费。</li>
 * </ul>
 * <p>
 * <b>空语料</b>：文本 chunks 为空时 indexed 亦为空，向量检索返回空列表。
 */
@Component
public class VectorCorpusIndex {

    /** 向量索引构建日志 */
    private static final Logger log = LoggerFactory.getLogger(VectorCorpusIndex.class);

    /** 文本语料索引（提供待嵌入的 chunks） */
    private final RagCorpusIndex corpusIndex;

    /** Embedding 客户端 */
    private final EmbeddingClient embeddingClient;

    /** 带向量的索引条目（不可变）；默认空 */
    private List<IndexedChunk> indexed = List.of();

    /**
     * @param corpusIndex     文本语料索引
     * @param embeddingClient Embedding 提供方
     */
    public VectorCorpusIndex(RagCorpusIndex corpusIndex, EmbeddingClient embeddingClient) {
        this.corpusIndex = corpusIndex;
        this.embeddingClient = embeddingClient;
    }

    /**
     * 启动后对全部 chunk 的 {@link TextChunk#searchableText()} 做 Embedding 并固化。
     * <p>
     * 空语料时 dims 记为 0；不抛业务异常。
     */
    @PostConstruct
    public void rebuild() {
        List<TextChunk> chunks = corpusIndex.allChunks();
        List<IndexedChunk> built = new ArrayList<>(chunks.size());
        for (TextChunk chunk : chunks) {
            // 对「章节+正文」嵌入，与检索时问题向量同一空间
            float[] vector = embeddingClient.embed(chunk.searchableText());
            built.add(new IndexedChunk(chunk, vector));
        }
        this.indexed = Collections.unmodifiableList(built);
        int dims = indexed.isEmpty() ? 0 : indexed.getFirst().getVector().length;
        log.info("Vector corpus ready: chunks={}, dims={}, embedding={}",
                indexed.size(), dims, embeddingClient.providerName());
    }

    /**
     * @return 不可变的向量索引列表；空语料时为空
     */
    public List<IndexedChunk> allIndexed() {
        return indexed;
    }
}
