package com.erp.ai.rag;

import com.erp.ai.common.config.AiProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 混合检索器：关键词召回 + 向量召回，再经 Reciprocal Rank Fusion（RRF）融合排序。
 * <p>
 * <b>职责</b>：在词面匹配与语义匹配之间取长补短，降低单一检索器漏召回。
 * <p>
 * <b>RAG 流水线位置</b>：{@code retrieve}。
 * <p>
 * <b>对应 Day</b>：Day13（hybrid + RRF）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>委托 {@link KeywordRetriever} 与 {@link VectorRetriever} 做宽召回；</li>
 *   <li>配置项 {@code recallK}/{@code rrfK} 来自 {@link AiProperties}；</li>
 *   <li>实现 {@link RagRetriever}，由 {@link com.erp.ai.rag.service.RagService} 按配置选用。</li>
 * </ul>
 * <p>
 * <b>RRF 公式</b>：对每个列表中排名 {@code i}（1-based）的文档累加 {@code 1/(rrfK + i)}；
 * 同分文档跨列表相加。最终分数量纲与原始 keyword/余弦不同，Gate 的 {@code minScore} 需单独标定。
 */
@Component
public class HybridRetriever implements RagRetriever {

    /** 关键词支路 */
    private final KeywordRetriever keyword;

    /** 向量支路 */
    private final VectorRetriever vector;

    /** 读取 recallK、rrfK 等 RAG 配置 */
    private final AiProperties properties;

    /**
     * @param keyword    关键词检索器
     * @param vector     向量检索器
     * @param properties AI/RAG 配置
     */
    public HybridRetriever(KeywordRetriever keyword, VectorRetriever vector, AiProperties properties) {
        this.keyword = keyword;
        this.vector = vector;
        this.properties = properties;
    }

    /**
     * 混合检索 + RRF 融合。
     * <p>
     * <b>算法步骤</b>：
     * <ol>
     *   <li>问题空或 {@code topK<=0} → 空列表；</li>
     *   <li>计算宽召回条数 {@code recall = max(topK*3, recallK)}；</li>
     *   <li>分别对 keyword、vector 取 {@code recall} 条；</li>
     *   <li>对两路结果调用 {@link #addRrf} 累加 RRF 分并记录 chunk；</li>
     *   <li>按融合分降序、id 升序截断为 {@code topK}。</li>
     * </ol>
     * <p>
     * <b>边界</b>：任一路空列表不影响另一路；两路皆空 → 空命中；
     * 仅低分弱相关时仍可能产出列表 → Gate 判 WEAK。
     *
     * @param question 用户问题
     * @param corpus   文本语料（传给 keyword；vector 侧仍读自有索引）
     * @param topK     最终返回条数
     * @return RRF 融合后的 TopK 命中
     */
    @Override
    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        // 步骤 1：非法输入 → 空命中
        if (question == null || question.isBlank() || topK <= 0) {
            return List.of();
        }
        // 步骤 2：宽召回规模——至少 topK*3，且不低于配置 recallK
        int recall = Math.max(topK * 3, Math.max(1, properties.getRag().getRecallK()));
        // RRF 平滑常数 k，至少为 1
        int rrfK = Math.max(1, properties.getRag().getRrfK());

        // 步骤 3：两路独立召回（分数量纲不同，故后面只用名次做 RRF）
        List<RetrievedChunk> kw = keyword.retrieve(question, corpus, recall);
        List<RetrievedChunk> vec = vector.retrieve(question, corpus, recall);

        // chunkId → 累加 RRF 分；chunkId → 文本块对象
        Map<String, Double> score = new HashMap<>();
        Map<String, TextChunk> byId = new HashMap<>();

        // 步骤 4：两路按排名注入 RRF 分
        addRrf(kw, score, byId, rrfK);
        addRrf(vec, score, byId, rrfK);

        // 步骤 5：融合排序截断
        return score.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(topK)
                .map(e -> new RetrievedChunk(byId.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 将一路有序列表按 Reciprocal Rank Fusion 注入分数表。
     * <p>
     * 对排名位置 {@code i}（0-based），贡献 {@code 1.0 / (rrfK + (i+1))}，
     * 同一 id 多次出现（跨列表）用 {@link Map#merge} 相加。
     *
     * @param list  单路检索结果（已按该路自身分数降序）
     * @param score 输出：id → RRF 累加分
     * @param byId  输出：id → {@link TextChunk} 样例（后写入覆盖前者，内容应相同）
     * @param rrfK  RRF 平滑常数
     */
    private static void addRrf(List<RetrievedChunk> list,
                               Map<String, Double> score,
                               Map<String, TextChunk> byId,
                               int rrfK) {
        for (int i = 0; i < list.size(); i++) {
            RetrievedChunk rc = list.get(i);
            String id = rc.getChunk().getId();
            // 记录文本块，供最终组装 RetrievedChunk
            byId.put(id, rc.getChunk());
            // 名次从 1 开始：第 1 名贡献 1/(rrfK+1)，第 2 名 1/(rrfK+2)，…
            double add = 1.0 / (rrfK + (i + 1));
            score.merge(id, add, Double::sum);
        }
    }

    /**
     * @return 固定名 {@code hybrid}
     */
    @Override
    public String name() {
        return "hybrid";
    }
}
