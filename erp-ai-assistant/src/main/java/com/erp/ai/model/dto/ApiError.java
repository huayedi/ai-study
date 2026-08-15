package com.erp.ai.model.dto;

import java.time.Instant;

/**
 * 统一错误响应体，避免直接把堆栈抛给前端。
 */
public class ApiError {

    /** 错误追踪 ID（与成功响应的 traceId 概念一致，便于排查） */
    private String traceId;

    /** 可读错误信息 */
    private String message;

    /** 服务端生成时间 */
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
