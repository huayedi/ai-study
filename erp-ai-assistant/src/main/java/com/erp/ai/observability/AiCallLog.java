package com.erp.ai.observability;

import com.erp.ai.store.JdbcAiCallAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AI 调用可观测：日志 + 学习库 {@code ai_call_audit} 落库。
 */
@Component
public class AiCallLog {

    private static final Logger log = LoggerFactory.getLogger(AiCallLog.class);

    private final JdbcAiCallAuditRepository auditRepository;

    public AiCallLog(JdbcAiCallAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

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
        try {
            auditRepository.insertSuccess(
                    traceId, sessionId, provider, model, latencyMs, attempts, promptTokens, completionTokens, costUsd);
        } catch (Exception e) {
            log.warn("ai_call_audit insert success failed: {}", e.getMessage());
        }
    }

    public void failure(String traceId, String sessionId, String provider, long latencyMs, int attempts, String reason) {
        log.warn(
                "ai_call failure traceId={} sessionId={} provider={} latencyMs={} attempts={} reason={}",
                traceId, sessionId, provider, latencyMs, attempts, reason
        );
        try {
            auditRepository.insertFailure(traceId, sessionId, provider, latencyMs, attempts, reason);
        } catch (Exception e) {
            log.warn("ai_call_audit insert failure failed: {}", e.getMessage());
        }
    }
}
