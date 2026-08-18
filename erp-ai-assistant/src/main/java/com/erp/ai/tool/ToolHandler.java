package com.erp.ai.tool;

import java.util.Map;

/**
 * Day16：单个<strong>只读</strong>工具的执行接口。
 * <p>
 * <b>职责</b>：约定白名单内工具的名称、元数据说明书与一次查询执行契约。
 * 每个实现类对应一个已注册的只读查询工具（如 queryItem / queryInventory / queryPeriodStatus）。
 * <p>
 * <b>只读白名单 / 禁写约束</b>：
 * <ul>
 *   <li>未注册的名字不得进入此接口的 {@link #execute(Map)} 调用链；</li>
 *   <li>本包<strong>故意不提供</strong>任何写库存、过账、删单、付款、开关期间的 {@code ToolHandler} 实现；</li>
 *   <li>原因：学习助手只允许查学习库假数据，避免模型或手测入口误改生产账；写操作由人工在 ERP 完成。</li>
 * </ul>
 * <p>
 * <b>Day16 / Day18 路径</b>：
 * <ul>
 *   <li>Day16：HTTP {@code /api/ai/tool/invoke} → {@code ToolExecutor} → 本接口；</li>
 *   <li>Day18 路径 A（默认 tool_calls）：模型选白名单工具名后仍经 {@code ToolExecutor} 调本接口；</li>
 *   <li>Day18 路径 B（rule）：{@code ChatToolOrchestrator} 预查后同样经 {@code ToolExecutor}。</li>
 * </ul>
 * <p>
 * <b>上下游</b>：上游为 {@code ToolRegistry.require} / {@code ToolExecutor.run}；
 * 下游为 {@code LearningDataRepository}（MySQL learning_* 或内存假数据）。
 *
 * @see ToolDefinition
 * @see ToolResult
 * @see com.erp.ai.tool.service.ToolExecutor
 */
public interface ToolHandler {

    /**
     * 工具稳定名（白名单键），须与 {@link ToolDefinition#name()} 一致。
     *
     * @return 非空工具名，如 {@code queryItem}
     */
    String name();

    /**
     * 暴露给模型/控制台的「说明书」。description 须诚实标明只读与学习假数据。
     *
     * @return 工具元数据（含参数 JSON Schema 风格描述）
     */
    ToolDefinition definition();

    /**
     * 执行<strong>只读</strong>查询。不得在实现内写库、改库存或开关期间。
     * <p>
     * <b>校验语义</b>：参数缺失、空白、查无数据时应抛 {@link IllegalArgumentException}，
     * 由 {@link com.erp.ai.tool.service.ToolExecutor} 统一记审计并包装为失败 {@link ToolResult}。
     * <p>
     * <b>超时语义</b>：本方法本身不设超时；由 {@code ToolExecutor} 在独立线程上
     * 以 {@code ai.tool.timeout-ms} 限制整次 {@code execute} 耗时。
     *
     * @param args 调用参数（可为 null，由调用方规范化为空 Map）；键名须符合 definition 中 required
     * @return 查询结果对象（通常为 Map，含 source=learning-mysql 等字段）
     * @throws IllegalArgumentException 缺参、非法参或学习库未命中
     * @throws RuntimeException         其它未预期运行时错误（Executor 会记失败审计）
     */
    Object execute(Map<String, Object> args);
}
