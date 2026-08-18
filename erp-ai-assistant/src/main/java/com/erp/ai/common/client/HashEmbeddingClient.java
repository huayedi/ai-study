package com.erp.ai.common.client;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 学习替身 Embedding：无远程 {@code /embeddings} 时也能跑通向量检索链路。
 *
 * <h2>职责</h2>
 * 用本地哈希把 token / 汉字 bigram 投射到固定维稀疏向量，再 L2 归一化，
 * 使相同词面文本得到稳定、可比较的余弦相似度。
 *
 * <h2>为何需要</h2>
 * DeepSeek 当前仅提供 Chat Completions、不提供 Embeddings；
 * Chat 仍可走 DeepSeek（openai-compatible），向量侧默认用本类。
 * <p>
 * <b>语义区分力弱</b>，仅用于结构练习；有真 embeddings 网关时切换
 * {@code ai.rag.embedding-provider=openai-compatible}。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * {@code EmbeddingClientConfig} 在 provider=hash|mock 时
 * {@code new HashEmbeddingClient(ai.rag.embedding-dimensions)}。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>索引与查询必须同一 dimensions</li>
 *   <li>归一化后适合用点积当余弦</li>
 *   <li>不是语义模型：同义词几乎无重叠</li>
 * </ul>
 */
public class HashEmbeddingClient implements EmbeddingClient {

    /**
     * 分词：非汉字、非字母数字的字符视为分隔符。
     * 例：{@code "库存 A001"} → {@code ["库存", "a001"]}（先 lower）
     */
    private static final Pattern SPLIT = Pattern.compile("[^\\p{IsHan}A-Za-z0-9]+");

    /** 向量维度；构造时强制 {@code >= 8} */
    private final int dimensions;

    /**
     * @param dimensions 输出向量长度，必须 {@code >= 8}
     * @throws IllegalArgumentException 维度过小时
     */
    public HashEmbeddingClient(int dimensions) {
        if (dimensions < 8) {
            throw new IllegalArgumentException("dimensions must be >= 8");
        }
        this.dimensions = dimensions;
    }

    /**
     * 本地哈希编码算法。
     * <p>
     * <b>详细步骤：</b>
     * <ol>
     *   <li>分配全零 {@code float[dimensions]}；空文本直接返回（零向量，归一化后仍为零）</li>
     *   <li>整体 lower-case，按 {@link #SPLIT} 切开 token</li>
     *   <li>每个 token 调用 {@link #addToken} 做双重哈希累加</li>
     *   <li>若 token 全是汉字且长度≥2，再对其滑动 bigram 累加（提高中文重叠）</li>
     *   <li>另取全文纯汉字串，再扫一遍 bigram（跨原分隔符的字对也能命中）</li>
     *   <li>{@link #normalize} 做 L2 归一化，便于余弦=点积</li>
     * </ol>
     *
     * @param text 原文；{@code null}/空白 → 零向量
     * @return 长度为 {@link #dimensions} 的向量；不会为 {@code null}
     */
    @Override
    public float[] embed(String text) {
        float[] vector = new float[dimensions];
        if (text == null || text.isBlank()) {
            return vector;
        }
        // 步骤 2：归一化大小写
        String normalized = text.toLowerCase(Locale.ROOT);
        // 步骤 3–4：token + 汉字 bigram
        for (String token : SPLIT.split(normalized)) {
            if (token == null || token.isBlank()) {
                continue;
            }
            addToken(vector, token);
            // 中文 bigram，与 KeywordRetriever 类似，略增重叠可分性
            if (token.codePoints().allMatch(Character::isIdeographic) && token.length() >= 2) {
                for (int i = 0; i + 1 < token.length(); i++) {
                    addToken(vector, token.substring(i, i + 2));
                }
            }
        }
        // 步骤 5：跨分隔符的纯汉字 bigram
        String hanOnly = normalized.replaceAll("[^\\p{IsHan}]", "");
        for (int i = 0; i + 1 < hanOnly.length(); i++) {
            addToken(vector, hanOnly.substring(i, i + 2));
        }
        // 步骤 6：单位化
        normalize(vector);
        return vector;
    }

    /**
     * 将一个 token 的哈希投射到两个桶并加权累加（feature hashing 简化版）。
     * <p>
     * 主桶 +1.0，次桶（高 16 位再取模）+0.5，减轻单哈希碰撞导致的特征丢失。
     *
     * @param vector 累加目标（原地修改）
     * @param token  非空特征串
     */
    private void addToken(float[] vector, String token) {
        int h = murmurish(token.getBytes(StandardCharsets.UTF_8));
        int idx = Math.floorMod(h, dimensions);
        vector[idx] += 1.0f;
        int idx2 = Math.floorMod(h >>> 16, dimensions);
        vector[idx2] += 0.5f;
    }

    /**
     * 轻量非密码学哈希（Murmur 风格混合），仅用于学习版分桶。
     * <p>
     * 逐步：异或字节 → 乘常数 → 右移混合，循环整个 UTF-8 字节序列。
     *
     * @param data UTF-8 字节
     * @return 32 位有符号哈希
     */
    private static int murmurish(byte[] data) {
        int h = 0x9747b28c;
        for (byte b : data) {
            h ^= b;
            h *= 0x5bd1e995;
            h ^= h >>> 15;
        }
        return h;
    }

    /**
     * L2 归一化：{@code v := v / ||v||}。
     * <p>
     * 若平方和极小（近似零向量），保持原样，避免除零。
     *
     * @param vector 原地修改
     */
    private static void normalize(float[] vector) {
        double sumSq = 0;
        for (float v : vector) {
            sumSq += (double) v * v;
        }
        if (sumSq <= 1e-12) {
            return;
        }
        float norm = (float) Math.sqrt(sumSq);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code hash}
     */
    @Override
    public String providerName() {
        return "hash";
    }
}
