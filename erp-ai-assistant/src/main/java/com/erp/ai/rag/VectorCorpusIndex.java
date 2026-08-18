package com.erp.ai.rag;

import com.erp.ai.common.client.EmbeddingClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 启动时对教材 chunk 做 Embedding，得到内存向量索引（Day11 索引阶段）。
 */
@Component
public class VectorCorpusIndex {

    private static final Logger log = LoggerFactory.getLogger(VectorCorpusIndex.class);

    private final RagCorpusIndex corpusIndex;
    private final EmbeddingClient embeddingClient;

    private List<IndexedChunk> indexed = List.of();

    public VectorCorpusIndex(RagCorpusIndex corpusIndex, EmbeddingClient embeddingClient) {
        this.corpusIndex = corpusIndex;
        this.embeddingClient = embeddingClient;
    }

    @PostConstruct
    public void rebuild() {
        List<TextChunk> chunks = corpusIndex.allChunks();
        List<IndexedChunk> built = new ArrayList<>(chunks.size());
        for (TextChunk chunk : chunks) {
            float[] vector = embeddingClient.embed(chunk.searchableText());
            built.add(new IndexedChunk(chunk, vector));
        }
        this.indexed = Collections.unmodifiableList(built);
        int dims = indexed.isEmpty() ? 0 : indexed.getFirst().getVector().length;
        log.info("Vector corpus ready: chunks={}, dims={}, embedding={}",
                indexed.size(), dims, embeddingClient.providerName());
    }

    public List<IndexedChunk> allIndexed() {
        return indexed;
    }
}
