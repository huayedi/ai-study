package com.erp.ai.common.client;

import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.model.ChatMessage;
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
 * OpenAI Compatible Chat Completions HTTP 客户端（含 Day18 路径 A tool_calls）。
 *
 * <h2>职责</h2>
 * 将本地 {@link ChatMessage} / {@link ToolDefinition} 组装为
 * {@code POST {baseUrl}/chat/completions} 请求，并解析 content 或 tool_calls。
 *
 * <h2>为何单独实现</h2>
 * DeepSeek、通义、Moonshot 等多数网关兼容同一协议；差异落在 baseUrl / model / key，
 * 不必为每个厂商写一套 Client。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 由 {@code LlmClientConfig} 在 {@code ai.provider} 为 openai-compatible 或其别名时创建。
 * 读取 {@link AiProperties} 的 apiKey、baseUrl、model、temperature；
 * HTTP 超时由共享 {@link RestTemplate}（见 {@code AppConfig}）控制。
 *
 * <h2>学习要点</h2>
 * <ul>
 *   <li>带 tools 时许多网关不支持同时强制 {@code response_format=json_object}</li>
 *   <li>401/404 错误信息要写成可操作的排查提示（Key / baseUrl）</li>
 *   <li>tool 角色消息必须带 {@code tool_call_id}</li>
 * </ul>
 */
public class OpenAiCompatibleLlmClient implements LlmClient {

    /** ai.* 配置：key、url、model、temperature */
    private final AiProperties properties;

    /** 同步 HTTP 客户端（已配置连接/读超时） */
    private final RestTemplate restTemplate;

    /** JSON 请求体组装与响应解析 */
    private final ObjectMapper objectMapper;

    /**
     * @param properties   AI 配置；不得为 {@code null}
     * @param restTemplate HTTP 客户端；不得为 {@code null}
     * @param objectMapper JSON 工具；不得为 {@code null}
     */
    public OpenAiCompatibleLlmClient(AiProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     *
     * @return 固定 {@code openai-compatible}
     */
    @Override
    public String providerName() {
        return "openai-compatible";
    }

    /**
     * 调用远程 Chat Completions。
     * <p>
     * <b>算法步骤：</b>
     * <ol>
     *   <li>校验 API Key，缺失则立刻失败（避免发出无鉴权请求）</li>
     *   <li>组装 body：model、temperature、messages；无 tools 时附加 json_object 格式约束</li>
     *   <li>若有 tools，写入 OpenAI function schema 数组</li>
     *   <li>Bearer 鉴权 POST；解析 choices[0].message</li>
     *   <li>优先返回 tool_calls；否则要求 content 非空</li>
     * </ol>
     * <b>失败语义：</b>一律包装为 {@link IllegalStateException}（含 401/404 友好说明），
     * 由全局异常处理映射为 HTTP 502。
     *
     * @param messages 完整消息；不应为 {@code null}
     * @param tools    工具定义；空或 {@code null} 表示不暴露 tools
     * @return 含 content 和/或 toolCalls 的结果
     * @throws IllegalStateException API Key 缺失、HTTP 失败、content 与 tool_calls 皆空、JSON 解析失败
     */
    @Override
    public LlmResult chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        // ---- 步骤 1：鉴权前置检查 ----
        String apiKey = properties.getApiKey() == null ? "" : properties.getApiKey().trim();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("ai.api-key 未配置。请设置环境变量 AI_API_KEY，或改回 ai.provider=mock");
        }

        boolean withTools = tools != null && !tools.isEmpty();

        // ---- 步骤 2：基础请求体 ----
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        body.put("temperature", properties.getTemperature());
        // 带 tools 时许多兼容网关不支持同时强制 json_object；终轮无 tool_calls 时再靠提示词约束 JSON
        if (!withTools) {
            body.put("response_format", objectMapper.createObjectNode().put("type", "json_object"));
        }

        // ---- 步骤 3：消息数组（含 tool / assistant.tool_calls 形态） ----
        ArrayNode messageNodes = body.putArray("messages");
        for (ChatMessage message : messages) {
            messageNodes.add(toMessageNode(message));
        }

        // ---- 步骤 4：可选 tools ----
        if (withTools) {
            ArrayNode toolNodes = body.putArray("tools");
            for (ToolDefinition def : tools) {
                toolNodes.add(toToolNode(def));
            }
        }

        // ---- 步骤 5：HTTP 头与 URL ----
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String url = trimTrailingSlash(properties.getBaseUrl()) + "/chat/completions";
        try {
            // ---- 步骤 6：发送并解析 ----
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body.toString(), headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode message = root.path("choices").path(0).path("message");

            List<LlmToolCall> toolCalls = parseToolCalls(message.path("tool_calls"));
            String content = textOrNull(message.path("content"));

            // ---- 步骤 7a：模型要求调工具 —— content 可空 ----
            if (!toolCalls.isEmpty()) {
                int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
                int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
                String model = root.path("model").asText(properties.getModel());
                return new LlmResult(content, model, promptTokens, completionTokens, toolCalls);
            }

            // ---- 步骤 7b：终答必须有文本 ----
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("模型返回内容为空且无 tool_calls");
            }

            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
            String model = root.path("model").asText(properties.getModel());
            return new LlmResult(content, model, promptTokens, completionTokens);
        } catch (HttpStatusCodeException ex) {
            // 4xx/5xx：附带可操作排查文案
            throw new IllegalStateException(describeHttpError(ex, url), ex);
        } catch (RestClientException ex) {
            // 超时、连接拒绝等
            throw new IllegalStateException("调用 LLM 失败: " + ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            // 保留已包装的业务语义异常
            throw ex;
        } catch (Exception ex) {
            // Jackson 等其它异常
            throw new IllegalStateException("解析 LLM 响应失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 把内部 {@link ChatMessage} 转为 OpenAI messages 元素。
     * <p>
     * 三种形态：
     * <ol>
     *   <li>{@code role=tool}：必须带 tool_call_id，可选 name</li>
     *   <li>assistant + toolCalls：回传上一轮模型的 function 请求</li>
     *   <li>普通 role+content</li>
     * </ol>
     *
     * @param message 单条消息；调用方保证非 null
     * @return 可直接放入请求数组的 ObjectNode
     */
    private ObjectNode toMessageNode(ChatMessage message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", message.getRole());
        // 形态 1：工具执行结果回填
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
        // 形态 2：assistant 发起的 tool_calls（多轮续聊时必须原样带回）
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
        // 形态 3：普通文本消息
        node.put("content", message.getContent() == null ? "" : message.getContent());
        return node;
    }

    /**
     * 把内部工具定义转为 OpenAI {@code tools[]} 元素（type=function）。
     *
     * @param def 工具定义
     * @return tools 数组元素；parameters 缺省时用空对象 schema
     */
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

    /**
     * 解析响应中的 {@code tool_calls} 数组。
     * <p>
     * 跳过缺少 function.name 的脏项；arguments 缺省为 {@code "{}"}。
     *
     * @param toolCallsNode JSON 节点；非数组时返回空列表
     * @return 可变的新建列表（调用方通常立即包进 LlmResult 变为不可变）
     */
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

    /**
     * 将 JSON 文本节点转为非空白 String，否则 {@code null}。
     *
     * @param node content 节点
     * @return 非空白文本或 {@code null}
     */
    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        String t = node.asText(null);
        return StringUtils.hasText(t) ? t : null;
    }

    /**
     * 把 HTTP 错误转成可操作的中文说明（截断 body，突出 401/404）。
     *
     * @param ex  Spring 抛出的状态码异常
     * @param url 实际请求 URL（便于核对 baseUrl）
     * @return 人类可读错误串（将作为 IllegalStateException message）
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

    /**
     * 去掉 baseUrl 末尾斜杠，避免拼出 {@code //chat/completions}。
     *
     * @param baseUrl 配置中的根地址
     * @return 规范化后的根；空白则空串
     */
    private static String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
