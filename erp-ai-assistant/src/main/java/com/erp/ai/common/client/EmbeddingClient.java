package com.erp.ai.common.client;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本向量化（Embedding）抽象接口（Day11 RAG 学习版）。
 *
 * <h2>职责</h2>
 * 将任意文本映射为定长 {@code float[]} 向量，供向量检索 / Hybrid 融合使用。
 *
 * <h2>为何抽象</h2>
 * <ul>
 *   <li>学习期可用 {@code HashEmbeddingClient}（无远程依赖）</li>
 *   <li>有兼容网关时切换 {@code OpenAiCompatibleEmbeddingClient}</li>
 *   <li><b>索引与查询必须使用同一实现与同一模型/维度</b>，否则余弦相似度无意义</li>
 * </ul>
 *
 * <h2>与 Spring / 配置的关系</h2>
 * Bean 由 {@code EmbeddingClientConfig} 根据 {@code ai.rag.embedding-provider} 创建。
 * Chat 的 LLM 与 Embedding 可分别配置（例如 Chat 走 DeepSeek，Embedding 走 hash）。
 *
 * <h2>学习要点</h2>
 * Embedding 不是「更聪明的关键词」；维度、归一化、模型版本都会影响召回质量。
 */
public interface EmbeddingClient {

    /**
     * 将单段文本编码为向量。
     * <p>
     * <b>边界：</b>{@code text} 为 {@code null} 或空白时，实现可返回全零向量或空语义向量，
     * 但不应抛出 NPE；具体语义以实现类 Javadoc 为准。
     *
     * @param text 待编码文本；允许 {@code null}（由实现处理）
     * @return 定长向量；长度由实现/配置决定（如 hash 的 dimensions）；不会为 {@code null}
     * @throws IllegalStateException 远程 Embedding 调用失败、鉴权失败或响应缺字段时
     * @throws IllegalArgumentException 实现侧配置非法（如维度过小）时可能在构造期已抛出
     */
    float[] embed(String text);

    /**
     * 批量编码：默认逐条调用 {@link #embed(String)}。
     * <p>
     * 真实网关若支持 batch input，子类可覆盖以减少 HTTP 往返；本默认实现保证语义正确即可。
     *
     * @param texts 文本列表；不应为 {@code null}；元素允许为 {@code null}（交给 {@link #embed}）
     * @return 与入参等长的向量列表；顺序与 {@code texts} 一一对应
     * @throws IllegalStateException 任一条远程调用失败时（默认实现不吞异常）
     * @throws NullPointerException  若 {@code texts} 本身为 {@code null}
     */
    default List<float[]> embedBatch(List<String> texts) {
        List<float[]> out = new ArrayList<>(texts.size());
        for (String text : texts) {
            out.add(embed(text));
        }
        return out;
    }

    /**
     * 当前 Embedding 实现名称，用于日志与配置核对。
     *
     * @return 例如 {@code hash}、{@code openai-compatible}；不会为 {@code null}
     */
    String providerName();
}
