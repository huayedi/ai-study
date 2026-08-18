package com.erp.ai.rag.dto;

import com.erp.ai.common.model.AssistantReply;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG 问答响应 DTO：回答正文 + 引用 sources + 门控与可观测性字段。
 * <p>
 * <b>职责</b>：向客户端暴露一次 Ask 调用的完整结果，便于学习项目观察
 * retriever / gate / 耗时 / 降级等中间状态。
 * <p>
 * <b>RAG 流水线位置</b>：generate 之后的 API 输出（汇总 retrieve/gate/prompt/generate 元数据）。
 * <p>
 * <b>对应 Day</b>：Day10+ 基础响应；Day11/13 增加 retriever/gate；Day22 promptVersion；Day24 searchMs/llmMs/degradedFrom。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>由 {@link com.erp.ai.rag.service.RagService} 组装；</li>
 *   <li>{@link #reply} 类型为通用 {@link AssistantReply}；</li>
 *   <li>嵌套 {@link Source} 来自 {@link com.erp.ai.rag.RetrievedChunk} 投影；</li>
 *   <li>{@link #gate} 对应 {@link com.erp.ai.rag.GateStrength} 枚举名。</li>
 * </ul>
 */
public class RagAskResponse {

    /** 本次调用追踪 id（无连字符 UUID），便于日志串联 */
    private String traceId;

    /** LLM 提供方名称（空命中跳过模型时仍可能填写客户端 provider 名） */
    private String provider;

    /** 实际调用的模型名；跳过 LLM 时可为 {@code n/a} */
    private String model;

    /** 实际生效的检索器名：{@code keyword} | {@code vector} | {@code hybrid}（Day11/13） */
    private String retriever;

    /** 门控强度字符串：{@code EMPTY} | {@code WEAK} | {@code STRONG}（Day13 Gate） */
    private String gate;

    /** 门控原因文案（如 top 分低于阈值） */
    private String gateReason;

    /** 门控快照的最高相关分；空命中为 0 */
    private double topScore;

    /** 模型解析后的结构化回答（空命中可为本地模板回复） */
    private AssistantReply reply;

    /** 引用片段列表；空命中必须为空，禁止伪造 */
    private List<Source> sources = new ArrayList<>();

    /** 端到端耗时（毫秒） */
    private long latencyMs;

    /** LLM 尝试次数（含解析失败重试）；跳过 LLM 时为 0 */
    private int attempts;

    /** Token 与费用估算 */
    private Usage usage;

    /** 系统提示版本指纹（Day22） */
    private String promptVersion;

    /** 本次检索 TopK 配置值 */
    private int topK;

    /** 检索阶段耗时毫秒（Day24） */
    private long searchMs;

    /** LLM 调用耗时毫秒（Day24）；跳过 LLM 时为 0 */
    private long llmMs;

    /** 降级来源检索器名（Day24），例如 {@code vector}→keyword；未降级为 {@code null} */
    private String degradedFrom;

    /**
     * @return 追踪 id
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * @param traceId 追踪 id
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * @return LLM 提供方名
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider 提供方名
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
     * @return 检索器名
     */
    public String getRetriever() {
        return retriever;
    }

    /**
     * @param retriever 检索器名
     */
    public void setRetriever(String retriever) {
        this.retriever = retriever;
    }

    /**
     * @return 门控强度名
     */
    public String getGate() {
        return gate;
    }

    /**
     * @param gate 门控强度名
     */
    public void setGate(String gate) {
        this.gate = gate;
    }

    /**
     * @return 门控原因
     */
    public String getGateReason() {
        return gateReason;
    }

    /**
     * @param gateReason 门控原因
     */
    public void setGateReason(String gateReason) {
        this.gateReason = gateReason;
    }

    /**
     * @return 最高相关分
     */
    public double getTopScore() {
        return topScore;
    }

    /**
     * @param topScore 最高相关分
     */
    public void setTopScore(double topScore) {
        this.topScore = topScore;
    }

    /**
     * @return 结构化回答
     */
    public AssistantReply getReply() {
        return reply;
    }

    /**
     * @param reply 结构化回答
     */
    public void setReply(AssistantReply reply) {
        this.reply = reply;
    }

    /**
     * @return 引用列表（可变，但服务层通常写入完整列表）
     */
    public List<Source> getSources() {
        return sources;
    }

    /**
     * 设置引用列表；{@code null} 时规整为空列表，避免 NPE。
     *
     * @param sources 引用列表
     */
    public void setSources(List<Source> sources) {
        this.sources = sources == null ? new ArrayList<>() : sources;
    }

    /**
     * @return 端到端耗时毫秒
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * @param latencyMs 端到端耗时毫秒
     */
    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    /**
     * @return LLM 尝试次数
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * @param attempts LLM 尝试次数
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * @return Token/费用用量
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * @param usage Token/费用用量
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * @return 提示词版本指纹
     */
    public String getPromptVersion() {
        return promptVersion;
    }

    /**
     * @param promptVersion 提示词版本指纹
     */
    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    /**
     * @return TopK
     */
    public int getTopK() {
        return topK;
    }

    /**
     * @param topK TopK
     */
    public void setTopK(int topK) {
        this.topK = topK;
    }

    /**
     * @return 检索耗时毫秒
     */
    public long getSearchMs() {
        return searchMs;
    }

    /**
     * @param searchMs 检索耗时毫秒
     */
    public void setSearchMs(long searchMs) {
        this.searchMs = searchMs;
    }

    /**
     * @return LLM 耗时毫秒
     */
    public long getLlmMs() {
        return llmMs;
    }

    /**
     * @param llmMs LLM 耗时毫秒
     */
    public void setLlmMs(long llmMs) {
        this.llmMs = llmMs;
    }

    /**
     * @return 降级前检索器名；未降级为 {@code null}
     */
    public String getDegradedFrom() {
        return degradedFrom;
    }

    /**
     * @param degradedFrom 降级前检索器名
     */
    public void setDegradedFrom(String degradedFrom) {
        this.degradedFrom = degradedFrom;
    }

    /**
     * 单条引用资料：由命中 chunk 投影而来，供前端展示与人工核对。
     * <p>
     * 空命中路径下响应的 sources 必须保持空列表，不得填充占位引用。
     */
    public static class Source {
        /** 来源文档 id（文件名） */
        private String docId;

        /** 章节标签 */
        private String section;

        /** 相关分数（与当前检索器量纲一致） */
        private double score;

        /** 正文摘录（过长时截断并加省略号） */
        private String excerpt;

        /**
         * @return 文档 id
         */
        public String getDocId() {
            return docId;
        }

        /**
         * @param docId 文档 id
         */
        public void setDocId(String docId) {
            this.docId = docId;
        }

        /**
         * @return 章节
         */
        public String getSection() {
            return section;
        }

        /**
         * @param section 章节
         */
        public void setSection(String section) {
            this.section = section;
        }

        /**
         * @return 相关分
         */
        public double getScore() {
            return score;
        }

        /**
         * @param score 相关分
         */
        public void setScore(double score) {
            this.score = score;
        }

        /**
         * @return 摘录文本
         */
        public String getExcerpt() {
            return excerpt;
        }

        /**
         * @param excerpt 摘录文本
         */
        public void setExcerpt(String excerpt) {
            this.excerpt = excerpt;
        }
    }

    /**
     * Token 用量与估算费用（学习版按配置单价粗算）。
     */
    public static class Usage {
        /** 提示（输入）token 数 */
        private int promptTokens;

        /** 补全（输出）token 数 */
        private int completionTokens;

        /** 总 token = 输入 + 输出 */
        private int totalTokens;

        /** 估算费用（USD），按配置的每千 token 单价计算 */
        private double estimatedCostUsd;

        /**
         * @return 输入 token
         */
        public int getPromptTokens() {
            return promptTokens;
        }

        /**
         * @param promptTokens 输入 token
         */
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }

        /**
         * @return 输出 token
         */
        public int getCompletionTokens() {
            return completionTokens;
        }

        /**
         * @param completionTokens 输出 token
         */
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }

        /**
         * @return 总 token
         */
        public int getTotalTokens() {
            return totalTokens;
        }

        /**
         * @param totalTokens 总 token
         */
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }

        /**
         * @return 估算 USD 费用
         */
        public double getEstimatedCostUsd() {
            return estimatedCostUsd;
        }

        /**
         * @param estimatedCostUsd 估算 USD 费用
         */
        public void setEstimatedCostUsd(double estimatedCostUsd) {
            this.estimatedCostUsd = estimatedCostUsd;
        }
    }
}
