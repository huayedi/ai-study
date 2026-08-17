package com.erp.ai.rag;

import com.erp.ai.client.HashEmbeddingClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorRetrieverTest {

    @Test
    void retrievesSemanticallyRelatedChunkWithHashEmbedding() {
        HashEmbeddingClient embedding = new HashEmbeddingClient(128);
        TextChunk purchase = new TextChunk(
                "c1", "01-purchase.md", "采购入库",
                "采购订单下达后，货到仓库需办理到货单与采购入库。"
        );
        TextChunk period = new TextChunk(
                "c2", "03-period.md", "会计期间",
                "会计期间关闭后，禁止对本期凭证进行过账。"
        );

        List<IndexedChunk> indexed = List.of(
                new IndexedChunk(purchase, embedding.embed(purchase.searchableText())),
                new IndexedChunk(period, embedding.embed(period.searchableText()))
        );

        VectorCorpusIndex index = new VectorCorpusIndex(null, embedding) {
            @Override
            public void rebuild() {
                // no-op for unit test
            }

            @Override
            public List<IndexedChunk> allIndexed() {
                return indexed;
            }
        };

        VectorRetriever retriever = new VectorRetriever(embedding, index);
        List<RetrievedChunk> hits = retriever.retrieve("下完采购单以后货到了怎么处理？", List.of(), 1);

        assertFalse(hits.isEmpty());
        assertEquals("01-purchase.md", hits.getFirst().getChunk().getDocId());
        assertTrue(hits.getFirst().getScore() > 0);
    }
}
