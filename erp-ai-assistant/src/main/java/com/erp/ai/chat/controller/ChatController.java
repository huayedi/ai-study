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
 * AI 聊天 HTTP 入口。
 * <p>
 * 路径：{@code POST /api/ai/chat}<br>
 * Controller 只负责协议层（入参校验、转发），业务编排在 {@link ChatService}。
 */
@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发起一轮对话。
     *
     * @param request {@code @Valid} 触发 Bean Validation（如 message 非空）
     * @return 结构化回复及观测字段
     */
    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
}
