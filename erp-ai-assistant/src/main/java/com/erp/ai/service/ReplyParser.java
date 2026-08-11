package com.erp.ai.service;

import com.erp.ai.model.AssistantReply;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ReplyParser {

    private final ObjectMapper objectMapper;

    public ReplyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AssistantReply parse(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalArgumentException("模型返回为空");
        }

        String json = extractJson(rawContent.trim());
        try {
            JsonNode node = objectMapper.readTree(json);
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
            if (node.has("suggested_doc_type") && !node.get("suggested_doc_type").isNull()) {
                reply.setSuggestedDocType(node.get("suggested_doc_type").asText());
            } else if (node.has("suggestedDocType") && !node.get("suggestedDocType").isNull()) {
                reply.setSuggestedDocType(node.get("suggestedDocType").asText());
            }
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
