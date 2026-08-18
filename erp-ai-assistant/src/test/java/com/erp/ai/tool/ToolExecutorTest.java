package com.erp.ai.tool;

import com.erp.ai.tool.handler.QueryInventoryTool;
import com.erp.ai.tool.handler.QueryItemTool;
import com.erp.ai.tool.handler.QueryPeriodStatusTool;
import com.erp.ai.tool.service.MemoryLearningDataRepository;
import com.erp.ai.tool.service.ToolExecutor;
import com.erp.ai.tool.service.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolExecutorTest {

    private ToolExecutor executor;

    @BeforeEach
    void setUp() {
        var data = new MemoryLearningDataRepository();
        ToolRegistry registry = new ToolRegistry();
        registry.register(new QueryItemTool(data));
        registry.register(new QueryInventoryTool(data));
        registry.register(new QueryPeriodStatusTool(data));
        executor = new ToolExecutor(registry, 3000);
    }

    @Test
    void queryInventoryReturnsFakeQty() {
        ToolResult result = executor.run("queryInventory", Map.of(
                "itemCode", "ITEM-A001",
                "warehouse", "原料仓"
        ));
        assertTrue(result.ok(), result.error());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.data();
        assertEquals(120.0, ((Number) data.get("qty")).doubleValue());
    }

    @Test
    void missingWarehouseFailsValidation() {
        ToolResult result = executor.run("queryInventory", Map.of("itemCode", "ITEM-A001"));
        assertFalse(result.ok());
        assertTrue(result.error().contains("warehouse"));
    }

    @Test
    void unregisteredToolRejected() {
        ToolResult result = executor.run("deleteOrder", Map.of());
        assertFalse(result.ok());
        assertTrue(result.error().contains("not allowed"));
    }

    @Test
    void writeInventoryNameHardBlocked() {
        ToolResult result = executor.run("writeInventory", Map.of("qty", 999));
        assertFalse(result.ok());
        assertTrue(result.error().toLowerCase().contains("forbidden")
                || result.error().contains("not allowed"));
    }

    @Test
    void queryPeriodOpen() {
        ToolResult result = executor.run("queryPeriodStatus", Map.of(
                "company", "主公司",
                "period", "2026-08"
        ));
        assertTrue(result.ok(), result.error());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.data();
        assertEquals("OPEN", data.get("status"));
    }
}
