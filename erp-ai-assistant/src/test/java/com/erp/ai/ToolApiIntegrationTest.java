package com.erp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ToolApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listContainsReadonlyTools() throws Exception {
        mockMvc.perform(get("/api/ai/tool/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[?(@.name=='queryInventory')]").exists());
    }

    @Test
    void invokeInventoryOk() throws Exception {
        mockMvc.perform(post("/api/ai/tool/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toolName":"queryInventory","args":{"itemCode":"A001","warehouse":"原料仓"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.qty").value(120.0));
    }

    @Test
    void invokeUnknownToolReturnsOkFalse() throws Exception {
        mockMvc.perform(post("/api/ai/tool/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toolName":"writeInventory","args":{}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false));
    }
}
