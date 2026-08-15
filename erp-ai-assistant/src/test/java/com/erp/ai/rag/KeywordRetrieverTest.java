package com.erp.ai.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KeywordRetrieverTest {

    private final KeywordRetriever retriever = new KeywordRetriever();

    @Test
    void ranksPurchaseChunkHigherForPurchaseQuestion() {
        TextChunk purchase = new TextChunk("1", "01.md", "主链路", "物资请购 采购订单 到货单 采购入库 采购发票");
        TextChunk period = new TextChunk("2", "03.md", "会计期间", "会计期间关闭后不能过账");
        List<RetrievedChunk> hit = retriever.retrieve("采购主链路有哪些单据", List.of(purchase, period), 2);
        assertFalse(hit.isEmpty());
        assertEquals("01.md", hit.getFirst().getChunk().getDocId());
    }
}
