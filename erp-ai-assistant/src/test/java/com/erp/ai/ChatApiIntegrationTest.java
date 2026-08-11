package com.erp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Chat API 集成测试（默认 mock provider，无需外网与真实 Key）。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChatApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 验证采购类问题能返回结构化字段：answer / needHuman / suggestedDocType / requiredFields。
     */
    @Test
    void chatReturnsStructuredReplyInMockMode() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"我想做一笔采购，需要填哪些字段？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.provider").value("mock"))
                .andExpect(jsonPath("$.reply.answer").isNotEmpty())
                .andExpect(jsonPath("$.reply.needHuman").value(true))
                .andExpect(jsonPath("$.reply.suggestedDocType").value("采购订单"))
                .andExpect(jsonPath("$.reply.requiredFields", hasItem("供应商")))
                .andExpect(jsonPath("$.usage.totalTokens").isNumber());
    }
}
