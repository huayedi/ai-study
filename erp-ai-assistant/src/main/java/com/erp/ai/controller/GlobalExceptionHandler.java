package com.erp.ai.controller;

import com.erp.ai.model.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理：把异常转换成统一的 {@link ApiError} JSON。
 * <p>
 * 状态码约定（学习用）：
 * <ul>
 *   <li>400：参数校验失败 / 业务入参非法</li>
 *   <li>502：上游模型调用失败（如 401、超时）</li>
 *   <li>500：未预期内部错误</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** {@code @Valid} 校验失败，例如 message 为空 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiError(newTraceId(), message));
    }

    /** 明确的客户端错误（如解析参数语义不对） */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError(newTraceId(), ex.getMessage()));
    }

    /**
     * 常见于 LLM 调用失败。
     * 用 502 表示“网关/上游依赖失败”，便于和纯内部 500 区分。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(newTraceId(), ex.getMessage()));
    }

    /** 兜底：避免裸堆栈直接返回 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(newTraceId(), "服务内部错误: " + ex.getMessage()));
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
