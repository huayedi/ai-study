package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 学习版检索器：中文友好的极简关键词重叠打分。
 * <p>
 * 后续可替换为向量检索，而不改 {@link RagService} 主流程。
 */
@Component
public class KeywordRetriever {

    private static final Pattern SPLIT = Pattern.compile("[^\\p{IsHan}A-Za-z0-9]+");

    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        Set<String> queryTerms = tokenize(question);
        if (queryTerms.isEmpty() || corpus == null || corpus.isEmpty() || topK <= 0) {
            return List.of();
        }

        List<RetrievedChunk> scored = new ArrayList<>();
        for (TextChunk chunk : corpus) {
            Set<String> docTerms = tokenize(chunk.searchableText());
            int overlap = 0;
            for (String term : queryTerms) {
                if (docTerms.contains(term)) {
                    overlap++;
                }
            }
            if (overlap == 0) {
                continue;
            }
            double score = overlap / (double) queryTerms.size();
            scored.add(new RetrievedChunk(chunk, score));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(RetrievedChunk::getScore).reversed()
                        .thenComparing(r -> r.getChunk().getId()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        Set<String> terms = new HashSet<>(Arrays.asList(SPLIT.split(normalized)));
        terms.removeIf(t -> t == null || t.isBlank() || t.length() == 1);
        // 对连续中文再做 bigram，提高短问句命中率
        String hanOnly = normalized.replaceAll("[^\\p{IsHan}]", "");
        for (int i = 0; i + 1 < hanOnly.length(); i++) {
            terms.add(hanOnly.substring(i, i + 2));
        }
        return terms;
    }
}
