package com.erp.ai.rag;

import java.util.List;

/**
 * 检索门控决策结果：封装强度、命中列表、原因文案与最高分。
 * <p>
 * <b>职责</b>：把 {@link RetrievalGate} 的判定结果固化为一份不可变快照，
 * 供编排层分支（跳过 LLM / 弱提示词 / 正常生成）与 API 可观测字段回传。
 * <p>
 * <b>RAG 流水线位置</b>：{@code gate} 的输出对象；下游为 {@code prompt} 与 {@code generate}。
 * <p>
 * <b>对应 Day</b>：Day13。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>由 {@link RetrievalGate} 工厂方法创建；</li>
 *   <li>被 {@link com.erp.ai.rag.service.RagService} 读取 {@link #getStrength()} / {@link #getHits()}；</li>
 *   <li>{@link #hits} 元素类型为 {@link RetrievedChunk}。</li>
 * </ul>
 */
public class GateDecision {

    /** 门控强度：EMPTY / WEAK / STRONG */
    private final GateStrength strength;

    /** 进入门控后的命中列表（不可变副本）；EMPTY 时为空列表 */
    private final List<RetrievedChunk> hits;

    /** 人类可读原因，例如弱命中时的分值对比说明 */
    private final String reason;

    /** 最高相关分；空命中为 {@code 0} */
    private final double topScore;

    /**
     * 私有构造：统一做 hits 空安全与不可变拷贝。
     *
     * @param strength 门控强度，不可为 {@code null}（由工厂保证）
     * @param hits     命中列表，{@code null} 视为空
     * @param reason   决策原因文案
     * @param topScore 最高分快照
     */
    private GateDecision(GateStrength strength, List<RetrievedChunk> hits, String reason, double topScore) {
        this.strength = strength;
        this.hits = hits == null ? List.of() : List.copyOf(hits);
        this.reason = reason;
        this.topScore = topScore;
    }

    /**
     * 构造空命中决策。
     * <p>
     * 边界：无检索结果时使用；{@code topScore=0}，hits 为空。
     *
     * @return {@link GateStrength#EMPTY} 决策实例
     */
    public static GateDecision empty() {
        return new GateDecision(GateStrength.EMPTY, List.of(), "no retrieved chunks", 0);
    }

    /**
     * 构造弱命中决策。
     * <p>
     * 边界：{@code hits} 为空时 {@code topScore} 仍记为 0（防御性；正常路径弱命中应非空）。
     *
     * @param hits   原始检索命中（通常已按分数降序）
     * @param reason 弱命中原因（如 top 分低于 minScore）
     * @return {@link GateStrength#WEAK} 决策实例
     */
    public static GateDecision weak(List<RetrievedChunk> hits, String reason) {
        double top = hits.isEmpty() ? 0 : hits.getFirst().getScore();
        return new GateDecision(GateStrength.WEAK, hits, reason, top);
    }

    /**
     * 构造强命中决策。
     * <p>
     * 边界：若误传入空 hits，则 topScore 为 0，但 strength 仍为 STRONG（调用方应保证非空）。
     *
     * @param hits 原始检索命中（通常已按分数降序）
     * @return {@link GateStrength#STRONG} 决策实例，原因为固定英文说明
     */
    public static GateDecision strong(List<RetrievedChunk> hits) {
        double top = hits.isEmpty() ? 0 : hits.getFirst().getScore();
        return new GateDecision(GateStrength.STRONG, hits, "top score above threshold", top);
    }

    /**
     * @return 门控强度枚举
     */
    public GateStrength getStrength() {
        return strength;
    }

    /**
     * @return 不可变命中列表；空命中时为空列表而非 {@code null}
     */
    public List<RetrievedChunk> getHits() {
        return hits;
    }

    /**
     * @return 决策原因文案（可写入响应 {@code gateReason}）
     */
    public String getReason() {
        return reason;
    }

    /**
     * @return 最高相关分快照
     */
    public double getTopScore() {
        return topScore;
    }

    /**
     * @return 是否为空命中（{@link GateStrength#EMPTY}）
     */
    public boolean isEmpty() {
        return strength == GateStrength.EMPTY;
    }

    /**
     * @return 是否为弱命中（{@link GateStrength#WEAK}）
     */
    public boolean isWeak() {
        return strength == GateStrength.WEAK;
    }
}
