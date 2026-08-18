package com.erp.ai.draft.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DraftParserTest {

    private final DraftParser parser = new DraftParser(new com.fasterxml.jackson.databind.ObjectMapper());

    @Test
    void parsesSnakeCaseDraftJson() {
        var draft = parser.parse("""
                {
                  "suggested_doc_type": "采购订单",
                  "fields": {"供应商":"华东供应","存货编码":"A001","数量":100},
                  "missing": ["仓库"],
                  "need_human": true,
                  "warnings": [],
                  "confidence": 0.7
                }
                """);
        assertEquals("采购订单", draft.getSuggestedDocType());
        assertEquals("华东供应", draft.getFields().get("供应商"));
        assertEquals(100L, draft.getFields().get("数量"));
        assertTrue(draft.getMissing().contains("仓库"));
        assertTrue(draft.isNeedHuman());
        assertEquals(0.7, draft.getConfidence());
    }

    @Test
    void extractsJsonFromMarkdownFence() {
        var draft = parser.parse("""
                ```json
                {"suggested_doc_type":"采购订单","fields":{},"missing":["供应商"],"need_human":true}
                ```
                """);
        assertTrue(draft.getMissing().contains("供应商"));
    }
}
