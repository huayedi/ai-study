package com.erp.ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatToolOrchestratorTest {

    private ChatToolOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new com.erp.ai.tool.tools.QueryItemTool());
        registry.register(new com.erp.ai.tool.tools.QueryInventoryTool());
        registry.register(new com.erp.ai.tool.tools.QueryPeriodStatusTool());
        ToolExecutor executor = new ToolExecutor(registry, 3000);
        orchestrator = new ChatToolOrchestrator(executor, new ObjectMapper());
    }

    @Test
    void inventoryQuestionRunsQueryAndReturns120() {
        ChatToolOrchestrator.AugmentResult result =
                orchestrator.augment("A001 原料仓多少库存？");
        assertFalse(result.writeBlocked());
        assertEquals(1, result.traces().size());
        assertTrue(result.traces().getFirst().ok());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.traces().getFirst().data();
        assertEquals(120.0, ((Number) data.get("qty")).doubleValue());
        assertTrue(result.injectionMessages().getFirst().contains(ChatToolOrchestrator.TOOL_RESULT_MARKER));
    }

    @Test
    void writeIntentBlockedWithoutToolCall() {
        ChatToolOrchestrator.AugmentResult result = orchestrator.augment("帮我改成 999");
        assertTrue(result.writeBlocked());
        assertTrue(result.forceNeedHuman());
        assertTrue(result.traces().isEmpty());
        assertTrue(result.injectionMessages().getFirst().contains(ChatToolOrchestrator.WRITE_BLOCK_MARKER));
    }

    @Test
    void inventoryMissingWarehouseFailsValidation() {
        ChatToolOrchestrator.AugmentResult result = orchestrator.augment("A001 库存多少？");
        assertEquals(1, result.traces().size());
        assertFalse(result.traces().getFirst().ok());
        assertTrue(result.forceNeedHuman());
        assertTrue(result.traces().getFirst().error().contains("warehouse"));
    }
}
