package com.erp.ai.client;

/**
 * OpenAI 风格的一次 tool_call（function calling）。
 */
public record LlmToolCall(
        String id,
        String name,
        String argumentsJson
) {
}
