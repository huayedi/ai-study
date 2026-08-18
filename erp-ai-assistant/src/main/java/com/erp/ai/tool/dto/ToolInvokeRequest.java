package com.erp.ai.tool.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Day16：手动调用工具（学习手测 / 控制台）。
 */
public class ToolInvokeRequest {

    @NotBlank(message = "toolName 不能为空")
    private String toolName;

    private Map<String, Object> args = Map.of();

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args == null ? Map.of() : args;
    }
}
