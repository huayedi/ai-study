package com.erp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "ai.provider=mock",
        "ai.api-key=",
        "ai.rag.retriever=hybrid",
        "ai.rag.embedding-provider=hash",
        "ai.rag.min-score=0.01",
        "ai.rag.skip-llm-on-empty=true"
})
@AutoConfigureMockMvc
class RagHybridApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hybridAskReturnsGateAndSources() throws Exception {
        mockMvc.perform(post("/api/ai/rag/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"采购主链路有哪些单据？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retriever").value("hybrid"))
                .andExpect(jsonPath("$.gate").exists())
                .andExpect(jsonPath("$.sources.length()", greaterThanOrEqualTo(1)));
    }
}

/**
 * 空语料 → EMPTY：不调 LLM，sources=[]。
 */
@SpringBootTest(properties = {
        "ai.provider=mock",
        "ai.api-key=",
        "ai.rag.retriever=hybrid",
        "ai.rag.embedding-provider=hash",
        "ai.rag.classpath-docs=rag-docs-empty",
        "ai.rag.skip-llm-on-empty=true"
})
@AutoConfigureMockMvc
class RagEmptyCorpusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void emptyCorpusSkipsLlmAndReturnsNoSources() throws Exception {
        mockMvc.perform(post("/api/ai/rag/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"采购主链路有哪些单据？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gate").value("EMPTY"))
                .andExpect(jsonPath("$.sources", hasSize(0)))
                .andExpect(jsonPath("$.reply.needHuman").value(true))
                .andExpect(jsonPath("$.attempts").value(0));
    }
}
