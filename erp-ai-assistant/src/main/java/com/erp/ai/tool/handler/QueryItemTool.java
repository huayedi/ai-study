package com.erp.ai.tool.handler;

import com.erp.ai.tool.service.LearningDataRepository;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：按存货编码查名称/规格（只读 · 学习库 MySQL / 假数据）。
 * <p>
 * <b>职责</b>：白名单工具 {@code queryItem}，从学习主数据读存货，不接公司生产库。
 * <p>
 * <b>只读 / 禁写</b>：仅 SELECT 语义；无 update/insert。本包不存在 writeItem——
 * 因为助手不得改主数据，写操作须人工在 ERP 完成。
 * <p>
 * <b>Day16 / Day18</b>：HTTP invoke、路径 A tool_calls、路径 B 规则预查均可调用。
 * <p>
 * <b>上下游</b>：上游 ToolExecutor；下游 {@link LearningDataRepository}。
 */
public class QueryItemTool implements ToolHandler {

    /** 学习主数据只读仓储 */
    private final LearningDataRepository dataRepository;

    /**
     * @param dataRepository 学习数据仓储（MySQL 或内存）
     */
    public QueryItemTool(LearningDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    /**
     * @return 固定名 {@code queryItem}
     */
    @Override
    public String name() {
        return "queryItem";
    }

    /**
     * 说明书：强调只读与学习库，required=itemCode。
     *
     * @return 工具定义
     */
    @Override
    public ToolDefinition definition() {
        // 构建 JSON Schema 风格参数描述（非严格校验器，供模型/控制台阅读）
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

    /**
     * 执行只读查询。
     * <p>
     * <b>校验</b>：itemCode 必填且非空白；未命中抛 IllegalArgumentException。
     * <b>超时</b>：本方法无本地超时，由 ToolExecutor 统一限制。
     *
     * @param args 须含 itemCode
     * @return 含 itemCode/name/spec/source 的 Map
     * @throws IllegalArgumentException 缺参或未找到
     */
    @Override
    public Object execute(Map<String, Object> args) {
        // 1) 强校验必填字符串参数
        String itemCode = requiredString(args, "itemCode");
        // 2) 只读查库；命中则投影为对外 Map
        return dataRepository.findItem(itemCode)
                .map(item -> Map.of(
                        "itemCode", item.itemCode(),
                        "name", item.name(),
                        "spec", item.spec(),
                        "source", "learning-mysql"
                ))
                // 3) 未命中：抛给 Executor 记失败审计
                .orElseThrow(() -> new IllegalArgumentException("item not found: " + itemCode));
    }

    /**
     * 从 args 取必填非空字符串（供本工具与其它 Handler 复用）。
     * <p>
     * 校验：键缺失、值为 null、trim 后空、字面量 {@code "null"} 均视为缺失。
     *
     * @param args 参数 Map，可为 null（视为缺失）
     * @param key  参数键
     * @return trim 后的非空字符串
     * @throws IllegalArgumentException 缺失或空白
     */
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
