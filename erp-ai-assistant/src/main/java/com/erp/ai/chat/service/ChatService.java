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
 * 聊天业务编排核心（chat 域 Service）。
 * <p>
 * <b>职责</b>：完成一轮 HTTP 对话的端到端编排——安全早拦、会话解析、工具路径选择、
 * messages 拼装、LLM 调用（含可选 tool_calls 循环）、结构化解析、强制 need_human、
 * 会话落库、用量/成本估算与审计日志，最终组装 {@link ChatResponse}。
 * <p>
 * <b>在系统中的位置</b>：chat 域中枢；上游为 {@link com.erp.ai.chat.controller.ChatController}，
 * 下游协作 {@link SessionStore}、{@link SystemPromptLoader}、{@link LlmClient}、
 * {@link ReplyParser}、{@link InjectionGuard}、{@link ChatToolOrchestrator}、
 * {@link ToolRegistry}/{@link ToolExecutor}、{@link AiCallLog} 等。
 * <p>
 * <b>对应学习 Day</b>：
 * <ul>
 *   <li>Week1：多轮会话、强制 JSON、校验失败重试、trace/token/成本</li>
 *   <li>Day18：工具路径 A（{@code tool-calls}）/ 路径 B（{@code rule}）/ {@code off}</li>
 *   <li>Day22：{@code promptVersion} 写入响应</li>
 *   <li>Day23：注入/越权话术早拦（硬防线仍是「无写工具」）</li>
 * </ul>
 * <p>
 * <b>调用链（成功主路径）</b>：
 * <pre>
 * ChatController
 *   → InjectionGuard.inspect（可早返回）
 *   → 按 ai.tool.chat-path 分支：rule 预查 / tool-calls 注册只读工具 / off
 *   → buildMessages + runWithOptionalToolLoop
 *   → ReplyParser.parse → SessionStore.append → AiCallLog.success → ChatResponse
 * </pre>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>只注册/执行只读学习工具；用户写账意图靠提示词拒绝 + need_human，不提供写库工具</li>
 *   <li>会话只写入 chat_session_message，不写 ERP 业务单据</li>
 *   <li>解析失败会带 repair hint 重试，耗尽后抛 {@link IllegalStateException}</li>
 *   <li>路径 A 的 tool_calls 轮次受 {@code ai.tool.max-rounds} 限制，防死循环</li>
 * </ul>
 *
 * @see com.erp.ai.chat.controller.ChatController
 * @see SessionStore
 * @see ChatToolOrchestrator
 */
@Service
public class ChatService {

    /**
     * Day18 路径 A 追加到 system 提示词的只读工具说明。
     * <p>
     * 为何放在代码常量而非单独文件：与 chat-path=tool-calls 强绑定，避免 off/rule 路径误加载；
     * 内容强调缺参先问、无写工具、最终仍输出业务 JSON。
     */
    private static final String PATH_A_HINT = """
            
            ## 只读工具（Day18 路径 A）
            你可以使用系统提供的 function tools 查询学习假数据（queryItem / queryInventory / queryPeriodStatus）。
            规则：
            1. 查现存量必须带 itemCode + warehouse；缺参先问用户，不要编造数量。
            2. 没有写库存/过账/删单/付款工具；用户要求改账时拒绝并 need_human=true。
            3. 工具结果返回后，最终只输出业务 JSON（含 answer 与 need_human），不要编造未查询仓库的数量。
            """;

    /** LLM 客户端（mock 或 OpenAI 兼容），实际 provider 以 Bean 配置为准 */
    private final LlmClient llmClient;

    /** 会话 ID 解析、历史读写与追加 */
    private final SessionStore sessionStore;

    /** 缓存的 ERP system 提示词加载器 */
    private final SystemPromptLoader systemPromptLoader;

    /** 将模型文本解析为 {@link AssistantReply}；失败则触发重试 */
    private final ReplyParser replyParser;

    /** 全局 AI 配置：重试次数、模型名、单价、tool 路径与轮次等 */
    private final AiProperties properties;

    /** 成功/失败调用审计日志 */
    private final AiCallLog aiCallLog;

    /** Day18 路径 B：规则预查编排，向 messages 注入工具结果文案 */
    private final ChatToolOrchestrator chatToolOrchestrator;

    /** 只读工具注册表，路径 A 用来列出 function definitions */
    private final ToolRegistry toolRegistry;

    /** 执行白名单工具并返回 {@link ToolResult} */
    private final ToolExecutor toolExecutor;

    /** JSON 序列化：解析 tool arguments、序列化 tool 回填内容 */
    private final ObjectMapper objectMapper;

    /** Day23：注入/越权话术检查，命中则不调 LLM */
    private final InjectionGuard injectionGuard;

    /**
     * 构造注入全部协作组件（由 Spring 提供）。
     *
     * @param llmClient             LLM 客户端
     * @param sessionStore          会话存储
     * @param systemPromptLoader    系统提示词
     * @param replyParser           回复 JSON 解析器
     * @param properties            AI 配置
     * @param aiCallLog             调用审计
     * @param chatToolOrchestrator  路径 B 预查编排
     * @param toolRegistry          工具注册表
     * @param toolExecutor          工具执行器
     * @param objectMapper          Jackson ObjectMapper
     * @param injectionGuard        注入守卫
     */
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

    /**
     * 处理一轮聊天请求的主入口。
     * <p>
     * 业务含义：在既定安全与只读工具约束下，为用户生成结构化助手回复，并维护多轮会话。
     * <p>
     * 边界条件：
     * <ul>
     *   <li>注入命中：不调模型，直接返回 needHuman 回复，attempts=0</li>
     *   <li>重试耗尽：记 failure 日志并抛 {@link IllegalStateException}</li>
     *   <li>写意图 / 预查强制：成功解析后仍可能把 needHuman 置 true</li>
     * </ul>
     *
     * @param request 已通过 Bean Validation 的入参（message 非空）
     * @return 完整 {@link ChatResponse}（含观测字段）
     * @throws IllegalStateException 多次尝试后 LLM/解析仍失败时
     */
    public ChatResponse chat(ChatRequest request) {
        // 去横线 UUID：日志检索更紧凑；与 draft 域 trace 风格一致
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionId = sessionStore.resolveSessionId(request.getSessionId());
        long started = System.currentTimeMillis();
        // Day22：对当前缓存 system 提示做版本标识，便于对照改 prompt 前后行为
        String promptVersion = PromptVersions.of("erp-system", systemPromptLoader.getSystemPrompt());

        // Day23：注入/越权话术早拦——省成本且快速拒绝；硬防线仍是系统无写工具
        InjectionGuard.Verdict security = injectionGuard.inspect(request.getMessage());
        if (security.blocked()) {
            AssistantReply reply = new AssistantReply();
            reply.setAnswer(security.reason());
            reply.setNeedHuman(true);
            // 高置信：规则命中而非模型猜测
            reply.setConfidence(0.95);
            long latency = System.currentTimeMillis() - started;
            // model 记为 security-guard，审计上区分「未真正调 LLM」
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

        // 归一化路径：tool-calls（A）/ rule（B）/ off
        String path = properties.getTool().normalizedChatPath();
        // 仅 rule 路径做预查注入；其它路径给 none，避免误注入
        ChatToolOrchestrator.AugmentResult augment = "rule".equals(path)
                ? chatToolOrchestrator.augment(request.getMessage())
                : ChatToolOrchestrator.AugmentResult.none();

        List<ChatMessage> messages = buildMessages(sessionId, request.getMessage(), augment, path);
        // 仅路径 A 把只读工具定义传给 LLM；off/rule 传空列表 → 走无 tools 的 chat 重载
        List<ToolDefinition> tools = "tool-calls".equals(path) ? listReadonlyTools() : List.of();

        // maxRetries 为「额外重试次数」，总尝试 = 1 + retries，且至少 1
        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;
        // 预查轨迹先放入；路径 A 后续 tool 执行再追加
        List<ToolTrace> traces = new ArrayList<>(augment.traces());
        int promptTokens = 0;
        int completionTokens = 0;
        String modelName = properties.getModel();

        while (attempts < maxAttempts) {
            attempts++;
            try {
                LlmResult result = runWithOptionalToolLoop(messages, tools, traces);
                // 跨重试累加 token，便于观察「修 JSON」成本
                promptTokens += result.getPromptTokens();
                completionTokens += result.getCompletionTokens();
                modelName = result.getModel();

                AssistantReply reply = replyParser.parse(result.getContent());
                // 预查判定写意图或需人工：覆盖模型可能偏乐观的 need_human
                if (augment.forceNeedHuman() || augment.writeBlocked()) {
                    reply.setNeedHuman(true);
                }
                // 双保险：即使用户话术逃过预查，写意图关键字仍强制人工
                if (ChatToolOrchestrator.isWriteIntent(request.getMessage())) {
                    reply.setNeedHuman(true);
                }

                // 仅成功路径落库：失败重试不污染历史
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
                // 解析失败或工具循环异常：记下原因，给模型加 repair 提示再试
                lastError = ex;
                messages = withRepairHint(messages, ex.getMessage());
            }
        }

        long latency = System.currentTimeMillis() - started;
        String reason = lastError == null ? "unknown" : lastError.getMessage();
        aiCallLog.failure(traceId, sessionId, llmClient.providerName(), latency, attempts, reason);
        // 把 traceId 带进异常消息，便于客户端/日志对齐
        throw new IllegalStateException("AI 调用失败(traceId=" + traceId + "): " + reason, lastError);
    }

    /**
     * 可选工具循环：无 tools 时单次 chat；有 tools 时按 Day18 路径 A 循环直至无 tool_calls。
     * <p>
     * 路径 A 形状：messages = [system, history..., user, ...] → LLM(tools)
     * → 若有 tool_calls 则执行并回填 tool 消息 → 再调 LLM，直到最终 content 或超轮次。
     * <p>
     * 为何累加 token：一轮对话可能多次 LLM 往返，对外只报合计用量。
     *
     * @param messages 可变消息列表；本方法可能向其中追加 assistant tool_calls 与 tool 结果
     * @param tools    工具定义列表；空则不走 tool_calls 分支
     * @param traces   输出参数：每次工具执行追加一条 {@link ToolTrace}
     * @return 最终文本结果，token 为循环内合计
     * @throws IllegalStateException 超过 {@code maxRounds} 仍持续 tool_calls 时
     */
    private LlmResult runWithOptionalToolLoop(List<ChatMessage> messages,
                                              List<ToolDefinition> tools,
                                              List<ToolTrace> traces) {
        int maxRounds = Math.max(1, properties.getTool().getMaxRounds());
        int promptSum = 0;
        int completionSum = 0;
        String model = properties.getModel();

        for (int round = 0; round < maxRounds; round++) {
            // 无工具定义时走单参数 chat，避免向不支持 tools 的 mock 路径传空列表语义差异
            LlmResult result = tools.isEmpty()
                    ? llmClient.chat(messages)
                    : llmClient.chat(messages, tools);
            promptSum += result.getPromptTokens();
            completionSum += result.getCompletionTokens();
            model = result.getModel();

            // 模型直接给最终 content：结束循环（路径 off / 或工具已够用）
            if (!result.hasToolCalls()) {
                return new LlmResult(result.getContent(), model, promptSum, completionSum);
            }

            // 先把「助手发起的 tool_calls」写入上下文，再逐个执行并回填 role=tool
            messages.add(ChatMessage.assistantToolCalls(result.getToolCalls()));
            for (LlmToolCall call : result.getToolCalls()) {
                Map<String, Object> args = parseArgs(call.argumentsJson());
                ToolResult toolResult = toolExecutor.run(call.name(), args);
                traces.add(ToolTrace.from(toolResult));
                // 回填内容是结构化 JSON 字符串，便于模型下一轮引用
                messages.add(ChatMessage.toolResult(call.id(), call.name(), toToolContent(toolResult)));
            }
        }
        // 防模型反复瞎调工具：硬上限后失败，由外层重试或最终失败
        throw new IllegalStateException("tool_calls 轮次超过上限 maxRounds=" + maxRounds);
    }

    /**
     * 从注册表收集全部工具的 OpenAI function 定义（学习环境均为只读查询）。
     *
     * @return 工具定义列表；注册表为空时为空列表
     */
    private List<ToolDefinition> listReadonlyTools() {
        return toolRegistry.all().stream().map(ToolHandler::definition).toList();
    }

    /**
     * 将模型返回的 tool arguments JSON 解析为 Map。
     * <p>
     * 边界：null/空白视为无参空 Map；非法 JSON 抛 {@link IllegalArgumentException} 触发外层重试。
     *
     * @param argumentsJson 模型给出的参数 JSON 字符串
     * @return 参数 Map，可能为空
     * @throws IllegalArgumentException JSON 非法时
     */
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

    /**
     * 将 {@link ToolResult} 序列化为回填模型的 JSON 文本。
     * <p>
     * 为何固定字段 ok/toolName/data/error：让模型用统一 schema 读工具结果，减少胡编。
     * 序列化失败时返回固定错误 JSON，避免中断整轮循环时丢失可观测性。
     *
     * @param result 工具执行结果
     * @return JSON 字符串
     */
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

    /**
     * 组装发往 LLM 的消息列表：system（可含路径 A 提示）+ 历史 + 本轮 user + 可选预查注入。
     * <p>
     * 路径 B 的 injection 以额外 user 消息形式追加，模拟「系统已查到的事实」而不改 session 落库内容
     *（落库仍只存真实用户话与最终 assistant content）。
     *
     * @param sessionId   会话 ID，用于拉历史
     * @param userMessage 本轮用户原文
     * @param augment     路径 B 预查结果；可为 none
     * @param path        归一化后的 chat-path
     * @return 新的可变消息列表（不含本轮尚未产生的 assistant）
     */
    private List<ChatMessage> buildMessages(String sessionId,
                                            String userMessage,
                                            ChatToolOrchestrator.AugmentResult augment,
                                            String path) {
        List<ChatMessage> messages = new ArrayList<>();
        String system = systemPromptLoader.getSystemPrompt();
        if ("tool-calls".equals(path)) {
            // 仅路径 A 追加工具说明，避免 rule/off 被无关 tools 文案干扰
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

    /**
     * 在消息末尾追加「请按 schema 重输出」的 user 提示，用于解析失败后的修复重试。
     * <p>
     * 为何复制列表：避免污染上一轮尝试的 messages 引用语义不清；每轮重试基于带 hint 的新列表。
     *
     * @param messages 当前消息列表
     * @param error    上一轮失败原因（写入提示文本）
     * @return 带 repair hint 的新列表
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
     * 按配置单价估算本次累计 token 费用（美元），并四舍五入到 6 位小数。
     * <p>
     * 学习用粗算，非云厂商精确账单。
     *
     * @param promptTokens     累计输入 token
     * @param completionTokens 累计输出 token
     * @return 估算费用（美元）
     */
    private double estimateCost(int promptTokens, int completionTokens) {
        double input = (promptTokens / 1000.0) * properties.getPriceInputPer1k();
        double output = (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
        return Math.round((input + output) * 1_000_000.0) / 1_000_000.0;
    }
}
