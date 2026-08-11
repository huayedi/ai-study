package com.erp.ai.service;

import com.erp.ai.client.LlmClient;
import com.erp.ai.client.LlmResult;
import com.erp.ai.config.AiProperties;
import com.erp.ai.model.AssistantReply;
import com.erp.ai.model.ChatMessage;
import com.erp.ai.model.dto.ChatRequest;
import com.erp.ai.model.dto.ChatResponse;
import com.erp.ai.observability.AiCallLog;
import com.erp.ai.prompt.SystemPromptLoader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 聊天业务编排核心。
 * <p>
 * 一次成功调用的步骤：
 * <ol>
 *   <li>生成 traceId / 解析 sessionId</li>
 *   <li>组装 messages：system + 历史 + 当前 user</li>
 *   <li>调用 {@link LlmClient}</li>
 *   <li>{@link ReplyParser} 校验并解析 JSON</li>
 *   <li>写入会话历史，记录可观测日志，返回 {@link ChatResponse}</li>
 * </ol>
 * 若解析失败或调用失败，会在有限次数内重试，并追加“请按 Schema 重输出”的纠错提示。
 */
@Service
public class ChatService {

    private final LlmClient llmClient;
    private final SessionStore sessionStore;
    private final SystemPromptLoader systemPromptLoader;
    private final ReplyParser replyParser;
    private final AiProperties properties;
    private final AiCallLog aiCallLog;

    public ChatService(LlmClient llmClient,
                       SessionStore sessionStore,
                       SystemPromptLoader systemPromptLoader,
                       ReplyParser replyParser,
                       AiProperties properties,
                       AiCallLog aiCallLog) {
        this.llmClient = llmClient;
        this.sessionStore = sessionStore;
        this.systemPromptLoader = systemPromptLoader;
        this.replyParser = replyParser;
        this.properties = properties;
        this.aiCallLog = aiCallLog;
    }

    /**
     * 处理一轮用户对话。
     *
     * @param request 含可选 sessionId 与必填 message
     * @return 结构化回复 + 观测字段
     */
    public ChatResponse chat(ChatRequest request) {
        // traceId：一次请求一条，贯穿日志
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = sessionStore.resolveSessionId(request.getSessionId());
        long started = System.currentTimeMillis();

        List<ChatMessage> messages = buildMessages(sessionId, request.getMessage());
        // maxRetries=2 → 最多尝试 3 次
        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                // 1) 调模型
                LlmResult result = llmClient.chat(messages);
                // 2) 解析/校验 JSON（失败会抛异常进入 catch 重试）
                AssistantReply reply = replyParser.parse(result.getContent());

                // 3) 仅在成功解析后写入历史，避免把脏输出污染后续轮次
                sessionStore.append(
                        sessionId,
                        new ChatMessage("user", request.getMessage()),
                        new ChatMessage("assistant", result.getContent())
                );

                double cost = estimateCost(result.getPromptTokens(), result.getCompletionTokens());
                long latency = System.currentTimeMillis() - started;
                aiCallLog.success(
                        traceId,
                        sessionId,
                        llmClient.providerName(),
                        result.getModel(),
                        latency,
                        attempts,
                        result.getPromptTokens(),
                        result.getCompletionTokens(),
                        cost
                );

                ChatResponse response = new ChatResponse();
                response.setTraceId(traceId);
                response.setSessionId(sessionId);
                response.setProvider(llmClient.providerName());
                response.setModel(result.getModel());
                response.setReply(reply);
                response.setLatencyMs(latency);
                response.setAttempts(attempts);

                ChatResponse.Usage usage = new ChatResponse.Usage();
                usage.setPromptTokens(result.getPromptTokens());
                usage.setCompletionTokens(result.getCompletionTokens());
                usage.setTotalTokens(result.getTotalTokens());
                usage.setEstimatedCostUsd(cost);
                response.setUsage(usage);
                return response;
            } catch (RuntimeException ex) {
                lastError = ex;
                // 把失败原因喂回模型，要求按 Schema 重试（对 401 这类鉴权错误效果有限，但逻辑统一）
                messages = withRepairHint(messages, ex.getMessage());
            }
        }

        long latency = System.currentTimeMillis() - started;
        String reason = lastError == null ? "unknown" : lastError.getMessage();
        aiCallLog.failure(traceId, sessionId, llmClient.providerName(), latency, attempts, reason);
        throw new IllegalStateException("AI 调用失败(traceId=" + traceId + "): " + reason, lastError);
    }

    /**
     * 组装发给模型的完整 messages。
     * 顺序固定：system → 历史对话 → 当前 user。
     */
    private List<ChatMessage> buildMessages(String sessionId, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPromptLoader.getSystemPrompt()));
        messages.addAll(sessionStore.getHistory(sessionId));
        messages.add(new ChatMessage("user", userMessage));
        return messages;
    }

    /**
     * 追加一条“纠错提示”用户消息，引导模型只输出合法 JSON。
     */
    private List<ChatMessage> withRepairHint(List<ChatMessage> messages, String error) {
        List<ChatMessage> repaired = new ArrayList<>(messages);
        repaired.add(new ChatMessage(
                "user",
                "上一次输出不符合要求（" + error + "）。请重新只输出合法 JSON，字段必须包含 answer 与 need_human。"
        ));
        return repaired;
    }

    /**
     * 按配置单价粗算成本（美元）。仅供学习观察，非官方账单。
     */
    private double estimateCost(int promptTokens, int completionTokens) {
        double input = (promptTokens / 1000.0) * properties.getPriceInputPer1k();
        double output = (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
        return Math.round((input + output) * 1_000_000.0) / 1_000_000.0;
    }
}
