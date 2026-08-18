package com.erp.ai.tool;

/**
 * Day18：单次工具调用轨迹（写入 Chat 响应，便于手测与审计对照）。
 * <p>
 * <b>职责</b>：把 {@link ToolResult} 投影为可序列化进对话响应的轻量轨迹，
 * 不改变查询语义，不引入写操作字段。
 * <p>
 * <b>只读 / 禁写</b>：轨迹仅记录只读查询成败；若写意图在编排层被拦，
 * 通常不会产生本轨迹，而是注入「写操作拦截」系统段。
 * <p>
 * <b>Day18 路径</b>：路径 B {@code ChatToolOrchestrator} 预查后收集本列表；
 * 路径 A 在 tool_calls 执行后同样可用本类型回写响应。
 * <p>
 * <b>上下游</b>：上游 {@link ToolResult}；下游 Chat 响应 DTO / 前端调试面板。
 *
 * @param toolName  工具名
 * @param ok        是否成功
 * @param data      成功数据；失败为 null
 * @param error     失败说明；成功为 null
 * @param latencyMs 耗时毫秒
 */
public record ToolTrace(
        /** 工具名 */
        String toolName,
        /** 是否成功 */
        boolean ok,
        /** 只读查询数据 */
        Object data,
        /** 错误信息 */
        String error,
        /** 耗时毫秒 */
        long latencyMs
) {
    /**
     * 从统一执行结果复制字段，避免 Chat 层直接依赖 Executor 内部结构之外的类型差异。
     *
     * @param result 非空的 {@link ToolResult}
     * @return 同字段轨迹
     */
    public static ToolTrace from(ToolResult result) {
        return new ToolTrace(
                result.toolName(),
                result.ok(),
                result.data(),
                result.error(),
                result.latencyMs()
        );
    }
}
