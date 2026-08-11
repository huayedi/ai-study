package com.erp.ai.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 聊天接口入参 DTO。
 * <p>
 * 示例：
 * <pre>
 * {"sessionId":"可选","message":"我想做一笔采购"}
 * </pre>
 */
public class ChatRequest {

    /**
     * 会话 ID。首次可空，服务端会生成并在响应中返回；
     * 后续多轮请带回，以便拼接历史上下文。
     */
    private String sessionId;

    /** 用户本轮输入，必填 */
    @NotBlank(message = "message 不能为空")
    private String message;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
