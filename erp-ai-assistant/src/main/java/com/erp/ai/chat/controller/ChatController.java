package com.erp.ai.chat.controller;

import com.erp.ai.chat.dto.ChatRequest;
import com.erp.ai.chat.dto.ChatResponse;
import com.erp.ai.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 聊天 HTTP 入口（chat 域 Controller）。
 * <p>
 * <b>职责</b>：对外暴露多轮对话 REST 接口；只做协议层工作（路径映射、Bean Validation、请求转发），
 * 不包含会话拼装、LLM 调用、工具循环或回复解析等业务逻辑。
 * <p>
 * <b>在系统中的位置</b>：属于 {@code com.erp.ai.chat} 包分层中的 controller 层，
 * 与 {@code tool} / {@code rag} / {@code draft} / {@code security} 等域隔离；
 * 统一挂在 {@code /api/ai} 前缀下，具体动作为 {@code POST /api/ai/chat}。
 * <p>
 * <b>对应学习 Day</b>：Week1 起的核心聊天能力；后续 Day18（tool_calls / 规则预查）、
 * Day22（promptVersion）、Day23（注入早拦）等能力由下游 {@link ChatService} 编排，本类接口形态保持稳定。
 * <p>
 * <b>调用链 / 上下游</b>：
 * <ul>
 *   <li>上游：HTTP 客户端（curl / 前端 / 评测脚本）提交 {@link ChatRequest}</li>
 *   <li>本层：{@code @Valid} 校验后委托 {@link ChatService#chat(ChatRequest)}</li>
 *   <li>下游：{@link ChatService} → {@code SessionStore} / {@code LlmClient} / 工具编排 / {@code ReplyParser} 等</li>
 *   <li>出参：{@link ChatResponse}（含 reply、trace、用量、toolTraces、promptVersion）</li>
 * </ul>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>Controller 不写业务库、不直接碰 LLM；会话落库与工具调用均在 Service 层</li>
 *   <li>入参校验失败由全局异常处理返回 400，本方法不捕获</li>
 *   <li>与草稿域 {@code DraftController} 隔离：聊天有 session，草稿无 session</li>
 * </ul>
 *
 * @see ChatService
 * @see ChatRequest
 * @see ChatResponse
 */
@RestController
@RequestMapping("/api/ai")
public class ChatController {

    /** 聊天业务编排服务：本 Controller 唯一依赖，负责完整一轮对话编排 */
    private final ChatService chatService;

    /**
     * 构造注入 {@link ChatService}。
     *
     * @param chatService 聊天业务编排核心，不可为 null（由 Spring 容器保证）
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发起一轮 AI 对话。
     * <p>
     * 业务含义：客户端提交本轮用户消息（及可选 sessionId），服务端完成安全检查、上下文拼装、
     * LLM 调用（可含工具循环）、结构化解析与会话落库，返回业务回复及可观测字段。
     * <p>
     * 边界条件：
     * <ul>
     *   <li>{@code message} 为空或空白时，{@code @Valid} + {@code @NotBlank} 在进入方法前失败</li>
     *   <li>{@code sessionId} 可空：空则由下游生成新会话 ID 并在响应中返回</li>
     *   <li>LLM / 解析多次失败时，下游可能抛出 {@link IllegalStateException}，由全局异常处理</li>
     * </ul>
     *
     * @param request 聊天入参；{@code @Valid} 触发 Bean Validation（如 message 非空）
     * @return 结构化回复及观测字段（traceId、sessionId、usage、toolTraces 等）
     */
    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
}
