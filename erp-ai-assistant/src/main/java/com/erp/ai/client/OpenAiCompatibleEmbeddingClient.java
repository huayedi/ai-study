package com.erp.ai.client;

import com.erp.ai.config.AiProperties;
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
 * <p>
 * 请求体概念：{@code {"model":"...","input":"文本"}}；响应 {@code data[0].embedding}。
 * <p>
 * <b>注意：</b>DeepSeek 官方目前不提供 /embeddings。Chat 可用 DeepSeek；
 * 若 {@code embedding-provider=openai-compatible}，请把
 * {@code ai.rag.embedding-base-url} 指到真正提供 embeddings 的兼容网关
 * （或改用 {@code hash} 学习替身）。
 */
public class OpenAiCompatibleEmbeddingClient implements EmbeddingClient {

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleEmbeddingClient(AiProperties properties,
                                           RestTemplate restTemplate,
                                           ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public float[] embed(String text) {
        String apiKey = resolveApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException(
                    "Embedding 需要 API Key。请设置 AI_API_KEY，或改 ai.rag.embedding-provider=hash");
        }

        String model = properties.getRag().getEmbeddingModel();
        if (!StringUtils.hasText(model)) {
            throw new IllegalStateException("ai.rag.embedding-model 未配置");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("input", text == null ? "" : text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String url = trimTrailingSlash(resolveBaseUrl()) + "/embeddings";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body.toString(), headers),
                    String.class
            );
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

    private String resolveBaseUrl() {
        String override = properties.getRag().getEmbeddingBaseUrl();
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return properties.getBaseUrl();
    }

    private String resolveApiKey() {
        String override = properties.getRag().getEmbeddingApiKey();
        if (StringUtils.hasText(override)) {
            return override.trim();
        }
        return properties.getApiKey() == null ? "" : properties.getApiKey().trim();
    }

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

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public String providerName() {
        return "openai-compatible";
    }
}
