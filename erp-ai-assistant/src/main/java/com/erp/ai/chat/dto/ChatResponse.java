package com.erp.ai.chat.dto;

import com.erp.ai.common.model.AssistantReply;
import com.erp.ai.tool.ToolTrace;

import java.util.List;

/**
 * 聊天接口出参 DTO。
 * <p>
 * 除业务回复 {@link #reply} 外，额外返回可观测字段（trace、用量、耗时），
 * 方便学习阶段排查问题和观察成本。
 */
public class ChatResponse {

    /** 本次请求追踪 ID，日志里可按它检索 */
    private String traceId;

    /** 会话 ID，多轮对话请原样回传 */
    private String sessionId;

    /** 实际使用的客户端标识：mock / openai-compatible */
    private String provider;

    /** 实际模型名 */
    private String model;

    /** 结构化业务回复 */
    private AssistantReply reply;

    /** Token 与成本估算 */
    private Usage usage;

    /** 端到端耗时（毫秒），含可能的重试 */
    private long latencyMs;

    /** 实际尝试次数（首次 + 重试） */
    private int attempts;

    /**
     * Day18：本轮 Chat 预查工具轨迹（路径 B）；无工具时为空列表。
     */
    private List<ToolTrace> toolTraces = List.of();

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public AssistantReply getReply() {
        return reply;
    }

    public void setReply(AssistantReply reply) {
        this.reply = reply;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public List<ToolTrace> getToolTraces() {
        return toolTraces;
    }

    public void setToolTraces(List<ToolTrace> toolTraces) {
        this.toolTraces = toolTraces == null ? List.of() : toolTraces;
    }

    /**
     * Token 使用量与粗略成本。
     * 真实账单以云厂商控制台为准，这里只是学习用估算。
     */
    public static class Usage {

        /** 输入 Token */
        private int promptTokens;

        /** 输出 Token */
        private int completionTokens;

        /** 合计 Token */
        private int totalTokens;

        /** 估算费用（美元） */
        private double estimatedCostUsd;

        public int getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }

        public double getEstimatedCostUsd() {
            return estimatedCostUsd;
        }

        public void setEstimatedCostUsd(double estimatedCostUsd) {
            this.estimatedCostUsd = estimatedCostUsd;
        }
    }
}
