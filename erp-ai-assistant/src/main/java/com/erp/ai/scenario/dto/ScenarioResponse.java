package com.erp.ai.scenario.dto;

/**
 * Day27：场景分类 / 轻量编排响应。
 */
public class ScenarioResponse {

    private String traceId;
    private String mode;
    private String reason;
    private double confidence;
    private String routedApi;
    private Object payload;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getRoutedApi() {
        return routedApi;
    }

    public void setRoutedApi(String routedApi) {
        this.routedApi = routedApi;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
