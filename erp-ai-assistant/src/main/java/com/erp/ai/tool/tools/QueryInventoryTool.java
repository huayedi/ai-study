package com.erp.ai.tool.tools;

import com.erp.ai.store.LearningDataRepository;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：按存货+仓库查现存量（只读 · 学习库 MySQL）。
 */
public class QueryInventoryTool implements ToolHandler {

    private final LearningDataRepository dataRepository;

    public QueryInventoryTool(LearningDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public String name() {
        return "queryInventory";
    }

    @Override
    public ToolDefinition definition() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", new String[]{"itemCode", "warehouse"});
        schema.put("properties", Map.of(
                "itemCode", Map.of("type", "string", "description", "存货编码"),
                "warehouse", Map.of("type", "string", "description", "仓库名称，如 原料仓 / 成品仓")
        ));
        return new ToolDefinition(
                name(),
                "只读查询现存量（学习库假数据，禁止改库存）。需 itemCode + warehouse。",
                schema
        );
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String itemCode = QueryItemTool.requiredString(args, "itemCode");
        String warehouse = QueryItemTool.requiredString(args, "warehouse");
        return dataRepository.findInventory(itemCode, warehouse)
                .map(inv -> Map.of(
                        "itemCode", inv.itemCode(),
                        "warehouse", inv.warehouse(),
                        "qty", inv.qty(),
                        "source", "learning-mysql"
                ))
                .orElseThrow(() -> new IllegalArgumentException(
                        "inventory not found: itemCode=" + itemCode + ", warehouse=" + warehouse));
    }
}
