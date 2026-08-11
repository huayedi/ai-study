package com.erp.ai.model.dto;

import java.time.Instant;

public class ApiError {

    private String traceId;
    private String message;
    private Instant timestamp = Instant.now();

    public ApiError() {
    }

    public ApiError(String traceId, String message) {
        this.traceId = traceId;
        this.message = message;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
