package com.erp.ai.chat.dto;

import com.erp.ai.common.model.AssistantReply;
import com.erp.ai.tool.ToolTrace;

import java.util.List;

/**
 * 聊天接口出参 DTO（chat 域）。
 * <p>
 * <b>职责</b>：封装一轮对话的业务回复与可观测字段，便于客户端展示答案，
 * 并在学习阶段按 traceId / 用量 / 工具轨迹排查问题与观察成本。
 * <p>
 * <b>在系统中的位置</b>：由 {@link com.erp.ai.chat.service.ChatService} 组装，
 * 经 {@link com.erp.ai.chat.controller.ChatController} 序列化为 JSON 返回。
 * <p>
 * <b>对应学习 Day</b>：
 * <ul>
 *   <li>Week1：reply / sessionId / latency / attempts / usage</li>
 *   <li>Day18：{@link #toolTraces}（路径 A tool_calls 或路径 B 规则预查）</li>
 *   <li>Day22：{@link #promptVersion}（系统提示词内容 hash）</li>
 * </ul>
 * <p>
 * <b>调用链 / 上下游</b>：{@code ChatService} 在 LLM 成功或安全早拦后填充本对象；
 * 失败场景不返回本 DTO，而是抛异常由全局处理。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>除 {@link #reply} 外的字段均为观测/运维向，不改变业务语义</li>
 *   <li>{@link #toolTraces} 默认空列表，setter 对 null 归一化为 {@code List.of()}</li>
 *   <li>成本 {@link Usage#estimatedCostUsd} 为学习用估算，非云厂商账单</li>
 * </ul>
 *
 * @see ChatRequest
 * @see AssistantReply
 * @see ToolTrace
 */
public class ChatResponse {

    /** 本次请求追踪 ID（去横线 UUID），日志与排障按此检索 */
    private String traceId;

    /** 会话 ID：多轮对话请原样回传到下一次 {@link ChatRequest#setSessionId(String)} */
    private String sessionId;

    /** 实际使用的 LLM 客户端标识，例如 mock / openai-compatible；安全早拦时仍为真实 provider 名 */
    private String provider;

    /** 实际模型名；安全早拦路径会写为 {@code security-guard} 以区分未真正调模型 */
    private String model;

    /** 结构化业务回复（answer / need_human / confidence 等），由 ReplyParser 解析模型输出得到 */
    private AssistantReply reply;

    /** Token 与成本估算；安全早拦时各计数一般为 0 */
    private Usage usage;

    /** 端到端耗时（毫秒），含可能的重试与工具循环 */
    private long latencyMs;

    /** 实际尝试次数（首次 + 重试）；安全早拦时为 0 */
    private int attempts;

    /**
     * Day18：本轮 Chat 工具轨迹（路径 A 的 tool_calls 执行记录，或路径 B 规则预查痕迹）；
     * 无工具时为空列表，不为 null。
     */
    private List<ToolTrace> toolTraces = List.of();

    /**
     * Day22：提示词版本标识（对系统提示词内容做 hash），
     * 用于对照「当前生效提示」与线上日志，改 prompt 后应反映新版本。
     */
    private String promptVersion;

    /**
     * @return 请求追踪 ID
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * @param traceId 请求追踪 ID，通常由服务端生成
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * @return 会话 ID，供客户端续聊回传
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId 会话 ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return LLM 客户端 / 提供商标识
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider 提供商标识，如 mock、openai-compatible
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return 实际模型名或安全守卫标记
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
     * @return 结构化业务回复
     */
    public AssistantReply getReply() {
        return reply;
    }

    /**
     * @param reply 解析后的助手回复
     */
    public void setReply(AssistantReply reply) {
        this.reply = reply;
    }

    /**
     * @return Token 与成本估算对象；可能为未填充分字段的空 Usage
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * @param usage Token / 成本信息
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * @return 端到端耗时（毫秒）
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * @param latencyMs 端到端耗时（毫秒），应 &gt;= 0
     */
    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    /**
     * @return LLM 尝试次数（含重试）
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * @param attempts 尝试次数；安全早拦可为 0
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * @return 工具轨迹列表，永不返回 null
     */
    public List<ToolTrace> getToolTraces() {
        return toolTraces;
    }

    /**
     * 设置工具轨迹；传入 null 时归一化为不可变空列表，避免 NPE。
     *
     * @param toolTraces 本轮工具执行 / 预查轨迹，可为 null
     */
    public void setToolTraces(List<ToolTrace> toolTraces) {
        this.toolTraces = toolTraces == null ? List.of() : toolTraces;
    }

    /**
     * @return 系统提示词版本（hash）
     */
    public String getPromptVersion() {
        return promptVersion;
    }

    /**
     * @param promptVersion Day22 提示词版本标识
     */
    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    /**
     * Token 使用量与粗略成本（嵌套 DTO）。
     * <p>
     * 真实账单以云厂商控制台为准，这里只是学习用估算：
     * 由 prompt/completion token 乘以配置单价（每 1k tokens）得到。
     * <p>
     * 与外层 {@link ChatResponse} 一并序列化，字段名与常见 OpenAI usage 习惯接近，便于对照。
     */
    public static class Usage {

        /** 输入（prompt）Token 累计，含重试与工具轮次累加 */
        private int promptTokens;

        /** 输出（completion）Token 累计 */
        private int completionTokens;

        /** 合计 Token = prompt + completion */
        private int totalTokens;

        /** 估算费用（美元）；学习环境粗算，非精确账单 */
        private double estimatedCostUsd;

        /**
         * @return 输入 Token 数
         */
        public int getPromptTokens() {
            return promptTokens;
        }

        /**
         * @param promptTokens 输入 Token 数，应 &gt;= 0
         */
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        /**
         * @return 输出 Token 数
         */
        public int getCompletionTokens() {
            return completionTokens;
        }

        /**
         * @param completionTokens 输出 Token 数，应 &gt;= 0
         */
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        /**
         * @return 合计 Token 数
         */
        public int getTotalTokens() {
            return totalTokens;
        }

        /**
         * @param totalTokens 合计 Token 数
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
