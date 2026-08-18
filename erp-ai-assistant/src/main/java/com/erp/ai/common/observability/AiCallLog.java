package com.erp.ai.common.observability;

import com.erp.ai.common.entity.AiCallAudit;
import com.erp.ai.common.mapper.AiCallAuditMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * AI 调用可观测：日志 + MyBatis XML 落库 ai_call_audit。
 */
@Component
public class AiCallLog {

    private static final Logger log = LoggerFactory.getLogger(AiCallLog.class);

    private final AiCallAuditMapper aiCallAuditMapper;

    public AiCallLog(AiCallAuditMapper aiCallAuditMapper) {
        this.aiCallAuditMapper = aiCallAuditMapper;
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
            AiCallAudit row = new AiCallAudit();
            row.setTraceId(traceId);
            row.setSessionId(sessionId);
            row.setProvider(provider);
            row.setModel(model);
            row.setSuccess(1);
            row.setLatencyMs(latencyMs);
            row.setAttempts(attempts);
            row.setPromptTokens(promptTokens);
            row.setCompletionTokens(completionTokens);
            row.setEstimatedCostUsd(BigDecimal.valueOf(costUsd));
            aiCallAuditMapper.insertAudit(row);
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
            AiCallAudit row = new AiCallAudit();
            row.setTraceId(traceId);
            row.setSessionId(sessionId);
            row.setProvider(provider);
            row.setSuccess(0);
            row.setLatencyMs(latencyMs);
            row.setAttempts(attempts);
            row.setErrorReason(reason == null ? null : (reason.length() > 500 ? reason.substring(0, 500) : reason));
            aiCallAuditMapper.insertAudit(row);
        } catch (Exception e) {
            log.warn("ai_call_audit insert failure failed: {}", e.getMessage());
        }
    }
}
