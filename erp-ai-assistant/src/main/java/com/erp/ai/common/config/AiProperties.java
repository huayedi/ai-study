package com.erp.ai.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 相关配置，对应 {@code application.yml} 中的 {@code ai} 节点。
 * <p>
 * 命名规则：YAML 用 kebab-case（如 {@code base-url}），Java 用 camelCase（如 {@code baseUrl}），
 * Spring Boot 会自动做宽松绑定；也可用环境变量 {@code AI_BASE_URL} 覆盖。
 * <p>
 * 重要概念：
 * <ul>
 *   <li>{@code provider} = 接入协议（mock / openai-compatible），不是厂商名</li>
 *   <li>DeepSeek、通义等差异主要体现在 {@code baseUrl} + {@code model}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 接入实现：mock | openai-compatible | deepseek（别名）等 */
    private String provider = "mock";

    /** 模型服务根地址，最终请求一般为 {baseUrl}/chat/completions */
    private String baseUrl = "https://api.openai.com/v1";

    /** API Key；真实调用时通过 Authorization: Bearer 发送，切勿提交到 Git */
    private String apiKey = "";

    /** 模型名称，如 deepseek-chat、gpt-4o-mini */
    private String model = "gpt-4o-mini";

    /**
     * 采样温度。越低越稳定、越适合 ERP 结构化输出；
     * 越高越发散。本项目默认 0.2。
     */
    private double temperature = 0.2;

    /** HTTP 连接/读取超时（毫秒） */
    private long timeoutMs = 30000;

    /**
     * 业务层额外重试次数（不含首次）。
     * 例如 2 表示最多尝试 1+2=3 次（常用于 JSON 解析失败后纠错）。
     */
    private int maxRetries = 2;

    /** 输入 Token 单价估算（美元 / 1K tokens），仅用于学习期成本观察 */
    private double priceInputPer1k = 0.00015;

    /** 输出 Token 单价估算（美元 / 1K tokens） */
    private double priceOutputPer1k = 0.0006;

    /** 会话相关子配置 */
    private Session session = new Session();

    /** RAG 学习版子配置 */
    private Rag rag = new Rag();

    /** Day16 只读工具子配置 */
    private Tool tool = new Tool();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public double getPriceInputPer1k() {
        return priceInputPer1k;
    }

    public void setPriceInputPer1k(double priceInputPer1k) {
        this.priceInputPer1k = priceInputPer1k;
    }

    public double getPriceOutputPer1k() {
        return priceOutputPer1k;
    }

    public void setPriceOutputPer1k(double priceOutputPer1k) {
        this.priceOutputPer1k = priceOutputPer1k;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /**
     * 多轮会话配置。
     * 对应 YAML：{@code ai.session.max-messages}
     */
    public static class Session {

        /**
         * 单个 session 最多保留的历史消息条数（含 user/assistant）。
         * 过大浪费 Token，过小会丢失上下文。
         */
        private int maxMessages = 20;

        public int getMaxMessages() {
            return maxMessages;
        }

        public void setMaxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
        }
    }

    /**
     * RAG 学习版配置。
     * 对应 YAML：{@code ai.rag.*}
     */
    public static class Rag {

        /** 每次提问取回的资料块数量 */
        private int topK = 3;

        /** classpath 下教材目录，默认 rag-docs */
        private String classpathDocs = "rag-docs";

        /**
         * 检索器：{@code keyword} | {@code vector} | {@code hybrid}（Day13）。
         */
        private String retriever = "keyword";

        /**
         * Embedding 实现：{@code hash}（学习替身，默认）|
         * {@code openai-compatible}（POST /embeddings）。
         * <p>
         * DeepSeek 无 embeddings 时请保持 hash，或把 embedding-base-url 指到其它兼容网关。
         */
        private String embeddingProvider = "hash";

        /** Embedding 模型名（仅 openai-compatible 时使用；与 chat model 通常不同） */
        private String embeddingModel = "text-embedding-3-small";

        /** 可选：单独的 embeddings base-url；空则复用 ai.base-url */
        private String embeddingBaseUrl = "";

        /** 可选：单独的 embeddings API Key；空则复用 ai.api-key */
        private String embeddingApiKey = "";

        /** hash embedding 维度（学习版） */
        private int embeddingDimensions = 256;

        /** Hybrid 每路先召回条数（再 RRF 融合取 topK） */
        private int recallK = 10;

        /** RRF 常数 k，常用 60 */
        private int rrfK = 60;

        /**
         * 弱命中阈值：最高分低于此值视为 WEAK。
         * keyword/向量余弦约为 0～1；hybrid 的 RRF 分通常更小（如 0.01～0.03），需按模式标定。
         */
        private double minScore = 0.02;

        /** 空命中时是否跳过 LLM，直接返回拒答（sources=[]） */
        private boolean skipLlmOnEmpty = true;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public String getClasspathDocs() {
            return classpathDocs;
        }

        public void setClasspathDocs(String classpathDocs) {
            this.classpathDocs = classpathDocs;
        }

        public String getRetriever() {
            return retriever;
        }

        public void setRetriever(String retriever) {
            this.retriever = retriever;
        }

        public String getEmbeddingProvider() {
            return embeddingProvider;
        }

        public void setEmbeddingProvider(String embeddingProvider) {
            this.embeddingProvider = embeddingProvider;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public String getEmbeddingBaseUrl() {
            return embeddingBaseUrl;
        }

        public void setEmbeddingBaseUrl(String embeddingBaseUrl) {
            this.embeddingBaseUrl = embeddingBaseUrl;
        }

        public String getEmbeddingApiKey() {
            return embeddingApiKey;
        }

        public void setEmbeddingApiKey(String embeddingApiKey) {
            this.embeddingApiKey = embeddingApiKey;
        }

        public int getEmbeddingDimensions() {
            return embeddingDimensions;
        }

        public void setEmbeddingDimensions(int embeddingDimensions) {
            this.embeddingDimensions = embeddingDimensions;
        }

        public int getRecallK() {
            return recallK;
        }

        public void setRecallK(int recallK) {
            this.recallK = recallK;
        }

        public int getRrfK() {
            return rrfK;
        }

        public void setRrfK(int rrfK) {
            this.rrfK = rrfK;
        }

        public double getMinScore() {
            return minScore;
        }

        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }

        public boolean isSkipLlmOnEmpty() {
            return skipLlmOnEmpty;
        }

        public void setSkipLlmOnEmpty(boolean skipLlmOnEmpty) {
            this.skipLlmOnEmpty = skipLlmOnEmpty;
        }
    }

    /**
     * Day16/18 只读工具配置。
     * 对应 YAML：{@code ai.tool.*}
     */
    public static class Tool {

        /** 单次工具执行超时（毫秒）；假数据也统一走超时习惯 */
        private long timeoutMs = 3000;

        /**
         * Chat 衔接路径：
         * {@code tool-calls}（Day18 路径 A，默认）|
         * {@code rule}（路径 B）|
         * {@code off}。
         */
        private String chatPath = "tool-calls";

        /** 路径 A：单次用户请求内最多 tool_calls 轮次（防死循环） */
        private int maxRounds = 4;

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public String getChatPath() {
            return chatPath;
        }

        public void setChatPath(String chatPath) {
            this.chatPath = chatPath;
        }

        public int getMaxRounds() {
            return maxRounds;
        }

        public void setMaxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
        }

        public String normalizedChatPath() {
            if (chatPath == null || chatPath.isBlank()) {
                return "tool-calls";
            }
            String p = chatPath.trim().toLowerCase().replace('_', '-');
            if ("a".equals(p) || "path-a".equals(p) || "toolcalls".equals(p) || "tools".equals(p)) {
                return "tool-calls";
            }
            if ("b".equals(p) || "path-b".equals(p)) {
                return "rule";
            }
            return p;
        }

        public boolean isRulePathEnabled() {
            return "rule".equals(normalizedChatPath());
        }

        public boolean isToolCallsPathEnabled() {
            return "tool-calls".equals(normalizedChatPath());
        }
    }
}
