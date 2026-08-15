package com.erp.ai.client;

import com.erp.ai.config.AiProperties;
import com.erp.ai.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

import java.util.List;

/**
 * OpenAI Compatible Chat Completions 客户端。
 * <p>
 * 适用于 OpenAI、DeepSeek、通义兼容模式等：请求路径通常是
 * {@code POST {baseUrl}/chat/completions}，鉴权为 {@code Authorization: Bearer <apiKey>}。
 * <p>
 * 由 {@link com.erp.ai.config.LlmClientConfig} 创建，不要加 {@code @Component}。
 * <p>
 * 常见错误：
 * <ul>
 *   <li>401：API Key 错误/过期/未生效，或 Key 带了多余空格引号</li>
 *   <li>404：base-url 写错（DeepSeek 常用 https://api.deepseek.com）</li>
 *   <li>400：模型名不对，或强制 json_object 但提示词未要求 JSON</li>
 * </ul>
 */
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final AiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleLlmClient(AiProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "openai-compatible";
    }

    /**
     * 组装 OpenAI 风格请求体并调用远程模型。
     * 这里返回的是“原始 content 字符串”，结构化解析交给 {@code ReplyParser}。
     */
    @Override
    public LlmResult chat(List<ChatMessage> messages) {
        String apiKey = properties.getApiKey() == null ? "" : properties.getApiKey().trim();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("ai.api-key 未配置。请设置环境变量 AI_API_KEY，或改回 ai.provider=mock");
        }

        // ---- 请求体：对应 Chat Completions API ----
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature());
        // 要求服务端尽量返回 JSON 对象（部分兼容网关可能忽略该字段）
        body.put("response_format", objectMapper.createObjectNode().put("type", "json_object"));

        ArrayNode messageNodes = body.putArray("messages");
        for (ChatMessage message : messages) {
            ObjectNode node = messageNodes.addObject();
            node.put("role", message.getRole());
            node.put("content", message.getContent());
        }

        // ---- 请求头：Bearer Token 鉴权 ----
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String url = trimTrailingSlash(properties.getBaseUrl()) + "/chat/completions";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body.toString(), headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());

            // 标准路径：choices[0].message.content
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("模型返回内容为空");
            }

            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
            String model = root.path("model").asText(properties.getModel());
            return new LlmResult(content, model, promptTokens, completionTokens);
        } catch (HttpStatusCodeException ex) {
            // 把 HTTP 状态码翻译成更可读的学习提示
            throw new IllegalStateException(describeHttpError(ex, url), ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("调用 LLM 失败: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("解析 LLM 响应失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 针对鉴权/地址类错误给出可操作提示（不打印完整 Key）。
     */
    private String describeHttpError(HttpStatusCodeException ex, String url) {
        HttpStatusCode status = ex.getStatusCode();
        String body = ex.getResponseBodyAsString();
        String snippet = body == null || body.isBlank() ? "[no body]" : body;
        if (snippet.length() > 300) {
            snippet = snippet.substring(0, 300) + "...";
        }

        if (status.value() == 401) {
            return "调用 LLM 失败: 401 鉴权失败。请检查 AI_API_KEY 是否正确、是否过期、是否多了空格/引号；"
                    + " DeepSeek Key 来自平台控制台。url=" + url + " body=" + snippet;
        }
        if (status.value() == 404) {
            return "调用 LLM 失败: 404 地址不存在。请检查 AI_BASE_URL（DeepSeek 常用 https://api.deepseek.com）。"
                    + " url=" + url;
        }
        return "调用 LLM 失败: HTTP " + status.value() + " url=" + url + " body=" + snippet;
    }

    /** 去掉 baseUrl 末尾多余斜杠，避免出现 //chat/completions */
    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
