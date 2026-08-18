package com.erp.ai.common.parse;

import com.erp.ai.common.model.AssistantReply;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 把模型原始文本解析为 {@link AssistantReply}。
 * <p>
 * 为什么要单独做解析器：
 * <ul>
 *   <li>模型可能包一层 Markdown 代码块</li>
 *   <li>字段名可能是 snake_case 或 camelCase</li>
 *   <li>缺少关键字段时应失败，触发上层重试，而不是把脏数据返回前端</li>
 * </ul>
 */
@Component
public class ReplyParser {

    private final ObjectMapper objectMapper;

    public ReplyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析并校验模型输出。
     *
     * @param rawContent 模型返回的原始字符串
     * @return 结构化回复
     * @throws IllegalArgumentException 空内容、缺字段或 JSON 非法时抛出
     */
    public AssistantReply parse(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalArgumentException("模型返回为空");
        }

        String json = extractJson(rawContent.trim());
        try {
            JsonNode node = objectMapper.readTree(json);

            // 必填字段校验：answer / need_human
            if (!node.hasNonNull("answer") || !StringUtils.hasText(node.get("answer").asText())) {
                throw new IllegalArgumentException("缺少 answer 字段");
            }
            if (!node.has("need_human") && !node.has("needHuman")) {
                throw new IllegalArgumentException("缺少 need_human 字段");
            }

            AssistantReply reply = new AssistantReply();
            reply.setAnswer(node.get("answer").asText());
            reply.setNeedHuman(node.has("need_human")
                    ? node.get("need_human").asBoolean()
                    : node.get("needHuman").asBoolean());

            // 可选字段：单据类型
            if (node.has("suggested_doc_type") && !node.get("suggested_doc_type").isNull()) {
                reply.setSuggestedDocType(node.get("suggested_doc_type").asText());
            } else if (node.has("suggestedDocType") && !node.get("suggestedDocType").isNull()) {
                reply.setSuggestedDocType(node.get("suggestedDocType").asText());
            }

            // 可选字段：必填字段列表
            if (node.has("required_fields") && node.get("required_fields").isArray()) {
                reply.setRequiredFields(objectMapper.convertValue(
                        node.get("required_fields"),
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)
                ));
            } else if (node.has("requiredFields") && node.get("requiredFields").isArray()) {
                reply.setRequiredFields(objectMapper.convertValue(
                        node.get("requiredFields"),
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)
                ));
            }

            // 可选字段：置信度
            if (node.has("confidence") && !node.get("confidence").isNull()) {
                reply.setConfidence(node.get("confidence").asDouble());
            }
            return reply;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("JSON 解析失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 从原始文本中提取 JSON：
     * <ol>
     *   <li>若是 ```json ... ``` 代码块，去掉围栏</li>
     *   <li>否则截取第一个 '{' 到最后一个 '}'</li>
     * </ol>
     */
    private static String extractJson(String raw) {
        if (raw.startsWith("```")) {
            int firstNewline = raw.indexOf('\n');
            int lastFence = raw.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return raw.substring(firstNewline + 1, lastFence).trim();
            }
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }
}
