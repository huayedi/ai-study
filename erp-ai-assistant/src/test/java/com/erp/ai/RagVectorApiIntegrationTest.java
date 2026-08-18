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

/**
 * Day11：向量检索模式（hash embedding + mock chat）。
 */
@SpringBootTest(properties = {
        "ai.provider=mock",
        "ai.api-key=",
        "ai.rag.retriever=vector",
        "ai.rag.embedding-provider=hash"
})
@AutoConfigureMockMvc
class RagVectorApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void vectorRagAskReturnsSources() throws Exception {
        mockMvc.perform(post("/api/ai/rag/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"下完采购单以后货到了怎么处理？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retriever").value("vector"))
                .andExpect(jsonPath("$.reply.answer", not(emptyString())))
                .andExpect(jsonPath("$.sources.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.sources[0].score").isNumber());
    }
}
