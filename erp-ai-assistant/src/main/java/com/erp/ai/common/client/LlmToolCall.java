package com.erp.ai.common.client;

/**
 * OpenAI 风格的一次 tool_call（function calling）不可变记录。
 *
 * <h2>职责</h2>
 * 描述模型请求调用的函数名与参数 JSON，供编排器执行只读工具后回填。
 *
 * <h2>为何用 record</h2>
 * 值对象、字段少、天然不可变；适合在消息列表与 HTTP 请求体之间传递。
 *
 * <h2>与协议的关系</h2>
 * 对应 Chat Completions 响应中 {@code choices[0].message.tool_calls[]} 的单项：
 * {@code id}、{@code function.name}、{@code function.arguments}。
 *
 * <h2>学习要点</h2>
 * {@code argumentsJson} 是<strong>字符串形式的 JSON</strong>（不是已解析的 Map）；
 * 执行前需再 parse，且要防模型胡写参数。
 *
 * @param id            本次调用唯一 ID；回填 role=tool 时必须带回同一 {@code tool_call_id}
 * @param name          函数/工具名，须与暴露的 {@code ToolDefinition#name()} 一致
 * @param argumentsJson 参数 JSON 字符串；缺失时实现侧常填 {@code "{}"}；可能非法 JSON，调用方需容错
 */
public record LlmToolCall(
        String id,
        String name,
        String argumentsJson
) {
}
