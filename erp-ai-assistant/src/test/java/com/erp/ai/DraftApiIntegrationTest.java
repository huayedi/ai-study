package com.erp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Day20：采购订单草稿辅助新接口（mock，无业务写库）。
 */
@SpringBootTest(properties = {
        "ai.provider=mock",
        "ai.api-key="
})
@AutoConfigureMockMvc
class DraftApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void purchaseOrderDraftExtractsFieldsAndMarksMissingWarehouse() throws Exception {
        mockMvc.perform(post("/api/ai/draft/purchase-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utterance":"向华东供应买 100 个 A001，下周一交货"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.suggestedDocType").value("采购订单"))
                .andExpect(jsonPath("$.fields.供应商").value("华东供应"))
                .andExpect(jsonPath("$.fields.存货编码").value("A001"))
                .andExpect(jsonPath("$.fields.数量").value(100))
                .andExpect(jsonPath("$.fields.交货日期").isNotEmpty())
                .andExpect(jsonPath("$.missing", hasItem("仓库")))
                .andExpect(jsonPath("$.missing", hasItem("含税单价")))
                .andExpect(jsonPath("$.needHuman").value(true))
                .andExpect(jsonPath("$.toolTraces[0].toolName").value("queryItem"))
                .andExpect(jsonPath("$.toolTraces[0].ok").value(true));
    }

    @Test
    void unknownItemCodeAddsWarningAndStillNeedHuman() throws Exception {
        mockMvc.perform(post("/api/ai/draft/purchase-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utterance":"向华东供应买 10 个 Z999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.needHuman").value(true))
                .andExpect(jsonPath("$.warnings[0]", containsString("Z999")))
                .andExpect(jsonPath("$.missing", hasItem(containsString("存货编码"))));
    }

    @Test
    void blankUtteranceRejected() throws Exception {
        mockMvc.perform(post("/api/ai/draft/purchase-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utterance":"  "}
                                """))
                .andExpect(status().isBadRequest());
    }
}
