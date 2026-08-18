package com.erp.ai.common.observability;

import com.erp.ai.common.entity.AiCallAudit;
import com.erp.ai.common.mapper.AiCallAuditMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * AI 调用可观测写入口：结构化日志 + MyBatis 落库 {@code ai_call_audit}。
 *
 * <h2>职责</h2>
 * 在 Chat 等业务流程成功/失败时记录一次调用指标，供日后统计与排障。
 *
 * <h2>为何落库失败只打 warn</h2>
 * 观测是旁路：DB 抖动不应导致用户主请求失败。日志仍保留，便于补救分析。
 *
 * <h2>与 Spring 的关系</h2>
 * {@code @Component}，注入 {@link AiCallAuditMapper}；由业务服务在适当时机调用。
 *
 * <h2>学习要点</h2>
 * 失败原因落库前截断到 500 字符，防止超长堆栈撑爆列宽。
 */
@Component
public class AiCallLog {

    /** SLF4J：success 用 info，failure 用 warn */
    private static final Logger log = LoggerFactory.getLogger(AiCallLog.class);

    /** 审计插入 Mapper */
    private final AiCallAuditMapper aiCallAuditMapper;

    /**
     * @param aiCallAuditMapper 审计 Mapper
     */
    public AiCallLog(AiCallAuditMapper aiCallAuditMapper) {
        this.aiCallAuditMapper = aiCallAuditMapper;
    }

    /**
     * 记录一次成功的 AI 调用。
     * <p>
     * 先打结构化日志，再尝试 insert；insert 异常吞掉并 warn。
     *
     * @param traceId          追踪 ID
     * @param sessionId        会话 ID，可为 null
     * @param provider         逻辑提供方
     * @param model            模型名
     * @param latencyMs        耗时毫秒
     * @param attempts         尝试次数（含首次）
     * @param promptTokens     输入 Token
     * @param completionTokens 输出 Token
     * @param costUsd          估算成本（美元）
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

    /**
     * 记录一次失败的 AI 调用。
     * <p>
     * {@code reason} 超过 500 字符时截断；insert 失败同样只 warn。
     *
     * @param traceId   追踪 ID
     * @param sessionId 会话 ID，可为 null
     * @param provider  逻辑提供方
     * @param latencyMs 耗时毫秒
     * @param attempts  已尝试次数
     * @param reason    失败原因；可为 null
     */
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
