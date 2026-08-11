package com.erp.ai.client;

import com.erp.ai.config.AiProperties;
import com.erp.ai.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 由 {@link com.erp.ai.config.LlmClientConfig} 按 ai.provider 创建，不要再加 @Component。
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

    @Override
    public LlmResult chat(List<ChatMessage> messages) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("ai.api-key 未配置。请设置环境变量 AI_API_KEY，或改回 ai.provider=mock");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature());
        body.put("response_format", objectMapper.createObjectNode().put("type", "json_object"));

        ArrayNode messageNodes = body.putArray("messages");
        for (ChatMessage message : messages) {
            ObjectNode node = messageNodes.addObject();
            node.put("role", message.getRole());
            node.put("content", message.getContent());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        String url = trimTrailingSlash(properties.getBaseUrl()) + "/chat/completions";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body.toString(), headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("模型返回内容为空");
            }
            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
            String model = root.path("model").asText(properties.getModel());
            return new LlmResult(content, model, promptTokens, completionTokens);
        } catch (RestClientException ex) {
            throw new IllegalStateException("调用 LLM 失败: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("解析 LLM 响应失败: " + ex.getMessage(), ex);
        }
    }

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
