package com.erp.ai.common.exception;

import com.erp.ai.common.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理：把未捕获异常转换成统一的 {@link ApiError} JSON。
 *
 * <h2>职责</h2>
 * 避免堆栈直接泄漏给前端；按异常类型映射 HTTP 状态码，并附带独立 traceId 便于排障。
 *
 * <h2>为何用 {@code @RestControllerAdvice}</h2>
 * 横切所有 {@code @RestController}，业务代码无需每个接口 try/catch。
 *
 * <h2>状态码约定（学习用）</h2>
 * <ul>
 *   <li>400：参数校验失败 / 业务入参非法（含模型输出解析失败若以 IllegalArgument 抛出）</li>
 *   <li>502：上游模型调用失败（如 401、超时）——用 IllegalStateException 表达</li>
 *   <li>500：未预期内部错误</li>
 * </ul>
 *
 * <h2>学习要点</h2>
 * 这里生成的 traceId 与业务成功路径的 traceId 可能不同来源；排障时以响应体为准。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * {@code @Valid} 校验失败，例如 message 为空。
     * <p>
     * 将所有字段错误信息用 {@code ;} 拼接进 {@link ApiError#getMessage()}。
     *
     * @param ex Bean Validation 异常
     * @return HTTP 400 + ApiError
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiError(newTraceId(), message));
    }

    /**
     * 明确的客户端错误（如解析参数语义不对、模型 JSON 缺字段）。
     *
     * @param ex 非法参数异常
     * @return HTTP 400 + ApiError（message 取异常原文）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError(newTraceId(), ex.getMessage()));
    }

    /**
     * 常见于 LLM / Embedding 调用失败。
     * <p>
     * 用 502 表示「网关/上游依赖失败」，便于和纯内部 500 区分。
     *
     * @param ex 非法状态（配置缺失、HTTP 失败等）
     * @return HTTP 502 + ApiError
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(newTraceId(), ex.getMessage()));
    }

    /**
     * 兜底：避免裸堆栈直接返回。
     * <p>
     * 仅暴露简短 message；完整堆栈应落在服务端日志。
     *
     * @param ex 任意其它异常
     * @return HTTP 500 + ApiError
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(newTraceId(), "服务内部错误: " + ex.getMessage()));
    }

    /**
     * 生成无横杠的 UUID 作为错误追踪 ID。
     *
     * @return 32 位十六进制字符串
     */
    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
