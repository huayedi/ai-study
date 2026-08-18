package com.erp.ai.tool;

/**
 * Day16：统一工具执行结果（成功 / 失败同一形状）。
 * <p>
 * <b>职责</b>：承载一次白名单工具调用的落盘前结果：工具名、是否成功、数据或错误、耗时毫秒。
 * HTTP 响应、Chat 轨迹 {@link ToolTrace}、审计日志均从此对象派生。
 * <p>
 * <b>只读 / 禁写</b>：本类型只描述查询结果，不表达写操作；失败时 {@code data} 为 null，
 * {@code error} 有说明（含禁写拦截、超时、缺参、未命中等）。
 * <p>
 * <b>Day16 / Day18</b>：Day16 {@code ToolController} 直接映射本记录；
 * Day18 {@link ToolTrace#from(ToolResult)} 写入 Chat 响应便于手测/审计。
 * <p>
 * <b>上下游</b>：上游 {@code ToolExecutor.run} 构造；下游 Controller / Orchestrator / Trace。
 *
 * @param toolName  实际尝试执行的工具名（禁写拦截时仍保留请求名；空白名为空串）
 * @param ok        true 表示查询成功且 data 可用；false 表示失败且 data 为 null
 * @param data      成功时的查询载荷；失败恒为 null
 * @param error     失败原因文案；成功恒为 null
 * @param latencyMs 从进入 Executor 到返回的墙钟耗时（含超时等待）
 */
public record ToolResult(
        /** 工具名（白名单名或被拒的请求名） */
        String toolName,
        /** 是否执行成功 */
        boolean ok,
        /** 成功时的只读查询数据；失败为 null */
        Object data,
        /** 失败说明；成功为 null */
        String error,
        /** 耗时毫秒 */
        long latencyMs
) {
    /**
     * 构造成功结果。
     *
     * @param toolName  工具名
     * @param data      查询数据（允许为业务 Map）
     * @param latencyMs 耗时毫秒
     * @return ok=true、error=null 的结果
     */
    public static ToolResult ok(String toolName, Object data, long latencyMs) {
        return new ToolResult(toolName, true, data, null, latencyMs);
    }

    /**
     * 构造失败结果（禁写、未注册、超时、校验失败、查无等统一走此工厂）。
     *
     * @param toolName  工具名（可为空串）
     * @param error     错误说明（不可为写操作成功之类误导文案）
     * @param latencyMs 耗时毫秒
     * @return ok=false、data=null 的结果
     */
    public static ToolResult fail(String toolName, String error, long latencyMs) {
        return new ToolResult(toolName, false, null, error, latencyMs);
    }
}
