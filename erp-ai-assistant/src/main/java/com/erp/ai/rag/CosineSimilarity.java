package com.erp.ai.rag;

/**
 * 余弦相似度工具类：计算两个浮点向量的 cosine similarity。
 * <p>
 * <b>职责</b>：提供无状态的纯函数 {@link #cosine(float[], float[])}，
 * 供 {@link VectorRetriever} 在检索阶段对「问题向量」与「教材 chunk 向量」打分。
 * <p>
 * <b>RAG 流水线位置</b>：{@code retrieve}（向量检索打分子步骤）。
 * 不参与 load / chunk / gate / prompt / generate。
 * <p>
 * <b>对应 Day</b>：Day11（向量检索与 Embedding 索引）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>被 {@link VectorRetriever} 调用，对 {@link IndexedChunk#getVector()} 与查询向量比较；</li>
 *   <li>间接服务于 {@link HybridRetriever}（其 vector 支路依赖本类分数再做 RRF）。</li>
 * </ul>
 * <p>
 * 公式：{@code cosine(a,b) = dot(a,b) / (||a|| * ||b||)}。
 * 零向量或近零范数时返回 {@code 0}（避免除零，并视为无相关性）。
 */
public final class CosineSimilarity {

    /**
     * 工具类禁止实例化。
     */
    private CosineSimilarity() {
    }

    /**
     * 计算两个同维向量的余弦相似度。
     * <p>
     * <b>算法步骤</b>：
     * <ol>
     *   <li>校验 {@code a}、{@code b} 非 null；</li>
     *   <li>校验长度一致（Embedding 维度必须匹配）；</li>
     *   <li>单次遍历累加点积 {@code dot}、以及各自平方和 {@code normA}/{@code normB}；</li>
     *   <li>若任一方范数平方 ≤ {@code 1e-12}（近零向量），返回 {@code 0}；</li>
     *   <li>否则返回 {@code dot / (sqrt(normA) * sqrt(normB))}，范围约 {@code [-1, 1]}（常见 Embedding 多为非负）。</li>
     * </ol>
     * <p>
     * <b>边界</b>：零向量 / 维度不匹配会抛异常或返回 0，由调用方决定是否过滤低分命中。
     *
     * @param a 查询向量（问题 Embedding），不可为 {@code null}
     * @param b 文档向量（chunk Embedding），不可为 {@code null}，长度须与 {@code a} 相同
     * @return 余弦相似度；近零范数时返回 {@code 0.0}
     * @throws IllegalArgumentException 当向量为 {@code null} 或长度不一致时
     */
    public static double cosine(float[] a, float[] b) {
        // 步骤 1：空指针防护——相似度无定义，直接失败
        if (a == null || b == null) {
            throw new IllegalArgumentException("vectors must not be null");
        }
        // 步骤 2：维度必须一致，否则点积无意义
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "vector length mismatch: " + a.length + " vs " + b.length);
        }
        // 步骤 3：累加点积与双方 L2 范数的平方（用 double 减轻 float 累加误差）
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            // 点积分量 a_i * b_i
            dot += (double) a[i] * b[i];
            // ||a||^2 分量
            normA += (double) a[i] * a[i];
            // ||b||^2 分量
            normB += (double) b[i] * b[i];
        }
        // 步骤 4：近零范数视为无方向，返回 0（避免除零；VectorRetriever 会继续过滤 score<=0）
        if (normA <= 1e-12 || normB <= 1e-12) {
            return 0;
        }
        // 步骤 5：标准余弦 = 点积 / (两范数之积)
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
