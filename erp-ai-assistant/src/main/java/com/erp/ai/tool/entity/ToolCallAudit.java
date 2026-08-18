package com.erp.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("tool_call_audit")
public class ToolCallAudit {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String toolName;
    private Integer ok;
    private String argsKeys;
    private String errorMessage;
    private Long latencyMs;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public Integer getOk() { return ok; }
    public void setOk(Integer ok) { this.ok = ok; }
    public String getArgsKeys() { return argsKeys; }
    public void setArgsKeys(String argsKeys) { this.argsKeys = argsKeys; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
