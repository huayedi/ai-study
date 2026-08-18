package com.erp.ai.common.client;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本 → 向量（Day11）。索引与查询必须用同一实现/同一模型。
 */
public interface EmbeddingClient {

    float[] embed(String text);

    default List<float[]> embedBatch(List<String> texts) {
        List<float[]> out = new ArrayList<>(texts.size());
        for (String text : texts) {
            out.add(embed(text));
        }
        return out;
    }

    String providerName();
}
