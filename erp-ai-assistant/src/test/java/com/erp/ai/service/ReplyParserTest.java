package com.erp.ai.service;

import com.erp.ai.model.AssistantReply;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplyParserTest {

    private final ReplyParser parser = new ReplyParser(new ObjectMapper());

    @Test
    void parsesStrictJson() {
        String raw = """
                {"answer":"先建采购订单草稿","need_human":true,"suggested_doc_type":"采购订单","required_fields":["供应商","物料编码"],"confidence":0.8}
                """;
        AssistantReply reply = parser.parse(raw);
        assertEquals("先建采购订单草稿", reply.getAnswer());
        assertTrue(reply.isNeedHuman());
        assertEquals("采购订单", reply.getSuggestedDocType());
        assertEquals(2, reply.getRequiredFields().size());
    }

    @Test
    void parsesFencedJson() {
        String raw = """
                ```json
                {"answer":"期间可能已关闭","need_human":false,"required_fields":[]}
                ```
                """;
        AssistantReply reply = parser.parse(raw);
        assertEquals("期间可能已关闭", reply.getAnswer());
    }
}
