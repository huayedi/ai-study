package com.erp.ai.store;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAiCallAuditRepository {

    private final JdbcTemplate jdbc;

    public JdbcAiCallAuditRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertSuccess(String traceId,
                              String sessionId,
                              String provider,
                              String model,
                              long latencyMs,
                              int attempts,
                              int promptTokens,
                              int completionTokens,
                              double costUsd) {
        jdbc.update("""
                        INSERT INTO ai_call_audit(
                          trace_id, session_id, provider, model, success,
                          latency_ms, attempts, prompt_tokens, completion_tokens, estimated_cost_usd, error_reason
                        ) VALUES (?,?,?,?,1,?,?,?,?,?,NULL)
                        """,
                traceId, sessionId, provider, model,
                latencyMs, attempts, promptTokens, completionTokens, costUsd);
    }

    public void insertFailure(String traceId,
                              String sessionId,
                              String provider,
                              long latencyMs,
                              int attempts,
                              String reason) {
        String clipped = reason == null ? null : (reason.length() > 500 ? reason.substring(0, 500) : reason);
        jdbc.update("""
                        INSERT INTO ai_call_audit(
                          trace_id, session_id, provider, model, success,
                          latency_ms, attempts, prompt_tokens, completion_tokens, estimated_cost_usd, error_reason
                        ) VALUES (?,?,?,NULL,0,?,?,NULL,NULL,NULL,?)
                        """,
                traceId, sessionId, provider, latencyMs, attempts, clipped);
    }
}
