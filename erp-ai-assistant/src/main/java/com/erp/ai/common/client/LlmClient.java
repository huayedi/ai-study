package com.erp.ai.common.client;

import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.tool.ToolDefinition;

import java.util.List;

/**
 * 大模型（LLM）调用抽象接口。
 *
 * <h2>职责</h2>
 * 对上层（如 {@code ChatService}、工具编排器）屏蔽「本地 Mock」与「远程 OpenAI 兼容网关」
 * 的差异，统一提供对话能力。
 *
 * <h2>为何抽象</h2>
 * <ul>
 *   <li>学习期默认 {@code mock}，无需 API Key 即可跑通业务链路</li>
 *   <li>生产/联调切换 {@code openai-compatible} 时，业务代码零改动</li>
 *   <li>便于单测注入假实现</li>
 * </ul>
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 唯一实现 Bean 由 {@code LlmClientConfig} 根据 {@code ai.provider} 注册。
 * 业务侧只依赖本接口，不要直接 {@code new} 具体类。
 *
 * <h2>Day18 路径 A</h2>
 * {@link #chat(List, List)} 可携带只读 {@link ToolDefinition}；
 * 若模型返回 {@link LlmToolCall}，上层需执行工具后再带回消息列表继续对话。
 *
 * <h2>学习要点</h2>
 * 接口稳定、实现可换；Token / 成本 / 重试属于上层关注点，本接口只返回原始 {@link LlmResult}。
 */
public interface LlmClient {

    /**
     * 当前实现的逻辑提供方名称，用于审计日志与观测。
     *
     * @return 例如 {@code mock}、{@code openai-compatible}；不会为 {@code null}
     */
    String providerName();

    /**
     * 无 tools 的普通对话（兼容旧调用方）。
     * <p>
     * 默认委托 {@link #chat(List, List)} 并传入空工具列表，表示本轮不暴露 function calling。
     *
     * @param messages 完整消息列表（system / user / assistant 等）；不应为 {@code null}
     * @return 模型原始结果；若实现失败则抛出运行时异常（通常包装为 {@link IllegalStateException}）
     * @throws IllegalStateException 远程调用失败、鉴权失败或响应非法时（具体由实现决定）
     * @throws IllegalArgumentException 若实现选择对空消息等入参做严格校验时
     */
    default LlmResult chat(List<ChatMessage> messages) {
        return chat(messages, List.of());
    }

    /**
     * 带可选工具定义的对话。
     * <p>
     * <b>边界与失败语义：</b>
     * <ul>
     *   <li>{@code tools} 为空或 {@code null}：本轮不暴露 tools，期望直接返回文本/JSON content</li>
     *   <li>{@code tools} 非空：可能返回 {@link LlmResult#hasToolCalls()} 为 true，
     *       此时 content 可能为空，调用方必须先执行工具</li>
     *   <li>实现不应静默吞掉 HTTP/解析错误；应向上抛出，由全局异常处理或重试层处理</li>
     * </ul>
     *
     * @param messages 完整消息列表（可含 assistant.tool_calls / role=tool）；不应为 {@code null}
     * @param tools    只读工具定义；空列表表示本轮不暴露 tools；允许 {@code null}（视同空）
     * @return 原始 LLM 结果（尚未解析为业务 {@code AssistantReply}）
     * @throws IllegalStateException 配置缺失（如无 API Key）、HTTP 失败或响应无法解析时
     */
    LlmResult chat(List<ChatMessage> messages, List<ToolDefinition> tools);
}
