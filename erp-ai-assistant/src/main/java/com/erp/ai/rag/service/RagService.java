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
 * RAG 编排：检索 → Gate → 拼提示词 → 调模型 → sources。
 * <p>
 * Day13：支持 keyword|vector|hybrid；空命中可跳过 LLM；弱命中强制 need_human。
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final RagCorpusIndex corpusIndex;
    private final KeywordRetriever keywordRetriever;
    private final VectorRetriever vectorRetriever;
    private final HybridRetriever hybridRetriever;
    private final RetrievalGate retrievalGate;
    private final RagPromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final ReplyParser replyParser;
    private final AiProperties properties;
    private final AiCallLog aiCallLog;

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

    public RagAskResponse ask(RagAskRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        long started = System.currentTimeMillis();

        RagRetriever retriever = selectRetriever();
        int topK = Math.max(1, properties.getRag().getTopK());
        String degradedFrom = null;

        long searchStarted = System.currentTimeMillis();
        List<RetrievedChunk> retrieved;
        try {
            retrieved = retriever.retrieve(request.getQuestion(), corpusIndex.allChunks(), topK);
        } catch (Exception e) {
            // Day24：向量/混合失败 → 关键词降级（可日志、可预期）
            if (!"keyword".equals(retriever.name())) {
                log.warn("retriever {} failed, fallback keyword: {}", retriever.name(), e.toString());
                degradedFrom = retriever.name();
                retriever = keywordRetriever;
                retrieved = keywordRetriever.retrieve(request.getQuestion(), corpusIndex.allChunks(), topK);
            } else {
                throw e;
            }
        }
        long searchMs = System.currentTimeMillis() - searchStarted;

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

        String system = gate.isWeak() ? promptBuilder.systemPromptWeak() : promptBuilder.systemPrompt();
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", system),
                new ChatMessage("user", promptBuilder.userPrompt(request.getQuestion(), gatedHits, gate.getStrength()))
        );

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
                lastError = ex;
                working = new ArrayList<>(working);
                working.add(new ChatMessage(
                        "user",
                        "上一次输出不符合要求（" + ex.getMessage() + "）。请重新只输出合法 JSON，字段必须包含 answer 与 need_human。"
                ));
            }
        }

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

    private static AssistantReply emptyMissReply(String question) {
        AssistantReply reply = new AssistantReply();
        reply.setAnswer("教材未检索到与「" + question + "」相关的片段，无法依据资料作答。"
                + "请换用教材中的术语重问，或确认文档已纳入 rag-docs。我不会编造制度条文或伪造引用。");
        reply.setNeedHuman(true);
        reply.setConfidence(0.2);
        return reply;
    }

    private RagRetriever selectRetriever() {
        String mode = properties.getRag().getRetriever();
        String normalized = mode == null ? "keyword" : mode.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "vector" -> vectorRetriever;
            case "hybrid" -> hybridRetriever;
            default -> keywordRetriever;
        };
    }

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

    private double estimateCost(int promptTokens, int completionTokens) {
        double input = (promptTokens / 1000.0) * properties.getPriceInputPer1k();
        double output = (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
        return Math.round((input + output) * 1_000_000.0) / 1_000_000.0;
    }
}
