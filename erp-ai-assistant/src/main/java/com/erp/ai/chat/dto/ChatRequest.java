package com.erp.ai.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 聊天接口入参 DTO（chat 域）。
 * <p>
 * <b>职责</b>：承载 {@code POST /api/ai/chat} 的 JSON 请求体字段；
 * 仅做数据传输与校验注解声明，不含业务逻辑。
 * <p>
 * <b>在系统中的位置</b>：由 {@link com.erp.ai.chat.controller.ChatController} 接收，
 * 再交给 {@link com.erp.ai.chat.service.ChatService} 消费。
 * <p>
 * <b>对应学习 Day</b>：Week1 多轮会话基础；后续 Day 增强不改变本 DTO 的核心字段形态。
 * <p>
 * <b>调用链 / 上下游</b>：HTTP JSON → 本类 → {@code ChatService.chat} →
 * {@code SessionStore.resolveSessionId(sessionId)} 与用户消息拼装。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>{@code message} 必填（{@code @NotBlank}）；{@code sessionId} 可选以支持首轮建会话</li>
 *   <li>无默认值注入：空 sessionId 由服务端生成，客户端需把响应中的 sessionId 回传以续聊</li>
 * </ul>
 * <p>
 * 请求体示例：
 * <pre>
 * {"sessionId":"可选","message":"我想做一笔采购"}
 * </pre>
 *
 * @see ChatResponse
 * @see com.erp.ai.chat.controller.ChatController
 */
public class ChatRequest {

    /**
     * 会话 ID。
     * <p>
     * 首次可空，服务端会生成并在 {@link ChatResponse#getSessionId()} 中返回；
     * 后续多轮请原样带回，以便 {@code SessionStore} 拼接历史上下文。
     * 边界：空白字符串与 null 均视为「新会话」。
     */
    private String sessionId;

    /**
     * 用户本轮输入文本，必填。
     * <p>
     * 校验：{@code @NotBlank}，校验失败消息为「message 不能为空」。
     * 下游还会做 Day23 注入/越权话术检查，以及可能的工具意图识别。
     */
    @NotBlank(message = "message 不能为空")
    private String message;

    /**
     * 获取会话 ID。
     *
     * @return 客户端传入的 sessionId；首次请求可能为 null 或空白
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 设置会话 ID。
     *
     * @param sessionId 多轮续聊时回传的会话标识；可为 null 表示新建会话
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取用户本轮消息。
     *
     * @return 用户输入；校验通过后不为空白
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置用户本轮消息。
     *
     * @param message 用户自然语言输入；调用方应保证非空（否则入站校验失败）
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
