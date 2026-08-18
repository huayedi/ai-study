package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 检索门控器：根据命中列表与最低分阈值判定 EMPTY / WEAK / STRONG。
 * <p>
 * <b>职责</b>：在 retrieve 与 prompt 之间做质量分流，避免低相关资料被当成强证据。
 * <p>
 * <b>RAG 流水线位置</b>：{@code gate}。
 * <p>
 * <b>对应 Day</b>：Day13。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>输入为各 {@link RagRetriever} 的 {@link RetrievedChunk} 列表；</li>
 *   <li>输出 {@link GateDecision} 供 {@link com.erp.ai.rag.service.RagService} 编排；</li>
 *   <li>阈值 {@code minScore} 来自配置，量纲依赖当前检索器（keyword 重叠比 / 余弦 / RRF 分不同）。</li>
 * </ul>
 * <p>
 * <b>重要</b>：keyword 分、向量余弦、RRF 融合分量纲不同；{@code minScore} 需按当前 retriever 理解与标定。
 */
@Component
public class RetrievalGate {

    /**
     * 对检索命中做门控判定。
     * <p>
     * <b>算法步骤</b>：
     * <ol>
     *   <li>若 {@code hits} 为 {@code null} 或空 → {@link GateDecision#empty()}（空命中）；</li>
     *   <li>取首条分数为 top（约定检索器已按分数降序）；</li>
     *   <li>若 {@code top < minScore} → {@link GateDecision#weak}（弱命中）；</li>
     *   <li>否则 → {@link GateDecision#strong}（强命中）。</li>
     * </ol>
     * <p>
     * <b>边界</b>：
     * <ul>
     *   <li>空命中：无片段可引用；</li>
     *   <li>弱命中：有片段但证据不足；</li>
     *   <li>强命中：top 达到阈值（相等算强：使用严格小于判断）。</li>
     * </ul>
     *
     * @param hits     检索结果，期望已按 score 降序；可为 {@code null}
     * @param minScore 最低可接受最高分阈值（与当前检索器分数量纲一致）
     * @return 不可变的 {@link GateDecision}
     */
    public GateDecision decide(List<RetrievedChunk> hits, double minScore) {
        // 步骤 1：空列表 / null → 空命中（与「全零分过滤后无结果」同属 EMPTY）
        if (hits == null || hits.isEmpty()) {
            return GateDecision.empty();
        }
        // 步骤 2：约定第一名为最高分
        double top = hits.getFirst().getScore();
        // 步骤 3：低于阈值 → 弱命中（仍保留 hits 供谨慎提示）
        if (top < minScore) {
            return GateDecision.weak(hits, "top score " + format(top) + " below minScore " + format(minScore));
        }
        // 步骤 4：达到或超过阈值 → 强命中
        return GateDecision.strong(hits);
    }

    /**
     * 将分值格式化为四位小数，便于日志与 {@code gateReason} 阅读。
     *
     * @param v 原始 double 分数
     * @return 形如 {@code 0.1234} 的字符串
     */
    private static String format(double v) {
        return String.format("%.4f", v);
    }
}
