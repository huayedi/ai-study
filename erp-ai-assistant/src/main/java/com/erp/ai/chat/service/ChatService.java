package com.erp.ai.chat.service;

import com.erp.ai.common.client.LlmClient;
import com.erp.ai.common.client.LlmResult;
import com.erp.ai.common.client.LlmToolCall;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.model.AssistantReply;
import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.chat.dto.ChatRequest;
import com.erp.ai.chat.dto.ChatResponse;
import com.erp.ai.chat.prompt.SystemPromptLoader;
import com.erp.ai.common.observability.AiCallLog;
import com.erp.ai.common.observability.PromptVersions;
import com.erp.ai.common.parse.ReplyParser;
import com.erp.ai.security.service.InjectionGuard;
import com.erp.ai.tool.service.ChatToolOrchestrator;
import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.service.ToolExecutor;
import com.erp.ai.tool.ToolHandler;
import com.erp.ai.tool.service.ToolRegistry;
import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.ToolTrace;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天业务编排核心。
 * <p>
 * Day18：
 * <ul>
 *   <li>默认路径 A（{@code ai.tool.chat-path=tool-calls}）：真 tool_calls 循环</li>
 *   <li>路径 B（{@code rule}）：规则预查注入</li>
 *   <li>{@code off}：不调工具</li>
 * </ul>
 */
@Service
public class ChatService {

    private static final String PATH_A_HINT = """
            
            ## 只读工具（Day18 路径 A）
            你可以使用系统提供的 function tools 查询学习假数据（queryItem / queryInventory / queryPeriodStatus）。
            规则：
            1. 查现存量必须带 itemCode + warehouse；缺参先问用户，不要编造数量。
            2. 没有写库存/过账/删单/付款工具；用户要求改账时拒绝并 need_human=true。
            3. 工具结果返回后，最终只输出业务 JSON（含 answer 与 need_human），不要编造未查询仓库的数量。
            """;

    private final LlmClient llmClient;
    private final SessionStore sessionStore;
    private final SystemPromptLoader systemPromptLoader;
    private final ReplyParser replyParser;
    private final AiProperties properties;
    private final AiCallLog aiCallLog;
    private final ChatToolOrchestrator chatToolOrchestrator;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;
    private final InjectionGuard injectionGuard;

    public ChatService(LlmClient llmClient,
                       SessionStore sessionStore,
                       SystemPromptLoader systemPromptLoader,
                       ReplyParser replyParser,
                       AiProperties properties,
                       AiCallLog aiCallLog,
                       ChatToolOrchestrator chatToolOrchestrator,
                       ToolRegistry toolRegistry,
                       ToolExecutor toolExecutor,
                       ObjectMapper objectMapper,
                       InjectionGuard injectionGuard) {
        this.llmClient = llmClient;
        this.sessionStore = sessionStore;
        this.systemPromptLoader = systemPromptLoader;
        this.replyParser = replyParser;
        this.properties = properties;
        this.aiCallLog = aiCallLog;
        this.chatToolOrchestrator = chatToolOrchestrator;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
        this.injectionGuard = injectionGuard;
    }

    public ChatResponse chat(ChatRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = sessionStore.resolveSessionId(request.getSessionId());
        long started = System.currentTimeMillis();
        String promptVersion = PromptVersions.of("erp-system", systemPromptLoader.getSystemPrompt());

        // Day23：注入/越权话术早拦（硬防线仍是无写工具）
        InjectionGuard.Verdict security = injectionGuard.inspect(request.getMessage());
        if (security.blocked()) {
            AssistantReply reply = new AssistantReply();
            reply.setAnswer(security.reason());
            reply.setNeedHuman(true);
            reply.setConfidence(0.95);
            long latency = System.currentTimeMillis() - started;
            aiCallLog.success(traceId, sessionId, llmClient.providerName(), "security-guard",
                    latency, 0, 0, 0, 0);
            ChatResponse response = new ChatResponse();
            response.setTraceId(traceId);
            response.setSessionId(sessionId);
            response.setProvider(llmClient.providerName());
            response.setModel("security-guard");
            response.setReply(reply);
            response.setLatencyMs(latency);
            response.setAttempts(0);
            response.setToolTraces(List.of());
            response.setPromptVersion(promptVersion);
            ChatResponse.Usage usage = new ChatResponse.Usage();
            response.setUsage(usage);
            return response;
        }

        String path = properties.getTool().normalizedChatPath();
        ChatToolOrchestrator.AugmentResult augment = "rule".equals(path)
                ? chatToolOrchestrator.augment(request.getMessage())
                : ChatToolOrchestrator.AugmentResult.none();

        List<ChatMessage> messages = buildMessages(sessionId, request.getMessage(), augment, path);
        List<ToolDefinition> tools = "tool-calls".equals(path) ? listReadonlyTools() : List.of();

        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;
        List<ToolTrace> traces = new ArrayList<>(augment.traces());
        int promptTokens = 0;
        int completionTokens = 0;
        String modelName = properties.getModel();

        while (attempts < maxAttempts) {
            attempts++;
            try {
                LlmResult result = runWithOptionalToolLoop(messages, tools, traces);
                promptTokens += result.getPromptTokens();
                completionTokens += result.getCompletionTokens();
                modelName = result.getModel();

                AssistantReply reply = replyParser.parse(result.getContent());
                if (augment.forceNeedHuman() || augment.writeBlocked()) {
                    reply.setNeedHuman(true);
                }
                if (ChatToolOrchestrator.isWriteIntent(request.getMessage())) {
                    reply.setNeedHuman(true);
                }

                sessionStore.append(
                        sessionId,
                        new ChatMessage("user", request.getMessage()),
                        new ChatMessage("assistant", result.getContent())
                );

                double cost = estimateCost(promptTokens, completionTokens);
                long latency = System.currentTimeMillis() - started;
                aiCallLog.success(
                        traceId,
                        sessionId,
                        llmClient.providerName(),
                        modelName,
                        latency,
                        attempts,
                        promptTokens,
                        completionTokens,
                        cost
                );

                ChatResponse response = new ChatResponse();
                response.setTraceId(traceId);
                response.setSessionId(sessionId);
                response.setProvider(llmClient.providerName());
                response.setModel(modelName);
                response.setReply(reply);
                response.setLatencyMs(latency);
                response.setAttempts(attempts);
                response.setToolTraces(List.copyOf(traces));
                response.setPromptVersion(promptVersion);

                ChatResponse.Usage usage = new ChatResponse.Usage();
                usage.setPromptTokens(promptTokens);
                usage.setCompletionTokens(completionTokens);
                usage.setTotalTokens(promptTokens + completionTokens);
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

    /**
     * 路径 A：messages = [system, user] → LLM(tools) → tool_calls? → 执行 → 回填 → 直至最终 content。
     */
    private LlmResult runWithOptionalToolLoop(List<ChatMessage> messages,
                                              List<ToolDefinition> tools,
                                              List<ToolTrace> traces) {
        int maxRounds = Math.max(1, properties.getTool().getMaxRounds());
        int promptSum = 0;
        int completionSum = 0;
        String model = properties.getModel();

        for (int round = 0; round < maxRounds; round++) {
            LlmResult result = tools.isEmpty()
                    ? llmClient.chat(messages)
                    : llmClient.chat(messages, tools);
            promptSum += result.getPromptTokens();
            completionSum += result.getCompletionTokens();
            model = result.getModel();

            if (!result.hasToolCalls()) {
                return new LlmResult(result.getContent(), model, promptSum, completionSum);
            }

            messages.add(ChatMessage.assistantToolCalls(result.getToolCalls()));
            for (LlmToolCall call : result.getToolCalls()) {
                Map<String, Object> args = parseArgs(call.argumentsJson());
                ToolResult toolResult = toolExecutor.run(call.name(), args);
                traces.add(ToolTrace.from(toolResult));
                messages.add(ChatMessage.toolResult(call.id(), call.name(), toToolContent(toolResult)));
            }
        }
        throw new IllegalStateException("tool_calls 轮次超过上限 maxRounds=" + maxRounds);
    }

    private List<ToolDefinition> listReadonlyTools() {
        return toolRegistry.all().stream().map(ToolHandler::definition).toList();
    }

    private Map<String, Object> parseArgs(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(argumentsJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("tool arguments JSON 非法: " + e.getMessage(), e);
        }
    }

    private String toToolContent(ToolResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", result.ok());
        payload.put("toolName", result.toolName());
        payload.put("data", result.data());
        payload.put("error", result.error());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{\"ok\":false,\"error\":\"serialize failed\"}";
        }
    }

    private List<ChatMessage> buildMessages(String sessionId,
                                            String userMessage,
                                            ChatToolOrchestrator.AugmentResult augment,
                                            String path) {
        List<ChatMessage> messages = new ArrayList<>();
        String system = systemPromptLoader.getSystemPrompt();
        if ("tool-calls".equals(path)) {
            system = system + PATH_A_HINT;
        }
        messages.add(new ChatMessage("system", system));
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
