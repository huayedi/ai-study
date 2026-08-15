package com.erp.ai.config;

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
    }
}
