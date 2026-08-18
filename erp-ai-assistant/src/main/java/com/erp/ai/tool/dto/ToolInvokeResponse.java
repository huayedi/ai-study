package com.erp.ai.tool.dto;

/**
 * Day16：工具调用 HTTP 响应。
 * <p>
 * <b>职责</b>：把 {@link com.erp.ai.tool.ToolResult} 加上本次请求 {@code traceId} 返回给客户端。
 * <p>
 * <b>只读 / 禁写</b>：响应形状仅描述查询成败；禁写拦截时 {@code ok=false} 且 {@code error} 含 not allowed。
 * <p>
 * <b>上下游</b>：上游 {@code ToolController}；下游 HTTP 客户端 / 手测脚本。
 *
 * @param traceId   本次 HTTP 追踪号（无横杠 UUID）
 * @param toolName  工具名
 * @param ok        是否成功
 * @param data      成功数据；失败为 null
 * @param error     失败说明；成功为 null
 * @param latencyMs 执行耗时毫秒
 */
public record ToolInvokeResponse(
        /** HTTP 层追踪号 */
        String traceId,
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
}
