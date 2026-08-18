package com.erp.ai.rag.service;

import com.erp.ai.rag.GateDecision;
import com.erp.ai.rag.HybridRetriever;
import com.erp.ai.rag.KeywordRetriever;
import com.erp.ai.rag.RagCorpusIndex;
import com.erp.ai.rag.RagPromptBuilder;
import com.erp.ai.rag.RagRetriever;
import com.erp.ai.rag.RetrievalGate;
import com.erp.ai.rag.RetrievedChunk;
import com.erp.ai.rag.TextChunk;
import com.erp.ai.rag.VectorRetriever;
import com.erp.ai.rag.dto.RagAskRequest;
import com.erp.ai.rag.dto.RagAskResponse;
import com.erp.ai.common.client.LlmClient;
import com.erp.ai.common.client.LlmResult;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.model.AssistantReply;
import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.common.observability.AiCallLog;
import com.erp.ai.common.observability.PromptVersions;
import com.erp.ai.common.parse.ReplyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * RAG 编排服务：串联检索、门控、提示词、模型调用与响应组装。
 * <p>
 * <b>职责</b>：实现在线路径
 * {@code retrieve → gate → prompt → generate}，并处理空/弱命中策略与检索降级。
 * <p>
 * <b>RAG 流水线位置</b>：在线编排中枢（load/chunk 由索引在启动期完成）。
 * <p>
 * <b>对应 Day</b>：
 * <ul>
 *   <li>Day10+：基础 Ask；</li>
 *   <li>Day11/12：keyword|vector；</li>
 *   <li>Day13：hybrid、Gate、空命中可跳过 LLM、弱命中强制 need_human；</li>
 *   <li>Day22：promptVersion；</li>
 *   <li>Day24：searchMs/llmMs、vector/hybrid 失败 → keyword fallback。</li>
 * </ul>
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>语料：{@link RagCorpusIndex}；</li>
 *   <li>检索：{@link KeywordRetriever}/{@link VectorRetriever}/{@link HybridRetriever}；</li>
 *   <li>门控：{@link RetrievalGate} → {@link GateDecision}；</li>
 *   <li>提示：{@link RagPromptBuilder}；</li>
 *   <li>生成：{@link LlmClient} + {@link ReplyParser}；</li>
 *   <li>API：被 {@link com.erp.ai.rag.controller.RagController} 调用。</li>
 * </ul>
 */
@Service
public class RagService {

    /** 编排日志（含降级 warn） */
    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    /** 内存文本语料索引 */
    private final RagCorpusIndex corpusIndex;

    /** 关键词检索实现（亦作降级目标） */
    private final KeywordRetriever keywordRetriever;

    /** 向量检索实现 */
    private final VectorRetriever vectorRetriever;

    /** 混合检索实现 */
    private final HybridRetriever hybridRetriever;

    /** 检索门控 */
    private final RetrievalGate retrievalGate;

    /** 提示词构建 */
    private final RagPromptBuilder promptBuilder;

    /** 大模型客户端 */
    private final LlmClient llmClient;

    /** 模型 JSON 回答解析器 */
    private final ReplyParser replyParser;

    /** 全局 AI/RAG 配置 */
    private final AiProperties properties;

    /** 调用成功/失败可观测日志 */
    private final AiCallLog aiCallLog;

    /**
     * 构造编排服务（Spring 注入全部协作 Bean）。
     *
     * @param corpusIndex       文本语料索引
     * @param keywordRetriever  关键词检索器
     * @param vectorRetriever   向量检索器
     * @param hybridRetriever   混合检索器
     * @param retrievalGate     门控器
     * @param promptBuilder     提示词构建器
     * @param llmClient         LLM 客户端
     * @param replyParser       回答解析器
     * @param properties        配置
     * @param aiCallLog         调用日志
     */
    public RagService(RagCorpusIndex corpusIndex,
                      KeywordRetriever keywordRetriever,
                      VectorRetriever vectorRetriever,
                      HybridRetriever hybridRetriever,
                      RetrievalGate retrievalGate,
                      RagPromptBuilder promptBuilder,
                      LlmClient llmClient,
                      ReplyParser replyParser,
                      AiProperties properties,
                      AiCallLog aiCallLog) {
        this.corpusIndex = corpusIndex;
        this.keywordRetriever = keywordRetriever;
        this.vectorRetriever = vectorRetriever;
        this.hybridRetriever = hybridRetriever;
        this.retrievalGate = retrievalGate;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.replyParser = replyParser;
        this.properties = properties;
        this.aiCallLog = aiCallLog;
    }

    /**
     * 执行一次 RAG 问答。
     * <p>
     * <b>主流程</b>：
     * <ol>
     *   <li>选择检索器并 retrieve（失败则 keyword 降级，见 Day24）；</li>
     *   <li>{@link RetrievalGate#decide} 得到 EMPTY/WEAK/STRONG；</li>
     *   <li>空命中且配置跳过 LLM → 本地模板回复，sources 强制空；</li>
     *   <li>否则按弱/强选择 system 提示，拼 user 提示，调用 LLM 并解析；</li>
     *   <li>弱/空命中强制 {@code need_human} 与置信度上限；</li>
     *   <li>解析失败则追加纠正消息重试，直至耗尽次数后抛错。</li>
     * </ol>
     * <p>
     * <b>边界</b>：空语料/空命中/弱命中均有明确分支；禁止空命中伪造 sources。
     *
     * @param request 含用户问题的请求
     * @return 完整 {@link RagAskResponse}
     * @throws IllegalStateException 当 LLM 重试全部失败时
     * @throws RuntimeException      当 keyword 检索本身失败（无降级空间）时原样抛出
     */
    public RagAskResponse ask(RagAskRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        long started = System.currentTimeMillis();

        // —— retrieve：按配置选择 keyword/vector/hybrid ——
        RagRetriever retriever = selectRetriever();
        int topK = Math.max(1, properties.getRag().getTopK());
        // 非 null 表示发生了 Day24 降级，值为原检索器名
        String degradedFrom = null;

        long searchStarted = System.currentTimeMillis();
        List<RetrievedChunk> retrieved;
        try {
            // 正常路径：用选定检索器召回 TopK
            retrieved = retriever.retrieve(request.getQuestion(), corpusIndex.allChunks(), topK);
        } catch (Exception e) {
            // Day24：向量/混合失败 → 关键词降级（可日志、可预期）
            if (!"keyword".equals(retriever.name())) {
                log.warn("retriever {} failed, fallback keyword: {}", retriever.name(), e.toString());
                degradedFrom = retriever.name();
                retriever = keywordRetriever;
                // 降级后再次检索；若仍失败则进入下方 else 同级逻辑之外——此处直接调用
                retrieved = keywordRetriever.retrieve(request.getQuestion(), corpusIndex.allChunks(), topK);
            } else {
                // 已是 keyword：无进一步降级，向上抛出
                throw e;
            }
        }
        long searchMs = System.currentTimeMillis() - searchStarted;

        // —— gate：空 / 弱 / 强 ——
        GateDecision gate = retrievalGate.decide(retrieved, properties.getRag().getMinScore());
        List<RetrievedChunk> gatedHits = gate.getHits();
        String promptVersion = PromptVersions.of("rag-system", promptBuilder.systemPrompt());

        // 空命中：默认不调模型，sources 必须为空（禁止伪造引用）
        if (gate.isEmpty() && properties.getRag().isSkipLlmOnEmpty()) {
            long latency = System.currentTimeMillis() - started;
            aiCallLog.success(
                    traceId,
                    "rag/" + retriever.name() + "/EMPTY",
                    llmClient.providerName(),
                    "n/a",
                    latency,
                    0,
                    0,
                    0,
                    0
            );
            return buildResponse(
                    traceId,
                    retriever.name(),
                    gate,
                    emptyMissReply(request.getQuestion()),
                    List.of(),
                    latency,
                    0,
                    "n/a",
                    0,
                    0,
                    0,
                    promptVersion,
                    topK,
                    searchMs,
                    0,
                    degradedFrom
            );
        }

        // —— prompt：弱命中用加强 system；user 带 Gate 状态与资料 ——
        String system = gate.isWeak() ? promptBuilder.systemPromptWeak() : promptBuilder.systemPrompt();
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", system),
                new ChatMessage("user", promptBuilder.userPrompt(request.getQuestion(), gatedHits, gate.getStrength()))
        );

        // —— generate：带解析重试的 LLM 调用 ——
        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;
        List<ChatMessage> working = new ArrayList<>(messages);

        while (attempts < maxAttempts) {
            attempts++;
            try {
                long llmStarted = System.currentTimeMillis();
                LlmResult result = llmClient.chat(working);
                long llmMs = System.currentTimeMillis() - llmStarted;
                AssistantReply reply = replyParser.parse(result.getContent());
                // 弱/空命中：服务端再强制 need_human 与置信度封顶（双保险）
                if (gate.isWeak() || gate.isEmpty()) {
                    reply.setNeedHuman(true);
                    if (reply.getConfidence() == null || reply.getConfidence() > 0.45) {
                        reply.setConfidence(0.4);
                    }
                }
                long latency = System.currentTimeMillis() - started;
                double cost = estimateCost(result.getPromptTokens(), result.getCompletionTokens());
                aiCallLog.success(
                        traceId,
                        "rag/" + retriever.name() + "/" + gate.getStrength(),
                        llmClient.providerName(),
                        result.getModel(),
                        latency,
                        attempts,
                        result.getPromptTokens(),
                        result.getCompletionTokens(),
                        cost
                );

                // sources 只来自检索；空命中强制 []
                List<RetrievedChunk> sourceHits = gate.isEmpty() ? List.of() : gatedHits;
                return buildResponse(
                        traceId,
                        retriever.name(),
                        gate,
                        reply,
                        sourceHits,
                        latency,
                        attempts,
                        result.getModel(),
                        result.getPromptTokens(),
                        result.getCompletionTokens(),
                        cost,
                        promptVersion,
                        topK,
                        searchMs,
                        llmMs,
                        degradedFrom
                );
            } catch (RuntimeException ex) {
                // 解析失败等：追加纠正 user 消息后重试
                lastError = ex;
                working = new ArrayList<>(working);
                working.add(new ChatMessage(
                        "user",
                        "上一次输出不符合要求（" + ex.getMessage() + "）。请重新只输出合法 JSON，字段必须包含 answer 与 need_human。"
                ));
            }
        }

        // 重试耗尽：记失败日志并抛业务异常
        long latency = System.currentTimeMillis() - started;
        String reason = lastError == null ? "unknown" : lastError.getMessage();
        aiCallLog.failure(
                traceId,
                "rag/" + retriever.name() + "/" + gate.getStrength(),
                llmClient.providerName(),
                latency,
                attempts,
                reason
        );
        throw new IllegalStateException("RAG 调用失败(traceId=" + traceId + "): " + reason, lastError);
    }

    /**
     * 将编排结果写入 {@link RagAskResponse}（含 Usage 子对象）。
     *
     * @param traceId         追踪 id
     * @param retrieverName   实际检索器名（可能已降级为 keyword）
     * @param gate            门控决策
     * @param reply           结构化回答
     * @param retrieved       用于 sources 的命中（空命中应传空列表）
     * @param latency         端到端毫秒
     * @param attempts        LLM 尝试次数
     * @param model           模型名
     * @param promptTokens    输入 token
     * @param completionTokens 输出 token
     * @param cost            估算费用
     * @param promptVersion   提示版本
     * @param topK            TopK
     * @param searchMs        检索耗时
     * @param llmMs           LLM 耗时
     * @param degradedFrom    降级前来源；未降级为 {@code null}
     * @return 填好的响应对象
     */
    private RagAskResponse buildResponse(String traceId,
                                         String retrieverName,
                                         GateDecision gate,
                                         AssistantReply reply,
                                         List<RetrievedChunk> retrieved,
                                         long latency,
                                         int attempts,
                                         String model,
                                         int promptTokens,
                                         int completionTokens,
                                         double cost,
                                         String promptVersion,
                                         int topK,
                                         long searchMs,
                                         long llmMs,
                                         String degradedFrom) {
        RagAskResponse response = new RagAskResponse();
        response.setTraceId(traceId);
        response.setProvider(llmClient.providerName());
        response.setModel(model);
        response.setRetriever(retrieverName);
        response.setGate(gate.getStrength().name());
        response.setGateReason(gate.getReason());
        response.setTopScore(gate.getTopScore());
        response.setReply(reply);
        response.setSources(toSources(retrieved));
        response.setLatencyMs(latency);
        response.setAttempts(attempts);
        response.setPromptVersion(promptVersion);
        response.setTopK(topK);
        response.setSearchMs(searchMs);
        response.setLlmMs(llmMs);
        response.setDegradedFrom(degradedFrom);

        RagAskResponse.Usage usage = new RagAskResponse.Usage();
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(promptTokens + completionTokens);
        usage.setEstimatedCostUsd(cost);
        response.setUsage(usage);
        return response;
    }

    /**
     * 空命中且跳过 LLM 时的本地模板回答：明确未覆盖、要求重问、不伪造引用。
     *
     * @param question 用户原问题（嵌入提示文案）
     * @return {@code need_human=true}、低置信度的 {@link AssistantReply}
     */
    private static AssistantReply emptyMissReply(String question) {
        AssistantReply reply = new AssistantReply();
        reply.setAnswer("教材未检索到与「" + question + "」相关的片段，无法依据资料作答。"
                + "请换用教材中的术语重问，或确认文档已纳入 rag-docs。我不会编造制度条文或伪造引用。");
        reply.setNeedHuman(true);
        reply.setConfidence(0.2);
        return reply;
    }

    /**
     * 按配置 {@code ai.rag.retriever} 选择实现；未知值回退 keyword。
     *
     * @return 选定的 {@link RagRetriever}
     */
    private RagRetriever selectRetriever() {
        String mode = properties.getRag().getRetriever();
        String normalized = mode == null ? "keyword" : mode.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "vector" -> vectorRetriever;
            case "hybrid" -> hybridRetriever;
            default -> keywordRetriever;
        };
    }

    /**
     * 将命中列表投影为响应 {@link RagAskResponse.Source}；正文超过 160 字截断。
     *
     * @param retrieved 命中列表；空则返回空 sources
     * @return sources 列表
     */
    private static List<RagAskResponse.Source> toSources(List<RetrievedChunk> retrieved) {
        List<RagAskResponse.Source> sources = new ArrayList<>();
        for (RetrievedChunk item : retrieved) {
            RagAskResponse.Source source = new RagAskResponse.Source();
            TextChunk chunk = item.getChunk();
            source.setDocId(chunk.getDocId());
            source.setSection(chunk.getSection());
            source.setScore(item.getScore());
            String content = chunk.getContent();
            source.setExcerpt(content.length() > 160 ? content.substring(0, 160) + "..." : content);
            sources.add(source);
        }
        return sources;
    }

    /**
     * 按配置单价估算本次调用 USD 费用，四舍五入到 6 位小数。
     *
     * @param promptTokens     输入 token
     * @param completionTokens 输出 token
     * @return 估算费用
     */
    private double estimateCost(int promptTokens, int completionTokens) {
        double input = (promptTokens / 1000.0) * properties.getPriceInputPer1k();
        double output = (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
        return Math.round((input + output) * 1_000_000.0) / 1_000_000.0;
    }
}
