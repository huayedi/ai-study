package com.erp.ai.tool.dto;

/**
 * Day16：工具调用 HTTP 响应。
 */
public record ToolInvokeResponse(
        String traceId,
        String toolName,
        boolean ok,
        Object data,
        String error,
        long latencyMs
) {
}
