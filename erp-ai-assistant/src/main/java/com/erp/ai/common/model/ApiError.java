package com.erp.ai.common.model;

import java.time.Instant;

/**
 * 统一错误响应体，避免直接把堆栈抛给前端。
 *
 * <h2>职责</h2>
 * 作为 REST 错误契约：traceId + 可读 message + 服务端时间戳。
 *
 * <h2>为何独立模型</h2>
 * 与成功业务 DTO 分离，前端可按 HTTP 状态码 + 本结构统一处理。
 *
 * <h2>与 Spring 的关系</h2>
 * 由 {@code GlobalExceptionHandler} 构造并写入 {@code ResponseEntity} body。
 *
 * <h2>学习要点</h2>
 * message 应对用户/开发者可读，但不要塞入敏感密钥或完整堆栈。
 */
public class ApiError {

    /**
     * 错误追踪 ID（与成功响应的 traceId 概念一致，便于排查）。
     */
    private String traceId;

    /**
     * 可读错误信息（校验文案、上游失败说明等）。
     */
    private String message;

    /**
     * 服务端生成时间（UTC 瞬间）；默认构造时取 {@link Instant#now()}。
     */
    private Instant timestamp = Instant.now();

    /**
     * Jackson 反序列化用无参构造。
     */
    public ApiError() {
    }

    /**
     * 便捷构造：自动填充 timestamp。
     *
     * @param traceId 追踪 ID
     * @param message 可读错误信息
     */
    public ApiError(String traceId, String message) {
        this.traceId = traceId;
        this.message = message;
    }

    /** @return 追踪 ID */
    public String getTraceId() {
        return traceId;
    }

    /** @param traceId 追踪 ID */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /** @return 错误信息 */
    public String getMessage() {
        return message;
    }

    /** @param message 错误信息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** @return 时间戳 */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** @param timestamp 时间戳 */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
