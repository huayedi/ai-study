package com.erp.ai.tool.handler;

import com.erp.ai.tool.service.LearningDataRepository;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：查会计期间是否打开（只读 · 学习库 MySQL）。
 * <p>
 * <b>职责</b>：白名单工具 {@code queryPeriodStatus}，返回 OPEN/CLOSED 等状态，禁止改期间。
 * <p>
 * <b>为何没有写工具</b>：不提供 closePeriod/openPeriod——开关账属高风险财务操作，
 * 必须人工在 ERP 完成；Executor 禁写名单亦包含 closeperiod/openperiod。
 * <p>
 * <b>Day16 / Day18</b>：手测与 Chat 编排共用。
 * <p>
 * <b>上下游</b>：上游 ToolExecutor；下游 {@link LearningDataRepository}。
 */
public class QueryPeriodStatusTool implements ToolHandler {

    /** 学习主数据只读仓储 */
    private final LearningDataRepository dataRepository;

    /**
     * @param dataRepository 学习数据仓储
     */
    public QueryPeriodStatusTool(LearningDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * @return 固定名 {@code queryPeriodStatus}
     */
    @Override
    public String name() {
        return "queryPeriodStatus";
    }

    /**
     * 说明书：required=company+period，文案标明禁止改期间。
     *
     * @return 工具定义
     */
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

    /**
     * 执行只读期间状态查询。
     * <p>
     * <b>校验</b>：company、period 必填；未命中抛 IllegalArgumentException。
     * <b>超时</b>：由 ToolExecutor 统一限制。
     *
     * @param args 须含 company、period
     * @return 含 company/period/status/source 的 Map
     * @throws IllegalArgumentException 缺参或期间不存在
     */
    @Override
    public Object execute(Map<String, Object> args) {
        // 1) 校验双必填参数
        String company = QueryItemTool.requiredString(args, "company");
        String period = QueryItemTool.requiredString(args, "period");
        // 2) 只读查期间（无 UPDATE status）
        return dataRepository.findPeriod(company, period)
                .map(p -> Map.of(
                        "company", p.company(),
                        "period", p.period(),
                        "status", p.status(),
                        "source", "learning-mysql"
                ))
                // 3) 未命中交 Executor 记 fail
                .orElseThrow(() -> new IllegalArgumentException(
                        "period not found: company=" + company + ", period=" + period));
    }
}
