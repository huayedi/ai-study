package com.erp.ai.common.client;

import com.erp.ai.common.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * OpenAI 兼容 Embeddings 客户端：{@code POST {baseUrl}/embeddings}。
 *
 * <h2>职责</h2>
 * 调用远程 embeddings 接口，把文本转为 {@code float[]}，供向量检索使用。
 *
 * <h2>请求 / 响应概念</h2>
 * <ul>
 *   <li>请求体：{@code {"model":"...","input":"文本"}}</li>
 *   <li>响应：{@code data[0].embedding} 为浮点数组</li>
 * </ul>
 *
 * <h2>重要限制</h2>
 * DeepSeek 官方目前不提供 {@code /embeddings}。Chat 仍可用 DeepSeek；
 * 若 {@code embedding-provider=openai-compatible}，请把
 * {@code ai.rag.embedding-base-url} 指到真正提供 embeddings 的兼容网关
 * （或改用 {@code hash} 学习替身）。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 由 {@code EmbeddingClientConfig} 在 {@code ai.rag.embedding-provider=openai-compatible} 时创建。
 * 支持独立的 embedding-base-url / embedding-api-key，空则回退到 chat 的 baseUrl / apiKey。
 *
 * <h2>学习要点</h2>
 * Embedding 模型名通常与 chat model 不同；维度由远端模型决定，索引侧必须一致。
 */
public class OpenAiCompatibleEmbeddingClient implements EmbeddingClient {

    /** 含 rag 子配置的 AI 属性 */
    private final AiProperties properties;

    /** 共享 HTTP 客户端 */
    private final RestTemplate restTemplate;

    /** JSON 编解码 */
    private final ObjectMapper objectMapper;

    /**
     * @param properties   AI 配置
     * @param restTemplate HTTP 客户端
     * @param objectMapper JSON 工具
     */
    public OpenAiCompatibleEmbeddingClient(AiProperties properties,
                                           RestTemplate restTemplate,
                                           ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用远程 embeddings 接口编码单段文本。
     * <p>
     * <b>算法步骤：</b>
     * <ol>
     *   <li>解析 API Key（优先 embedding 专用，否则回退 chat key）</li>
     *   <li>校验 embedding-model 非空</li>
     *   <li>组装 {@code model}+{@code input}，Bearer POST {@code /embeddings}</li>
     *   <li>读取 {@code data[0].embedding} 转为 float[]</li>
     * </ol>
     * <b>失败语义：</b>缺 Key/缺模型/HTTP 失败/响应无 embedding → {@link IllegalStateException}。
     * {@code text==null} 时按空串发送，不抛 NPE。
     *
     * @param text 待编码文本；{@code null} 视为 {@code ""}
     * @return 远端返回的向量；长度由模型决定
     * @throws IllegalStateException 配置或调用失败时
     */
    @Override
    public float[] embed(String text) {
        // ---- 步骤 1：Key ----
        String apiKey = resolveApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException(
                    "Embedding 需要 API Key。请设置 AI_API_KEY，或改 ai.rag.embedding-provider=hash");
        }

        // ---- 步骤 2：模型名 ----
        String model = properties.getRag().getEmbeddingModel();
        if (!StringUtils.hasText(model)) {
            throw new IllegalStateException("ai.rag.embedding-model 未配置");
        }

        // ---- 步骤 3：请求体 ----
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("input", text == null ? "" : text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // ---- 步骤 4：URL（可覆盖 base） ----
        String url = trimTrailingSlash(resolveBaseUrl()) + "/embeddings";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body.toString(), headers),
                    String.class
            );
            // ---- 步骤 5：解析 embedding 数组 ----
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode embeddingNode = root.path("data").path(0).path("embedding");
            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new IllegalStateException("Embedding 响应缺少 data[0].embedding");
            }
            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }
            return vector;
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(describeHttpError(ex, url), ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("调用 Embedding 失败: " + ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("解析 Embedding 响应失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 解析 embeddings 专用 baseUrl；空白则回退 {@code ai.base-url}。
     *
     * @return 非 blank 的根地址（调用方仍会 trim 斜杠）
     */
    private String resolveBaseUrl() {
        String override = properties.getRag().getEmbeddingBaseUrl();
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return properties.getBaseUrl();
    }

    /**
     * 解析 embeddings 专用 API Key；空白则回退 {@code ai.api-key}。
     *
     * @return trim 后的 key；可能为空串
     */
    private String resolveApiKey() {
        String override = properties.getRag().getEmbeddingApiKey();
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return properties.getApiKey() == null ? "" : properties.getApiKey().trim();
    }

    /**
     * HTTP 错误说明；404 特别提示 DeepSeek 通常无 embeddings。
     *
     * @param ex  状态码异常
     * @param url 请求 URL
     * @return 可读错误信息
     */
    private String describeHttpError(HttpStatusCodeException ex, String url) {
        HttpStatusCode status = ex.getStatusCode();
        String body = ex.getResponseBodyAsString();
        String snippet = body == null || body.isBlank() ? "[no body]" : body;
        if (snippet.length() > 300) {
            snippet = snippet.substring(0, 300) + "...";
        }
        if (status.value() == 404) {
            return "调用 Embedding 失败: 404。DeepSeek 通常无 /embeddings；"
                    + "请设 ai.rag.embedding-provider=hash，或把 embedding-base-url 指到有 embeddings 的网关。"
                    + " url=" + url + " body=" + snippet;
        }
        if (status.value() == 401) {
            return "调用 Embedding 失败: 401 鉴权失败。检查 Key。url=" + url + " body=" + snippet;
        }
        return "调用 Embedding 失败: HTTP " + status.value() + " url=" + url + " body=" + snippet;
    }

    /**
     * @param baseUrl 根地址
     * @return 去掉末尾 {@code /} 后的地址
     */
    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code openai-compatible}
     */
    @Override
    public String providerName() {
        return "openai-compatible";
    }
}
