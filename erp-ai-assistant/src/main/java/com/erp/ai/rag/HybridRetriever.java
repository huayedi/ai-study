package com.erp.ai.rag;

import com.erp.ai.common.config.AiProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 混合检索：keyword + vector 召回后做 RRF 融合（Day13）。
 */
@Component
public class HybridRetriever implements RagRetriever {

    private final KeywordRetriever keyword;
    private final VectorRetriever vector;
    private final AiProperties properties;

    public HybridRetriever(KeywordRetriever keyword, VectorRetriever vector, AiProperties properties) {
        this.keyword = keyword;
        this.vector = vector;
        this.properties = properties;
    }

    @Override
    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        if (question == null || question.isBlank() || topK <= 0) {
            return List.of();
        }
        int recall = Math.max(topK * 3, Math.max(1, properties.getRag().getRecallK()));
        int rrfK = Math.max(1, properties.getRag().getRrfK());

        List<RetrievedChunk> kw = keyword.retrieve(question, corpus, recall);
        List<RetrievedChunk> vec = vector.retrieve(question, corpus, recall);

        Map<String, Double> score = new HashMap<>();
        Map<String, TextChunk> byId = new HashMap<>();

        addRrf(kw, score, byId, rrfK);
        addRrf(vec, score, byId, rrfK);

        return score.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(topK)
                .map(e -> new RetrievedChunk(byId.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    private static void addRrf(List<RetrievedChunk> list,
                               Map<String, Double> score,
                               Map<String, TextChunk> byId,
                               int rrfK) {
        for (int i = 0; i < list.size(); i++) {
            RetrievedChunk rc = list.get(i);
            String id = rc.getChunk().getId();
            byId.put(id, rc.getChunk());
            double add = 1.0 / (rrfK + (i + 1));
            score.merge(id, add, Double::sum);
        }
    }

    @Override
    public String name() {
        return "hybrid";
    }
}
