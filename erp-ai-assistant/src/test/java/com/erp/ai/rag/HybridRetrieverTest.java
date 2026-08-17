package com.erp.ai.rag;

import com.erp.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridRetrieverTest {

    @Test
    void rrfBoostsChunkHitByBothPaths() {
        TextChunk both = new TextChunk("id-both", "doc-a.md", "采购", "采购订单与到货入库");
        TextChunk onlyKw = new TextChunk("id-kw", "doc-b.md", "专名", "会计期间关闭");
        TextChunk onlyVec = new TextChunk("id-vec", "doc-c.md", "口语", "货到了怎么入账");

        KeywordRetriever keyword = mock(KeywordRetriever.class);
        VectorRetriever vector = mock(VectorRetriever.class);
        when(keyword.retrieve(anyString(), anyList(), anyInt())).thenReturn(List.of(
                new RetrievedChunk(both, 0.9),
                new RetrievedChunk(onlyKw, 0.5)
        ));
        when(vector.retrieve(anyString(), anyList(), anyInt())).thenReturn(List.of(
                new RetrievedChunk(both, 0.8),
                new RetrievedChunk(onlyVec, 0.7)
        ));

        AiProperties props = new AiProperties();
        props.getRag().setRecallK(10);
        props.getRag().setRrfK(60);

        HybridRetriever hybrid = new HybridRetriever(keyword, vector, props);
        List<RetrievedChunk> hits = hybrid.retrieve("下完单货到了怎么入库", List.of(), 3);

        assertEquals(3, hits.size());
        assertEquals("id-both", hits.getFirst().getChunk().getId());
        // dual hit: 1/61 + 1/61 > single 1/62
        assertTrue(hits.getFirst().getScore() > hits.get(1).getScore());
    }
}
