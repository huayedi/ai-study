package com.erp.ai.tool.handler;

import com.erp.ai.tool.service.LearningDataRepository;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：按存货+仓库查现存量（只读 · 学习库 MySQL）。
 * <p>
 * <b>职责</b>：白名单工具 {@code queryInventory}，返回学习假数据现存量，禁止改库存。
 * <p>
 * <b>为何没有写工具</b>：不提供 writeInventory/updateInventory——改库存必须人工在 ERP 完成，
 * 避免 AI 幻觉写账；Executor 层对含 write+inventor 的名字亦硬拦。
 * <p>
 * <b>Day16 / Day18</b>：手测 invoke、路径 A/B 均可调用本 Handler。
 * <p>
 * <b>上下游</b>：上游 ToolExecutor；下游 {@link LearningDataRepository}。
 */
public class QueryInventoryTool implements ToolHandler {

    /** 学习主数据只读仓储 */
    private final LearningDataRepository dataRepository;

    /**
     * @param dataRepository 学习数据仓储
     */
    public QueryInventoryTool(LearningDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * @return 固定名 {@code queryInventory}
     */
    @Override
    public String name() {
        return "queryInventory";
    }

    /**
     * 说明书：required=itemCode+warehouse，文案标明禁止改库存。
     *
     * @return 工具定义
     */
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

    /**
     * 执行只读现存查询。
     * <p>
     * <b>校验</b>：itemCode、warehouse 均必填；未命中抛 IllegalArgumentException。
     * <b>超时</b>：由 ToolExecutor 统一限制整次 execute。
     *
     * @param args 须含 itemCode、warehouse
     * @return 含 itemCode/warehouse/qty/source 的 Map
     * @throws IllegalArgumentException 缺参或库存行不存在
     */
    @Override
    public Object execute(Map<String, Object> args) {
        // 1) 校验双必填参数
        String itemCode = QueryItemTool.requiredString(args, "itemCode");
        String warehouse = QueryItemTool.requiredString(args, "warehouse");
        // 2) 只读查现存量（无 UPDATE）
        return dataRepository.findInventory(itemCode, warehouse)
                .map(inv -> Map.of(
                        "itemCode", inv.itemCode(),
                        "warehouse", inv.warehouse(),
                        "qty", inv.qty(),
                        "source", "learning-mysql"
                ))
                // 3) 未命中交 Executor 记 fail
                .orElseThrow(() -> new IllegalArgumentException(
                        "inventory not found: itemCode=" + itemCode + ", warehouse=" + warehouse));
    }
}
