package com.erp.ai.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 调用审计实体，映射表 {@code ai_call_audit}。
 *
 * <h2>职责</h2>
 * 持久化每次（或每轮）LLM 调用的成功/失败、耗时、Token、估算成本与错误原因，
 * 供 Day22 观测统计与排障使用。
 *
 * <h2>为何独立实体</h2>
 * 与业务会话消息表解耦：审计可异步失败而不影响主流程（见 {@code AiCallLog} 吞异常写 warn）。
 *
 * <h2>与 MyBatis / 配置的关系</h2>
 * {@code @TableName} + {@link com.erp.ai.common.mapper.AiCallAuditMapper}；
 * 插入走 XML {@code insertAudit}，聚合走 {@code selectStats}。
 *
 * <h2>学习要点</h2>
 * {@code success} 用 Integer 0/1 便于 SQL 聚合；成本为估算字段，非真实账单。
 */
@TableName("ai_call_audit")
public class AiCallAudit {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 请求追踪 ID，与 API 响应/错误体中的 traceId 对齐 */
    private String traceId;

    /** 会话 ID；匿名或单轮场景可能为空 */
    private String sessionId;

    /** LLM 逻辑提供方，如 mock / openai-compatible */
    private String provider;

    /** 实际模型名；失败记录可能为空 */
    private String model;

    /** 是否成功：1=成功，0=失败 */
    private Integer success;

    /** 端到端耗时（毫秒） */
    private Long latencyMs;

    /** 业务层尝试次数（含首次） */
    private Integer attempts;

    /** 输入 Token；失败时可能为 null */
    private Integer promptTokens;

    /** 输出 Token；失败时可能为 null */
    private Integer completionTokens;

    /** 估算成本（美元）；基于配置单价，非账单 */
    private BigDecimal estimatedCostUsd;

    /** 失败原因摘要；成功时为 null；落库前可能截断 */
    private String errorReason;

    /** 记录创建时间（库侧或应用侧写入，视 XML 而定） */
    private LocalDateTime createdAt;

    /** @return 主键 */
    public Long getId() { return id; }

    /** @param id 主键 */
    public void setId(Long id) { this.id = id; }

    /** @return 追踪 ID */
    public String getTraceId() { return traceId; }

    /** @param traceId 追踪 ID */
    public void setTraceId(String traceId) { this.traceId = traceId; }

    /** @return 会话 ID */
    public String getSessionId() { return sessionId; }

    /** @param sessionId 会话 ID */
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    /** @return provider */
    public String getProvider() { return provider; }

    /** @param provider 逻辑提供方 */
    public void setProvider(String provider) { this.provider = provider; }

    /** @return 模型名 */
    public String getModel() { return model; }

    /** @param model 模型名 */
    public void setModel(String model) { this.model = model; }

    /** @return 1 成功 / 0 失败 */
    public Integer getSuccess() { return success; }

    /** @param success 1 或 0 */
    public void setSuccess(Integer success) { this.success = success; }

    /** @return 耗时毫秒 */
    public Long getLatencyMs() { return latencyMs; }

    /** @param latencyMs 耗时毫秒 */
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    /** @return 尝试次数 */
    public Integer getAttempts() { return attempts; }

    /** @param attempts 尝试次数 */
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    /** @return prompt tokens */
    public Integer getPromptTokens() { return promptTokens; }

    /** @param promptTokens 输入 Token */
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

    /** @return completion tokens */
    public Integer getCompletionTokens() { return completionTokens; }

    /** @param completionTokens 输出 Token */
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

    /** @return 估算成本 USD */
    public BigDecimal getEstimatedCostUsd() { return estimatedCostUsd; }

    /** @param estimatedCostUsd 估算成本 */
    public void setEstimatedCostUsd(BigDecimal estimatedCostUsd) { this.estimatedCostUsd = estimatedCostUsd; }

    /** @return 错误原因 */
    public String getErrorReason() { return errorReason; }

    /** @param errorReason 错误原因 */
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }

    /** @return 创建时间 */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @param createdAt 创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
