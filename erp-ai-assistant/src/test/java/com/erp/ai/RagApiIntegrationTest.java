package com.erp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "ai.provider=mock",
        "ai.api-key=",
        "ai.rag.retriever=keyword",
        "ai.rag.embedding-provider=hash"
})
@AutoConfigureMockMvc
class RagApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ragAskReturnsSourcesFromTextbook() throws Exception {
        mockMvc.perform(post("/api/ai/rag/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"采购主链路有哪些单据？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.provider").value("mock"))
                .andExpect(jsonPath("$.retriever").value("keyword"))
                .andExpect(jsonPath("$.gate").exists())
                .andExpect(jsonPath("$.reply.answer", not(emptyString())))
                .andExpect(jsonPath("$.sources.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.sources[0].docId").isNotEmpty());
    }
}
