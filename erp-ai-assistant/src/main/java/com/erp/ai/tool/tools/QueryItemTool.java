package com.erp.ai.tool.tools;

import com.erp.ai.store.LearningDataRepository;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：按存货编码查名称/规格（只读 · 学习库 MySQL / 假数据）。
 */
public class QueryItemTool implements ToolHandler {

    private final LearningDataRepository dataRepository;

    public QueryItemTool(LearningDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public String name() {
        return "queryItem";
    }

    @Override
    public ToolDefinition definition() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", new String[]{"itemCode"});
        schema.put("properties", Map.of(
                "itemCode", Map.of("type", "string", "description", "存货编码，如 ITEM-A001 或 A001")
        ));
        return new ToolDefinition(
                name(),
                "只读查询存货主数据（学习库/假数据，不接公司生产库）。按 itemCode 返回名称与规格。",
                schema
        );
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String itemCode = requiredString(args, "itemCode");
        return dataRepository.findItem(itemCode)
                .map(item -> Map.of(
                        "itemCode", item.itemCode(),
                        "name", item.name(),
                        "spec", item.spec(),
                        "source", "learning-mysql"
                ))
                .orElseThrow(() -> new IllegalArgumentException("item not found: " + itemCode));
    }

    static String requiredString(Map<String, Object> args, String key) {
        if (args == null || !args.containsKey(key) || args.get(key) == null) {
            throw new IllegalArgumentException("missing required arg: " + key);
        }
        String v = String.valueOf(args.get(key)).trim();
        if (v.isEmpty() || "null".equalsIgnoreCase(v)) {
            throw new IllegalArgumentException("missing required arg: " + key);
        }
        return v;
    }
}
