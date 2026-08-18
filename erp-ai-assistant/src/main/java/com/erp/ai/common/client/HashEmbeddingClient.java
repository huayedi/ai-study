package com.erp.ai.common.client;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 学习替身 Embedding：无远程 /embeddings 时也能跑通向量检索链路。
 * <p>
 * DeepSeek 当前仅提供 Chat Completions、不提供 Embeddings；
 * Chat 仍走 DeepSeek（openai-compatible），向量侧默认可用本类。
 * <p>
 * 语义区分力弱，仅用于结构练习；有真 embeddings 网关时切换
 * {@code ai.rag.embedding-provider=openai-compatible}。
 */
public class HashEmbeddingClient implements EmbeddingClient {

    private static final Pattern SPLIT = Pattern.compile("[^\\p{IsHan}A-Za-z0-9]+");

    private final int dimensions;

    public HashEmbeddingClient(int dimensions) {
        if (dimensions < 8) {
            throw new IllegalArgumentException("dimensions must be >= 8");
        }
        this.dimensions = dimensions;
    }

    @Override
    public float[] embed(String text) {
        float[] vector = new float[dimensions];
        if (text == null || text.isBlank()) {
            return vector;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
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
        String hanOnly = normalized.replaceAll("[^\\p{IsHan}]", "");
        for (int i = 0; i + 1 < hanOnly.length(); i++) {
            addToken(vector, hanOnly.substring(i, i + 2));
        }
        normalize(vector);
        return vector;
    }

    private void addToken(float[] vector, String token) {
        int h = murmurish(token.getBytes(StandardCharsets.UTF_8));
        int idx = Math.floorMod(h, dimensions);
        vector[idx] += 1.0f;
        int idx2 = Math.floorMod(h >>> 16, dimensions);
        vector[idx2] += 0.5f;
    }

    private static int murmurish(byte[] data) {
        int h = 0x9747b28c;
        for (byte b : data) {
            h ^= b;
            h *= 0x5bd1e995;
            h ^= h >>> 15;
        }
        return h;
    }

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

    @Override
    public String providerName() {
        return "hash";
    }
}
