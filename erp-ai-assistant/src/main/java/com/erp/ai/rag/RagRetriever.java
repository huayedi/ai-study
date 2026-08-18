package com.erp.ai.rag;

import java.util.List;

/**
 * RAG 检索器抽象接口：对用户问题从语料中召回 TopK 片段。
 * <p>
 * <b>职责</b>：统一 keyword / vector / hybrid 的调用契约，使
 * {@link com.erp.ai.rag.service.RagService} 只依赖本接口做编排与降级。
 * <p>
 * <b>RAG 流水线位置</b>：{@code retrieve}。
 * <p>
 * <b>对应 Day</b>：Day11/12（可替换检索实现）；Day13 增加 hybrid。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>实现类：{@link KeywordRetriever}、{@link VectorRetriever}、{@link HybridRetriever}；</li>
 *   <li>输入语料多为 {@link RagCorpusIndex#allChunks()}（向量实现可忽略 corpus 改读自有索引）；</li>
 *   <li>输出进入 {@link RetrievalGate}。</li>
 * </ul>
 */
public interface RagRetriever {

    /**
     * 对问题检索最相关的教材块。
     * <p>
     * <b>边界约定（各实现应遵守）</b>：
     * <ul>
     *   <li>问题为空/空白、{@code topK<=0}、空语料 → 返回空列表（空命中原料）；</li>
     *   <li>无正分重叠/相似度 → 空列表或更短列表（可能导致 Gate EMPTY）；</li>
     *   <li>弱相关低分结果仍可返回，由 Gate 判 WEAK。</li>
     * </ul>
     *
     * @param question 用户问题；空/空白时实现应返回空列表
     * @param corpus   切分后的教材块（关键词检索使用；向量检索可忽略，改读自有索引）
     * @param topK     最多返回条数；≤0 时返回空列表
     * @return 按相关分降序的命中列表；无命中时为空列表（非 {@code null}）
     */
    List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK);

    /**
     * 检索器稳定名称，供日志、响应字段 {@code retriever} 与降级判断使用。
     *
     * @return 如 {@code keyword}、{@code vector}、{@code hybrid}
     */
    String name();
}
