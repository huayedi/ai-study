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
import com.erp.ai.tool.ChatToolOrchestrator;
import com.erp.ai.tool.ToolTrace;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 聊天业务编排核心。
 * <p>
 * Day18：路径 B — 在调 LLM 前由 {@link ChatToolOrchestrator} 预查只读工具并注入结果。
 */
@Service
public class ChatService {

    private final LlmClient llmClient;
    private final SessionStore sessionStore;
    private final SystemPromptLoader systemPromptLoader;
    private final ReplyParser replyParser;
    private final AiProperties properties;
    private final AiCallLog aiCallLog;
    private final ChatToolOrchestrator chatToolOrchestrator;

    public ChatService(LlmClient llmClient,
                       SessionStore sessionStore,
                       SystemPromptLoader systemPromptLoader,
                       ReplyParser replyParser,
                       AiProperties properties,
                       AiCallLog aiCallLog,
                       ChatToolOrchestrator chatToolOrchestrator) {
        this.llmClient = llmClient;
        this.sessionStore = sessionStore;
        this.systemPromptLoader = systemPromptLoader;
        this.replyParser = replyParser;
        this.properties = properties;
        this.aiCallLog = aiCallLog;
        this.chatToolOrchestrator = chatToolOrchestrator;
    }

    public ChatResponse chat(ChatRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = sessionStore.resolveSessionId(request.getSessionId());
        long started = System.currentTimeMillis();

        ChatToolOrchestrator.AugmentResult augment = properties.getTool().isRulePathEnabled()
                ? chatToolOrchestrator.augment(request.getMessage())
                : ChatToolOrchestrator.AugmentResult.none();

        List<ChatMessage> messages = buildMessages(sessionId, request.getMessage(), augment);
        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                LlmResult result = llmClient.chat(messages);
                AssistantReply reply = replyParser.parse(result.getContent());
                if (augment.forceNeedHuman() || augment.writeBlocked()) {
                    reply.setNeedHuman(true);
                }

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
                response.setToolTraces(List.copyOf(augment.traces()));

                ChatResponse.Usage usage = new ChatResponse.Usage();
                usage.setPromptTokens(result.getPromptTokens());
                usage.setCompletionTokens(result.getCompletionTokens());
                usage.setTotalTokens(result.getTotalTokens());
                usage.setEstimatedCostUsd(cost);
                response.setUsage(usage);
                return response;
            } catch (RuntimeException ex) {
                lastError = ex;
                messages = withRepairHint(messages, ex.getMessage());
            }
        }

        long latency = System.currentTimeMillis() - started;
        String reason = lastError == null ? "unknown" : lastError.getMessage();
        aiCallLog.failure(traceId, sessionId, llmClient.providerName(), latency, attempts, reason);
        throw new IllegalStateException("AI 调用失败(traceId=" + traceId + "): " + reason, lastError);
    }

    private List<ChatMessage> buildMessages(String sessionId,
                                            String userMessage,
                                            ChatToolOrchestrator.AugmentResult augment) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPromptLoader.getSystemPrompt()));
        messages.addAll(sessionStore.getHistory(sessionId));
        messages.add(new ChatMessage("user", userMessage));
        if (augment != null && augment.hasInjections()) {
            for (String injection : augment.injectionMessages()) {
                messages.add(new ChatMessage("user", injection));
            }
        }
        return messages;
    }

    private List<ChatMessage> withRepairHint(List<ChatMessage> messages, String error) {
        List<ChatMessage> repaired = new ArrayList<>(messages);
        repaired.add(new ChatMessage(
                "user",
                "上一次输出不符合要求（" + error + "）。请重新只输出合法 JSON，字段必须包含 answer 与 need_human。"
        ));
        return repaired;
    }

    private double estimateCost(int promptTokens, int completionTokens) {
        double input = (promptTokens / 1000.0) * properties.getPriceInputPer1k();
        double output = (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
        return Math.round((input + output) * 1_000_000.0) / 1_000_000.0;
    }
}
