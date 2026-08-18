package com.erp.ai.rag;

import com.erp.ai.common.client.EmbeddingClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量检索器：问题 Embedding 与内存向量索引做余弦相似度 TopK。
 * <p>
 * <b>职责</b>：语义召回——即使用词不完全重合，也能靠向量空间邻近命中相关章节。
 * <p>
 * <b>RAG 流水线位置</b>：{@code retrieve}（依赖启动期已建好的向量索引）。
 * <p>
 * <b>对应 Day</b>：Day11。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>读 {@link VectorCorpusIndex#allIndexed()}；</li>
 *   <li>用 {@link EmbeddingClient} 嵌入问题、用 {@link CosineSimilarity} 打分；</li>
 *   <li>实现 {@link RagRetriever}；被 {@link HybridRetriever} 与 {@link com.erp.ai.rag.service.RagService} 使用。</li>
 * </ul>
 * <p>
 * <b>注意</b>：方法参数 {@code corpus} 在本实现中被忽略，始终以向量索引为准。
 */
@Component
public class VectorRetriever implements RagRetriever {

    /** 问题侧 Embedding 客户端 */
    private final EmbeddingClient embeddingClient;

    /** 启动期构建的 chunk→向量 内存索引 */
    private final VectorCorpusIndex vectorCorpusIndex;

    /**
     * @param embeddingClient    Embedding 提供方
     * @param vectorCorpusIndex  向量语料索引
     */
    public VectorRetriever(EmbeddingClient embeddingClient, VectorCorpusIndex vectorCorpusIndex) {
        this.embeddingClient = embeddingClient;
        this.vectorCorpusIndex = vectorCorpusIndex;
    }

    /**
     * 向量相似度检索。
     * <p>
     * <b>算法步骤</b>：
     * <ol>
     *   <li>问题空或 {@code topK<=0} → 空列表；</li>
     *   <li>索引为空（空语料未建向量）→ 空列表；</li>
     *   <li>对问题做 Embedding 得到查询向量；</li>
     *   <li>与每条 {@link IndexedChunk} 计算余弦；</li>
     *   <li>{@code score <= 0} 丢弃（正交/反向/零向量）；</li>
     *   <li>按分数降序、id 升序取 TopK。</li>
     * </ol>
     * <p>
     * 低正分仍可能返回 → Gate 可判 WEAK；全丢弃 → EMPTY。
     *
     * @param question 用户问题
     * @param corpus   文本语料（本实现忽略，保留接口兼容）
     * @param topK     返回上限
     * @return 余弦分降序的命中列表；无正分则为空
     */
    @Override
    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        // 步骤 1：非法输入直接空命中
        if (question == null || question.isBlank() || topK <= 0) {
            return List.of();
        }
        // 步骤 2：空索引（例如教材目录为空）→ 空命中
        List<IndexedChunk> indexed = vectorCorpusIndex.allIndexed();
        if (indexed.isEmpty()) {
            return List.of();
        }

        // 步骤 3：问题向量化（须与索引 Embedding 同模型/同维）
        float[] query = embeddingClient.embed(question);
        List<RetrievedChunk> scored = new ArrayList<>();
        for (IndexedChunk item : indexed) {
            // 步骤 4：余弦相似度打分
            double score = CosineSimilarity.cosine(query, item.getVector());
            // 步骤 5：非正分视为无关（含零向量保护返回的 0）
            if (score <= 0) {
                continue;
            }
            scored.add(new RetrievedChunk(item.getChunk(), score));
        }

        // 步骤 6：稳定排序截断
        return scored.stream()
                .sorted(Comparator.comparingDouble(RetrievedChunk::getScore).reversed()
                        .thenComparing(r -> r.getChunk().getId()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * @return 固定名 {@code vector}
     */
    @Override
    public String name() {
        return "vector";
    }
}
