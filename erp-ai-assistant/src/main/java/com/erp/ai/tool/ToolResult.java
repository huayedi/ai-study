package com.erp.ai.tool;

/**
 * Day16：统一工具执行结果。失败时 data 为 null，error 有说明。
 */
public record ToolResult(
        String toolName,
        boolean ok,
        Object data,
        String error,
        long latencyMs
) {
    public static ToolResult ok(String toolName, Object data, long latencyMs) {
        return new ToolResult(toolName, true, data, null, latencyMs);
    }

    public static ToolResult fail(String toolName, String error, long latencyMs) {
        return new ToolResult(toolName, false, null, error, latencyMs);
    }
}
