package com.erp.ai.tool.tools;

import com.erp.ai.store.LearningDataRepository;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：查会计期间是否打开（只读 · 学习库 MySQL）。
 */
public class QueryPeriodStatusTool implements ToolHandler {

    private final LearningDataRepository dataRepository;

    public QueryPeriodStatusTool(LearningDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public String name() {
        return "queryPeriodStatus";
    }

    @Override
    public ToolDefinition definition() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", new String[]{"company", "period"});
        schema.put("properties", Map.of(
                "company", Map.of("type", "string", "description", "公司名，学习假数据用「主公司」"),
                "period", Map.of("type", "string", "description", "期间，如 2026-08")
        ));
        return new ToolDefinition(
                name(),
                "只读查询会计期间状态 OPEN/CLOSED（学习库假数据，禁止改期间）。",
                schema
        );
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String company = QueryItemTool.requiredString(args, "company");
        String period = QueryItemTool.requiredString(args, "period");
        return dataRepository.findPeriod(company, period)
                .map(p -> Map.of(
                        "company", p.company(),
                        "period", p.period(),
                        "status", p.status(),
                        "source", "learning-mysql"
                ))
                .orElseThrow(() -> new IllegalArgumentException(
                        "period not found: company=" + company + ", period=" + period));
    }
}
