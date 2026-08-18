package com.erp.ai.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 工具调用审计实体，映射表 {@code tool_call_audit}。
 * <p>
 * <b>职责</b>：记录每次 ToolExecutor 调用的工具名、成败、参数键摘要、错误截断、耗时。
 * 这是工具域<strong>唯一允许的写库路径</strong>（审计日志），不等于业务写库存工具。
 * <p>
 * <b>为何仍无写业务工具</b>：审计写入只服务合规与手测回溯；存货/期间业务表仍禁止经工具改写。
 * <p>
 * <b>Day16 / Day18</b>：两条路径只要走 Executor.run 都会尝试落本表。
 * <p>
 * <b>上下游</b>：上游 ToolExecutor.audit；下游 ToolCallAuditMapper.insertAudit。
 */
@TableName("tool_call_audit")
public class ToolCallAudit {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 工具名 */
    private String toolName;
    /** 1=成功 0=失败 */
    private Integer ok;
    /** 参数键集合摘要（截断至约 250 字符） */
    private String argsKeys;
    /** 失败信息（截断至约 500 字符） */
    private String errorMessage;
    /** 耗时毫秒 */
    private Long latencyMs;
    /** 入库时间（可由 DB 默认填充） */
    private LocalDateTime createdAt;

    /** @return 主键 */
    public Long getId() { return id; }
    /** @param id 主键 */
    public void setId(Long id) { this.id = id; }
    /** @return 工具名 */
    public String getToolName() { return toolName; }
    /** @param toolName 工具名 */
    public void setToolName(String toolName) { this.toolName = toolName; }
    /** @return 成败标志 1/0 */
    public Integer getOk() { return ok; }
    /** @param ok 1 成功 / 0 失败 */
    public void setOk(Integer ok) { this.ok = ok; }
    /** @return 参数键摘要 */
    public String getArgsKeys() { return argsKeys; }
    /** @param argsKeys 参数键摘要 */
    public void setArgsKeys(String argsKeys) { this.argsKeys = argsKeys; }
    /** @return 错误信息 */
    public String getErrorMessage() { return errorMessage; }
    /** @param errorMessage 错误信息 */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    /** @return 耗时毫秒 */
    public Long getLatencyMs() { return latencyMs; }
    /** @param latencyMs 耗时毫秒 */
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    /** @return 创建时间 */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /** @param createdAt 创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
