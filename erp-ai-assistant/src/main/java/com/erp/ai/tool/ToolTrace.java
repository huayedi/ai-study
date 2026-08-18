package com.erp.ai.tool;

/**
 * Day18：单次工具调用轨迹（写入 Chat 响应，便于手测/审计）。
 */
public record ToolTrace(
        String toolName,
        boolean ok,
        Object data,
        String error,
        long latencyMs
) {
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
