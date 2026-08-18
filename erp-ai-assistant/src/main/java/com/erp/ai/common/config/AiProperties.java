package com.erp.ai.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 相关类型安全配置，对应 {@code application.yml} 中的 {@code ai} 节点。
 *
 * <h2>职责</h2>
 * 集中承载 LLM、会话、RAG、只读工具等可调参数，供 Client 工厂与业务服务注入使用。
 *
 * <h2>为何用 {@code @ConfigurationProperties}</h2>
 * <ul>
 *   <li>比散落 {@code @Value} 更易维护、可嵌套、可校验</li>
 *   <li>YAML kebab-case（如 {@code base-url}）自动绑定到 Java camelCase（{@code baseUrl}）</li>
 *   <li>环境变量宽松绑定：{@code AI_BASE_URL}、{@code AI_API_KEY} 等可覆盖</li>
 * </ul>
 *
 * <h2>与 Spring 的关系</h2>
 * 由启动类 {@code @EnableConfigurationProperties(AiProperties.class)} 启用；
 * 也可被 {@code @EnableConfigurationProperties} / {@code @ConfigurationPropertiesScan} 扫描。
 * 本类<strong>不是</strong> {@code @Component}，依赖上述显式启用。
 *
 * <h2>重要概念</h2>
 * <ul>
 *   <li>{@code provider} = 接入协议（mock / openai-compatible），不是厂商展示名</li>
 *   <li>DeepSeek、通义等差异主要体现在 {@code baseUrl} + {@code model}</li>
 *   <li>Chat 与 Embedding 可分开配置（见 {@link Rag}）</li>
 * </ul>
 *
 * <h2>学习要点</h2>
 * 改行为优先改配置；密钥只走环境变量，切勿提交到 Git。
 */
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 接入实现标识：{@code mock} | {@code openai-compatible} | {@code deepseek}（别名）等。
     * <p>
     * 工厂见 {@code LlmClientConfig}：别名最终都映射到同一兼容客户端。
     */
    private String provider = "mock";

    /**
     * 模型服务根地址，最终 Chat 请求一般为 {@code {baseUrl}/chat/completions}。
     * <p>
     * DeepSeek 常用 {@code https://api.deepseek.com}（以官方文档为准）。
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * API Key；真实调用时通过 {@code Authorization: Bearer} 发送。
     * <p>
     * <b>安全：</b>默认空串；务必用环境变量注入，切勿写入仓库。
     */
    private String apiKey = "";

    /**
     * Chat 模型名称，如 {@code deepseek-chat}、{@code gpt-4o-mini}。
     * <p>
     * 与 Embedding 模型名通常不同，勿混用。
     */
    private String model = "gpt-4o-mini";

    /**
     * 采样温度。越低越稳定、越适合 ERP 结构化输出；越高越发散。
     * <p>
     * 本项目默认 {@code 0.2}：优先可解析 JSON 与可重复演示。
     */
    private double temperature = 0.2;

    /**
     * HTTP 连接/读取超时（毫秒），由 {@code AppConfig#restTemplate} 读取并应用到 RestTemplate。
     * <p>
     * 过大浪费线程；过小导致长回答被误判失败。
     */
    private long timeoutMs = 30000;

    /**
     * 业务层额外重试次数（不含首次）。
     * <p>
     * 例如 {@code 2} 表示最多尝试 {@code 1+2=3} 次（常用于 JSON 解析失败后纠错重试）。
     * <b>注意：</b>这是业务重试，不是 RestTemplate 自动重试。
     */
    private int maxRetries = 2;

    /**
     * 输入 Token 单价估算（美元 / 1K tokens），仅用于学习期成本观察与审计字段。
     * <p>
     * 非计费账单；真实价格以云厂商为准。
     */
    private double priceInputPer1k = 0.00015;

    /**
     * 输出 Token 单价估算（美元 / 1K tokens）。
     */
    private double priceOutputPer1k = 0.0006;

    /**
     * 多轮会话子配置（对应 {@code ai.session.*}）。
     */
    private Session session = new Session();

    /**
     * RAG 学习版子配置（对应 {@code ai.rag.*}）。
     */
    private Rag rag = new Rag();

    /**
     * Day16/18 只读工具子配置（对应 {@code ai.tool.*}）。
     */
    private Tool tool = new Tool();

    /**
     * @return 当前 provider 字符串（可能含别名原始大小写，取决于 YAML）
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider 接入实现；{@code null} 时工厂侧会当 mock 处理（见 LlmClientConfig）
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return Chat API 根地址
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl 根地址；建议不要带末尾多余路径
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return API Key；可能为空串
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey Bearer Token 明文；勿日志打印完整值
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return Chat 模型名
     */
    public String getModel() {
        return model;
    }

    /**
     * @param model 模型名，须为网关已开通的型号
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return 采样温度
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * @param temperature 通常 0～2；ERP 场景建议偏低
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * @return HTTP 超时毫秒
     */
    public long getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * @param timeoutMs 连接与读取共用该值（见 AppConfig）
     */
    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * @return 额外重试次数（不含首次）
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @param maxRetries {@code >=0}；过大可能放大费用与延迟
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * @return 输入单价估算
     */
    public double getPriceInputPer1k() {
        return priceInputPer1k;
    }

    /**
     * @param priceInputPer1k 美元 / 1K prompt tokens
     */
    public void setPriceInputPer1k(double priceInputPer1k) {
        this.priceInputPer1k = priceInputPer1k;
    }

    /**
     * @return 输出单价估算
     */
    public double getPriceOutputPer1k() {
        return priceOutputPer1k;
    }

    /**
     * @param priceOutputPer1k 美元 / 1K completion tokens
     */
    public void setPriceOutputPer1k(double priceOutputPer1k) {
        this.priceOutputPer1k = priceOutputPer1k;
    }

    /**
     * @return 会话子配置；默认非 null
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session 会话子配置；绑定框架通常不会传 null
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return RAG 子配置；默认非 null
     */
    public Rag getRag() {
        return rag;
    }

    /**
     * @param rag RAG 子配置
     */
    public void setRag(Rag rag) {
        this.rag = rag;
    }

    /**
     * @return 工具子配置；默认非 null
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * @param tool 工具子配置
     */
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /**
     * 多轮会话配置。
     * <p>
     * 对应 YAML：{@code ai.session.max-messages}。
     * 控制单 session 历史长度，平衡上下文完整性与 Token 成本。
     */
    public static class Session {

        /**
         * 单个 session 最多保留的历史消息条数（含 user/assistant）。
         * <p>
         * 过大浪费 Token；过小会丢失上下文导致答非所问。
         */
        private int maxMessages = 20;

        /**
         * @return 历史上限
         */
        public int getMaxMessages() {
            return maxMessages;
        }

        /**
         * @param maxMessages 正整数；业务层应自行 clamp
         */
        public void setMaxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
        }
    }

    /**
     * RAG 学习版配置。
     * <p>
     * 对应 YAML：{@code ai.rag.*}。
     * 覆盖检索器类型、Embedding 实现、Hybrid/RRF 参数与弱命中阈值。
     *
     * <h3>配置联动（学习要点）</h3>
     * <ol>
     *   <li>{@code retriever=vector|hybrid} 时依赖 {@link EmbeddingClient} Bean</li>
     *   <li>{@code embedding-provider=hash} 时使用 {@code embedding-dimensions}</li>
     *   <li>{@code embedding-provider=openai-compatible} 时使用 model/base-url/api-key</li>
     *   <li>{@code min-score} 对 keyword/向量/hybrid 的分数量纲不同，需按模式标定</li>
     * </ol>
     */
    public static class Rag {

        /**
         * 每次提问取回的资料块数量（最终 topK）。
         */
        private int topK = 3;

        /**
         * classpath 下教材目录名，默认 {@code rag-docs}。
         */
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

        /**
         * Embedding 模型名（仅 openai-compatible 时使用；与 chat model 通常不同）。
         */
        private String embeddingModel = "text-embedding-3-small";

        /**
         * 可选：单独的 embeddings base-url；空则复用 {@code ai.base-url}。
         */
        private String embeddingBaseUrl = "";

        /**
         * 可选：单独的 embeddings API Key；空则复用 {@code ai.api-key}。
         */
        private String embeddingApiKey = "";

        /**
         * hash embedding 维度（学习版）；工厂侧会 {@code Math.max(8, dims)}。
         */
        private int embeddingDimensions = 256;

        /**
         * Hybrid 每路先召回条数（再 RRF 融合取 topK）。
         * <p>
         * 应 {@code >= topK}，否则融合池过窄。
         */
        private int recallK = 10;

        /**
         * RRF 常数 k，常用 60；越大则排名差异被平滑得越厉害。
         */
        private int rrfK = 60;

        /**
         * 弱命中阈值：最高分低于此值视为 WEAK。
         * <p>
         * keyword/向量余弦约为 0～1；hybrid 的 RRF 分通常更小（如 0.01～0.03），需按模式标定。
         */
        private double minScore = 0.02;

        /**
         * 空命中时是否跳过 LLM，直接返回拒答（sources=[]）。
         * <p>
         * {@code true} 可省费用并避免模型胡编教材。
         */
        private boolean skipLlmOnEmpty = true;

        /** @return 最终返回的资料块数 */
        public int getTopK() {
            return topK;
        }

        /** @param topK 建议 {@code >= 1} */
        public void setTopK(int topK) {
            this.topK = topK;
        }

        /** @return classpath 教材目录 */
        public String getClasspathDocs() {
            return classpathDocs;
        }

        /** @param classpathDocs 相对 classpath 的目录名 */
        public void setClasspathDocs(String classpathDocs) {
            this.classpathDocs = classpathDocs;
        }

        /** @return 检索器类型字符串 */
        public String getRetriever() {
            return retriever;
        }

        /** @param retriever keyword / vector / hybrid */
        public void setRetriever(String retriever) {
            this.retriever = retriever;
        }

        /** @return embedding 实现名 */
        public String getEmbeddingProvider() {
            return embeddingProvider;
        }

        /** @param embeddingProvider hash 或 openai-compatible */
        public void setEmbeddingProvider(String embeddingProvider) {
            this.embeddingProvider = embeddingProvider;
        }

        /** @return 远程 embedding 模型名 */
        public String getEmbeddingModel() {
            return embeddingModel;
        }

        /** @param embeddingModel 网关支持的 embedding 型号 */
        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        /** @return 覆盖用 base-url；可空 */
        public String getEmbeddingBaseUrl() {
            return embeddingBaseUrl;
        }

        /** @param embeddingBaseUrl 空串表示回退 chat base-url */
        public void setEmbeddingBaseUrl(String embeddingBaseUrl) {
            this.embeddingBaseUrl = embeddingBaseUrl;
        }

        /** @return 覆盖用 API Key；可空 */
        public String getEmbeddingApiKey() {
            return embeddingApiKey;
        }

        /** @param embeddingApiKey 空串表示回退 chat api-key */
        public void setEmbeddingApiKey(String embeddingApiKey) {
            this.embeddingApiKey = embeddingApiKey;
        }

        /** @return hash 向量维度 */
        public int getEmbeddingDimensions() {
            return embeddingDimensions;
        }

        /** @param embeddingDimensions 建议 64～1024 的学习取值 */
        public void setEmbeddingDimensions(int embeddingDimensions) {
            this.embeddingDimensions = embeddingDimensions;
        }

        /** @return Hybrid 每路召回数 */
        public int getRecallK() {
            return recallK;
        }

        /** @param recallK 融合前候选池大小 */
        public void setRecallK(int recallK) {
            this.recallK = recallK;
        }

        /** @return RRF 常数 k */
        public int getRrfK() {
            return rrfK;
        }

        /** @param rrfK 常用 60 */
        public void setRrfK(int rrfK) {
            this.rrfK = rrfK;
        }

        /** @return 弱命中分数阈值 */
        public double getMinScore() {
            return minScore;
        }

        /** @param minScore 按检索器量纲标定 */
        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }

        /** @return 空命中是否跳过 LLM */
        public boolean isSkipLlmOnEmpty() {
            return skipLlmOnEmpty;
        }

        /** @param skipLlmOnEmpty true=省调用、防胡编 */
        public void setSkipLlmOnEmpty(boolean skipLlmOnEmpty) {
            this.skipLlmOnEmpty = skipLlmOnEmpty;
        }
    }

    /**
     * Day16/18 只读工具配置。
     * <p>
     * 对应 YAML：{@code ai.tool.*}。
     * 控制 Chat 与工具的衔接路径（路径 A tool-calls / 路径 B rule）及轮次上限。
     */
    public static class Tool {

        /**
         * 单次工具执行超时（毫秒）；假数据也统一走超时习惯，便于换成真实 RPC。
         */
        private long timeoutMs = 3000;

        /**
         * Chat 衔接路径：
         * {@code tool-calls}（Day18 路径 A，默认）|
         * {@code rule}（路径 B）|
         * {@code off}。
         * <p>
         * 原始值可能含别名；请用 {@link #normalizedChatPath()} 读取规范化结果。
         */
        private String chatPath = "tool-calls";

        /**
         * 路径 A：单次用户请求内最多 tool_calls 轮次（防模型死循环反复调工具）。
         */
        private int maxRounds = 4;

        /** @return 工具执行超时毫秒 */
        public long getTimeoutMs() {
            return timeoutMs;
        }

        /** @param timeoutMs 建议数百到数千毫秒 */
        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        /** @return 原始 chatPath 配置串 */
        public String getChatPath() {
            return chatPath;
        }

        /** @param chatPath tool-calls / rule / off 或其别名 */
        public void setChatPath(String chatPath) {
            this.chatPath = chatPath;
        }

        /** @return 路径 A 最大轮次 */
        public int getMaxRounds() {
            return maxRounds;
        }

        /** @param maxRounds {@code >= 1}；过小可能导致工具未跑完就结束 */
        public void setMaxRounds(int maxRounds) {
            this.maxRounds = maxRounds;
        }

        /**
         * 规范化 chatPath：去空白、小写、下划线转横杠，并映射常见别名。
         * <p>
         * <b>映射规则：</b>
         * <ul>
         *   <li>{@code a}/{@code path-a}/{@code toolcalls}/{@code tools} → {@code tool-calls}</li>
         *   <li>{@code b}/{@code path-b} → {@code rule}</li>
         *   <li>空白 → 默认 {@code tool-calls}</li>
         *   <li>其它原样返回规范化后的字符串（如 {@code off}）</li>
         * </ul>
         *
         * @return 永不为 {@code null} 的路径标识
         */
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

        /**
         * @return 是否启用路径 B（规则注入工具结果）
         */
        public boolean isRulePathEnabled() {
            return "rule".equals(normalizedChatPath());
        }

        /**
         * @return 是否启用路径 A（原生 tool_calls）
         */
        public boolean isToolCallsPathEnabled() {
            return "tool-calls".equals(normalizedChatPath());
        }
    }
}
