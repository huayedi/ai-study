package com.erp.ai.draft.dto;

import com.erp.ai.tool.ToolTrace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Day20：采购订单草稿建议（只建议，不写业务库）。
 */
public class DraftResponse {

    private String traceId;
    private String provider;
    private String model;
    private String suggestedDocType = "采购订单";
    private Map<String, Object> fields = new LinkedHashMap<>();
    private List<String> missing = new ArrayList<>();
    private boolean needHuman = true;
    private List<String> warnings = new ArrayList<>();
    private Double confidence;
    private long latencyMs;
    private int attempts;
    private Usage usage;
    /** 可选：后置 queryItem 校验轨迹 */
    private List<ToolTrace> toolTraces = List.of();
    /** Day22 */
    private String promptVersion;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
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

    public String getSuggestedDocType() {
        return suggestedDocType;
    }

    public void setSuggestedDocType(String suggestedDocType) {
        this.suggestedDocType = suggestedDocType;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fields);
    }

    public List<String> getMissing() {
        return missing;
    }

    public void setMissing(List<String> missing) {
        this.missing = missing == null ? new ArrayList<>() : new ArrayList<>(missing);
    }

    public boolean isNeedHuman() {
        return needHuman;
    }

    public void setNeedHuman(boolean needHuman) {
        this.needHuman = needHuman;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
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

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public List<ToolTrace> getToolTraces() {
        return toolTraces;
    }

    public void setToolTraces(List<ToolTrace> toolTraces) {
        this.toolTraces = toolTraces == null ? List.of() : List.copyOf(toolTraces);
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
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
