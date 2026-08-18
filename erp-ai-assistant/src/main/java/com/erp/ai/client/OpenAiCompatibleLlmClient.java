package com.erp.ai.client;

import com.erp.ai.config.AiProperties;
import com.erp.ai.model.ChatMessage;
import com.erp.ai.tool.ToolDefinition;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Compatible Chat Completions 客户端（含 Day18 路径 A tool_calls）。
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
    public LlmResult chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        String apiKey = properties.getApiKey() == null ? "" : properties.getApiKey().trim();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("ai.api-key 未配置。请设置环境变量 AI_API_KEY，或改回 ai.provider=mock");
        }

        boolean withTools = tools != null && !tools.isEmpty();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature());
        // 带 tools 时许多兼容网关不支持同时强制 json_object；终轮无 tool_calls 时再靠提示词约束 JSON
        if (!withTools) {
            body.put("response_format", objectMapper.createObjectNode().put("type", "json_object"));
        }

        ArrayNode messageNodes = body.putArray("messages");
        for (ChatMessage message : messages) {
            messageNodes.add(toMessageNode(message));
        }

        if (withTools) {
            ArrayNode toolNodes = body.putArray("tools");
            for (ToolDefinition def : tools) {
                toolNodes.add(toToolNode(def));
            }
        }

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
            JsonNode message = root.path("choices").path(0).path("message");

            List<LlmToolCall> toolCalls = parseToolCalls(message.path("tool_calls"));
            String content = textOrNull(message.path("content"));

            if (!toolCalls.isEmpty()) {
                int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
                int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
                String model = root.path("model").asText(properties.getModel());
                return new LlmResult(content, model, promptTokens, completionTokens, toolCalls);
            }

            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("模型返回内容为空且无 tool_calls");
            }

            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
            String model = root.path("model").asText(properties.getModel());
            return new LlmResult(content, model, promptTokens, completionTokens);
        } catch (HttpStatusCodeException ex) {
            throw new IllegalStateException(describeHttpError(ex, url), ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("调用 LLM 失败: " + ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("解析 LLM 响应失败: " + ex.getMessage(), ex);
        }
    }

    private ObjectNode toMessageNode(ChatMessage message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", message.getRole());
        if ("tool".equals(message.getRole())) {
            if (StringUtils.hasText(message.getToolCallId())) {
                node.put("tool_call_id", message.getToolCallId());
            }
            if (StringUtils.hasText(message.getName())) {
                node.put("name", message.getName());
            }
            node.put("content", message.getContent() == null ? "" : message.getContent());
            return node;
        }
        if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
            if (message.getContent() != null) {
                node.put("content", message.getContent());
            } else {
                node.putNull("content");
            }
            ArrayNode calls = node.putArray("tool_calls");
            for (LlmToolCall call : message.getToolCalls()) {
                ObjectNode c = calls.addObject();
                c.put("id", call.id());
                c.put("type", "function");
                ObjectNode fn = c.putObject("function");
                fn.put("name", call.name());
                fn.put("arguments", call.argumentsJson() == null ? "{}" : call.argumentsJson());
            }
            return node;
        }
        node.put("content", message.getContent() == null ? "" : message.getContent());
        return node;
    }

    private ObjectNode toToolNode(ToolDefinition def) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "function");
        ObjectNode fn = node.putObject("function");
        fn.put("name", def.name());
        fn.put("description", def.description() == null ? "" : def.description());
        Map<String, Object> schema = def.parametersSchema() == null ? Map.of() : def.parametersSchema();
        fn.set("parameters", objectMapper.valueToTree(schema));
        return node;
    }

    private List<LlmToolCall> parseToolCalls(JsonNode toolCallsNode) {
        List<LlmToolCall> list = new ArrayList<>();
        if (toolCallsNode == null || !toolCallsNode.isArray()) {
            return list;
        }
        for (JsonNode n : toolCallsNode) {
            String id = n.path("id").asText("call_" + list.size());
            String name = n.path("function").path("name").asText(null);
            String args = n.path("function").path("arguments").asText("{}");
            if (!StringUtils.hasText(name)) {
                continue;
            }
            list.add(new LlmToolCall(id, name, args));
        }
        return list;
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        String t = node.asText(null);
        return StringUtils.hasText(t) ? t : null;
    }

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

    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
