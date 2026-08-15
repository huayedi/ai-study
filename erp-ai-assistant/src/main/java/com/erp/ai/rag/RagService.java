package com.erp.ai.rag;

import com.erp.ai.client.LlmClient;
import com.erp.ai.client.LlmResult;
import com.erp.ai.config.AiProperties;
import com.erp.ai.model.AssistantReply;
import com.erp.ai.model.ChatMessage;
import com.erp.ai.observability.AiCallLog;
import com.erp.ai.rag.dto.RagAskRequest;
import com.erp.ai.rag.dto.RagAskResponse;
import com.erp.ai.service.ReplyParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RAG 编排：检索教材 → 拼提示词 → 调模型 → 返回回答与 sources。
 */
@Service
public class RagService {

    private final RagCorpusIndex corpusIndex;
    private final KeywordRetriever retriever;
    private final RagPromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final ReplyParser replyParser;
    private final AiProperties properties;
    private final AiCallLog aiCallLog;

    public RagService(RagCorpusIndex corpusIndex,
                      KeywordRetriever retriever,
                      RagPromptBuilder promptBuilder,
                      LlmClient llmClient,
                      ReplyParser replyParser,
                      AiProperties properties,
                      AiCallLog aiCallLog) {
        this.corpusIndex = corpusIndex;
        this.retriever = retriever;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.replyParser = replyParser;
        this.properties = properties;
        this.aiCallLog = aiCallLog;
    }

    public RagAskResponse ask(RagAskRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        long started = System.currentTimeMillis();

        int topK = Math.max(1, properties.getRag().getTopK());
        List<RetrievedChunk> retrieved = retriever.retrieve(
                request.getQuestion(),
                corpusIndex.allChunks(),
                topK
        );

        List<ChatMessage> messages = List.of(
                new ChatMessage("system", promptBuilder.systemPrompt()),
                new ChatMessage("user", promptBuilder.userPrompt(request.getQuestion(), retrieved))
        );

        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;
        List<ChatMessage> working = new ArrayList<>(messages);

        while (attempts < maxAttempts) {
            attempts++;
            try {
                LlmResult result = llmClient.chat(working);
                AssistantReply reply = replyParser.parse(result.getContent());
                long latency = System.currentTimeMillis() - started;
                double cost = estimateCost(result.getPromptTokens(), result.getCompletionTokens());
                aiCallLog.success(
                        traceId,
                        "rag",
                        llmClient.providerName(),
                        result.getModel(),
                        latency,
                        attempts,
                        result.getPromptTokens(),
                        result.getCompletionTokens(),
                        cost
                );

                RagAskResponse response = new RagAskResponse();
                response.setTraceId(traceId);
                response.setProvider(llmClient.providerName());
                response.setModel(result.getModel());
                response.setReply(reply);
                response.setSources(toSources(retrieved));
                response.setLatencyMs(latency);
                response.setAttempts(attempts);

                RagAskResponse.Usage usage = new RagAskResponse.Usage();
                usage.setPromptTokens(result.getPromptTokens());
                usage.setCompletionTokens(result.getCompletionTokens());
                usage.setTotalTokens(result.getTotalTokens());
                usage.setEstimatedCostUsd(cost);
                response.setUsage(usage);
                return response;
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
        aiCallLog.failure(traceId, "rag", llmClient.providerName(), latency, attempts, reason);
        throw new IllegalStateException("RAG 调用失败(traceId=" + traceId + "): " + reason, lastError);
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
