package com.erp.ai.draft.dto;

import com.erp.ai.tool.ToolTrace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Day20：采购订单草稿建议出参 DTO（draft 域）。
 * <p>
 * <b>职责</b>：承载「只建议、不写库」的结构化草稿结果：建议单据类型、字段 Map、
 * 缺失项、警告、是否需人工、置信度，以及 trace/用量/工具轨迹等观测字段。
 * <p>
 * <b>在系统中的位置</b>：由 {@link com.erp.ai.draft.service.DraftParser} 填充业务字段，
 * 再由 {@link com.erp.ai.draft.service.DraftService} 补齐观测与后置规则后，
 * 经 {@link com.erp.ai.draft.controller.DraftController} 返回。
 * <p>
 * <b>对应学习 Day</b>：Day20 草稿辅助；Day22 增加 {@link #promptVersion}。
 * <p>
 * <b>调用链 / 上下游</b>：模型 JSON → DraftParser →（相对日期规则 / queryItem 校验 /
 * 必填与 needHuman 强制）→ 本对象完整形态 → HTTP JSON。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li><b>不写业务库</b>：本 DTO 即最终产物，无后续 insert 订单步骤</li>
 *   <li><b>无 session</b>：响应不含 sessionId</li>
 *   <li>{@link #needHuman} 默认 true；Service 层结束时仍会强制 true（建议 ≠ 过账）</li>
 *   <li>集合类 setter 对 null 做防御性拷贝 / 空列表，避免 NPE</li>
 * </ul>
 *
 * @see DraftRequest
 * @see com.erp.ai.draft.service.DraftService
 */
public class DraftResponse {

    /** 本次请求追踪 ID（去横线 UUID），排障与审计用 */
    private String traceId;

    /** LLM 客户端标识，如 mock / openai-compatible */
    private String provider;

    /** 实际模型名 */
    private String model;

    /** 建议单据类型文案；缺省「采购订单」，解析器可覆盖 */
    private String suggestedDocType = "采购订单";

    /**
     * 建议填写的业务字段键值（如 供应商、存货编码、数量、仓库、含税单价、交货日期）。
     * 使用 LinkedHashMap 保持模型/规则写入顺序，便于展示。
     */
    private Map<String, Object> fields = new LinkedHashMap<>();

    /** 仍缺失或无效、需用户补全的字段名列表 */
    private List<String> missing = new ArrayList<>();

    /**
     * 是否必须人工确认。
     * 默认 true；草稿语义下 Service 最终仍会强制为 true。
     */
    private boolean needHuman = true;

    /** 非阻断提示（如相对日期换算说明、queryItem 未找到等） */
    private List<String> warnings = new ArrayList<>();

    /** 模型或规则给出的置信度；可为 null（未提供） */
    private Double confidence;

    /** 端到端耗时（毫秒），含重试 */
    private long latencyMs;

    /** LLM 尝试次数（首次 + 重试） */
    private int attempts;

    /** Token 与成本估算 */
    private Usage usage;

    /**
     * 可选：后置 queryItem 校验产生的工具轨迹；无校验时为空列表。
     */
    private List<ToolTrace> toolTraces = List.of();

    /**
     * Day22：草稿专用提示词版本（对 draft prompt 内容 hash）。
     */
    private String promptVersion;

    /**
     * @return 请求追踪 ID
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * @param traceId 请求追踪 ID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * @return 提供商标识
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider 提供商标识
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return 模型名
     */
    public String getModel() {
        return model;
    }

    /**
     * @param model 模型名
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return 建议单据类型
     */
    public String getSuggestedDocType() {
        return suggestedDocType;
    }

    /**
     * @param suggestedDocType 建议单据类型文案；空时下游可能再默认「采购订单」
     */
    public void setSuggestedDocType(String suggestedDocType) {
        this.suggestedDocType = suggestedDocType;
    }

    /**
     * @return 建议字段 Map（可变，后置规则可能继续 put）
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    /**
     * 设置字段 Map；null 时置为空 LinkedHashMap，非 null 则拷贝，避免外部引用被意外共享污染。
     *
     * @param fields 建议字段，可为 null
     */
    public void setFields(Map<String, Object> fields) {
        this.fields = fields == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fields);
    }

    /**
     * @return 缺失字段列表
     */
    public List<String> getMissing() {
        return missing;
    }

    /**
     * 设置缺失列表；null → 空 ArrayList；非 null → 拷贝。
     *
     * @param missing 缺失项，可为 null
     */
    public void setMissing(List<String> missing) {
        this.missing = missing == null ? new ArrayList<>() : new ArrayList<>(missing);
    }

    /**
     * @return 是否需人工确认
     */
    public boolean isNeedHuman() {
        return needHuman;
    }

    /**
     * @param needHuman 是否需人工确认
     */
    public void setNeedHuman(boolean needHuman) {
        this.needHuman = needHuman;
    }

    /**
     * @return 警告列表
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * 设置警告列表；null → 空 ArrayList；非 null → 拷贝。
     *
     * @param warnings 警告文案，可为 null
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings == null ? new ArrayList<>() : new ArrayList<>(warnings);
    }

    /**
     * @return 置信度；可能为 null
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * @param confidence 置信度，可为 null
     */
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    /**
     * @return 耗时毫秒
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * @param latencyMs 耗时毫秒，应 &gt;= 0
     */
    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    /**
     * @return 尝试次数
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * @param attempts 尝试次数
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * @return Token / 成本用量
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * @param usage Token / 成本用量
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * @return 工具轨迹；默认非 null
     */
    public List<ToolTrace> getToolTraces() {
        return toolTraces;
    }

    /**
     * 设置工具轨迹；null → 空不可变列表；非 null → 不可变拷贝，防止外部再改。
     *
     * @param toolTraces 轨迹列表，可为 null
     */
    public void setToolTraces(List<ToolTrace> toolTraces) {
        this.toolTraces = toolTraces == null ? List.of() : List.copyOf(toolTraces);
    }

    /**
     * @return 草稿提示词版本
     */
    public String getPromptVersion() {
        return promptVersion;
    }

    /**
     * @param promptVersion Day22 提示词版本
     */
    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    /**
     * Token 使用量与粗略成本（嵌套 DTO）。
     * <p>
     * 学习用估算，真实账单以云厂商为准；由 DraftService 按配置单价计算后填入。
     */
    public static class Usage {

        /** 输入 Token */
        private int promptTokens;

        /** 输出 Token */
        private int completionTokens;

        /** 合计 Token */
        private int totalTokens;

        /** 估算费用（美元） */
        private double estimatedCostUsd;

        /**
         * @return 输入 Token
         */
        public int getPromptTokens() {
            return promptTokens;
        }

        /**
         * @param promptTokens 输入 Token，应 &gt;= 0
         */
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        /**
         * @return 输出 Token
         */
        public int getCompletionTokens() {
            return completionTokens;
        }

        /**
         * @param completionTokens 输出 Token，应 &gt;= 0
         */
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        /**
         * @return 合计 Token
         */
        public int getTotalTokens() {
            return totalTokens;
        }

        /**
         * @param totalTokens 合计 Token
         */
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }

        /**
         * @return 估算费用（美元）
         */
        public double getEstimatedCostUsd() {
            return estimatedCostUsd;
        }

        /**
         * @param estimatedCostUsd 估算费用（美元）
         */
        public void setEstimatedCostUsd(double estimatedCostUsd) {
            this.estimatedCostUsd = estimatedCostUsd;
        }
    }
}
