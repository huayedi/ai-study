package com.erp.ai.rag;

import com.erp.ai.common.client.EmbeddingClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量检索：问题 embed → 与索引余弦相似度 → TopK（Day11）。
 */
@Component
public class VectorRetriever implements RagRetriever {

    private final EmbeddingClient embeddingClient;
    private final VectorCorpusIndex vectorCorpusIndex;

    public VectorRetriever(EmbeddingClient embeddingClient, VectorCorpusIndex vectorCorpusIndex) {
        this.embeddingClient = embeddingClient;
        this.vectorCorpusIndex = vectorCorpusIndex;
    }

    @Override
    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        if (question == null || question.isBlank() || topK <= 0) {
            return List.of();
        }
        List<IndexedChunk> indexed = vectorCorpusIndex.allIndexed();
        if (indexed.isEmpty()) {
            return List.of();
        }

        float[] query = embeddingClient.embed(question);
        List<RetrievedChunk> scored = new ArrayList<>();
        for (IndexedChunk item : indexed) {
            double score = CosineSimilarity.cosine(query, item.getVector());
            if (score <= 0) {
                continue;
            }
            scored.add(new RetrievedChunk(item.getChunk(), score));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(RetrievedChunk::getScore).reversed()
                        .thenComparing(r -> r.getChunk().getId()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "vector";
    }
}
