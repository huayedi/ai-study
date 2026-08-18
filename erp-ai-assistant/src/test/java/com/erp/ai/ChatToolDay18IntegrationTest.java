package com.erp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Day18 路径 A：tool_calls 循环验收。
 */
@SpringBootTest(properties = {
        "ai.provider=mock",
        "ai.api-key=",
        "ai.tool.chat-path=tool-calls"
})
@AutoConfigureMockMvc
class ChatToolDay18IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void inventoryQuestionUsesToolQty120() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"A001 原料仓多少库存？"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolTraces.length()").value(1))
                .andExpect(jsonPath("$.toolTraces[0].toolName").value("queryInventory"))
                .andExpect(jsonPath("$.toolTraces[0].ok").value(true))
                .andExpect(jsonPath("$.toolTraces[0].data.qty").value(120.0))
                .andExpect(jsonPath("$.reply.answer", containsString("120")))
                .andExpect(jsonPath("$.reply.needHuman").value(false));
    }

    @Test
    void writeIntentRejectedWithNeedHuman() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"帮我把库存改成 999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolTraces.length()").value(0))
                .andExpect(jsonPath("$.reply.needHuman").value(true))
                .andExpect(jsonPath("$.reply.answer", containsString("拦截")));
    }
}
