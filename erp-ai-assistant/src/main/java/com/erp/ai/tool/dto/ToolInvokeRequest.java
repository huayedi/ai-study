package com.erp.ai.tool.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Day16：手动调用工具请求体（学习手测 / 控制台）。
 * <p>
 * <b>职责</b>：承载 HTTP invoke 的工具名与参数 Map。
 * <p>
 * <b>只读 / 禁写</b>：本 DTO 可携带任意 toolName 字符串，但 Executor 会对禁写名硬拦；
 * 不存在「写库存请求体」专用字段——因为系统不提供写工具。
 * <p>
 * <b>上下游</b>：上游 HTTP JSON；下游 {@code ToolController.invoke} → {@code ToolExecutor.run}。
 */
public class ToolInvokeRequest {

    /** 白名单工具名，如 queryItem；空白将被 Bean Validation 拒绝 */
    @NotBlank(message = "toolName 不能为空")
    private String toolName;

    /** 工具参数；null 在 setter 中归一为空 Map */
    private Map<String, Object> args = Map.of();

    /**
     * @return 工具名
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * @param toolName 工具名
     */
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    /**
     * @return 参数 Map（永不为 null）
     */
    public Map<String, Object> getArgs() {
        return args;
    }

    /**
     * 设置参数；传入 null 时置为空不可变 Map，避免 NPE。
     *
     * @param args 参数，可为 null
     */
    public void setArgs(Map<String, Object> args) {
        this.args = args == null ? Map.of() : args;
    }
}
