package com.erp.ai.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiCallLog {

    private static final Logger log = LoggerFactory.getLogger(AiCallLog.class);

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

    public void failure(String traceId, String sessionId, String provider, long latencyMs, int attempts, String reason) {
        log.warn(
                "ai_call failure traceId={} sessionId={} provider={} latencyMs={} attempts={} reason={}",
                traceId, sessionId, provider, latencyMs, attempts, reason
        );
    }
}
