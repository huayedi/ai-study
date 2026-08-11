package com.erp.ai.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AI 调用可观测日志。
 * <p>
 * 学习阶段先用结构化日志打点；后续可替换/补充为 OpenTelemetry metrics/traces。
 * 排查问题时优先按 {@code traceId} 搜索。
 */
@Component
public class AiCallLog {

    private static final Logger log = LoggerFactory.getLogger(AiCallLog.class);

    /**
     * 记录一次成功调用：耗时、重试次数、Token、估算成本。
     */
    public void success(String traceId,
                        String sessionId,
                        String provider,
                        String model,
                        long latencyMs,
                        int attempts,
                        int promptTokens,
                        int completionTokens,
                        double costUsd) {
        log.info(
                "ai_call success traceId={} sessionId={} provider={} model={} latencyMs={} attempts={} promptTokens={} completionTokens={} estimatedCostUsd={}",
                traceId, sessionId, provider, model, latencyMs, attempts, promptTokens, completionTokens, costUsd
        );
    }

    /**
     * 记录一次最终失败（已耗尽重试）。
     */
    public void failure(String traceId, String sessionId, String provider, long latencyMs, int attempts, String reason) {
        log.warn(
                "ai_call failure traceId={} sessionId={} provider={} latencyMs={} attempts={} reason={}",
                traceId, sessionId, provider, latencyMs, attempts, reason
        );
    }
}
