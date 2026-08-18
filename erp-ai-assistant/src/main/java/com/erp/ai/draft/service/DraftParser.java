package com.erp.ai.draft.service;

import com.erp.ai.draft.dto.DraftResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把模型草稿 JSON 解析为 {@link DraftResponse} 的业务字段部分。
 */
@Component
public class DraftParser {

    private final ObjectMapper objectMapper;

    public DraftParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DraftResponse parse(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalArgumentException("模型返回为空");
        }
        String json = extractJson(rawContent.trim());
        try {
            JsonNode node = objectMapper.readTree(json);
            DraftResponse draft = new DraftResponse();

            if (node.hasNonNull("suggested_doc_type")) {
                draft.setSuggestedDocType(node.get("suggested_doc_type").asText());
            } else if (node.hasNonNull("suggestedDocType")) {
                draft.setSuggestedDocType(node.get("suggestedDocType").asText());
            } else {
                draft.setSuggestedDocType("采购订单");
            }

            Map<String, Object> fields = new LinkedHashMap<>();
            JsonNode fieldsNode = node.has("fields") ? node.get("fields") : null;
            if (fieldsNode != null && fieldsNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> it = fieldsNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> e = it.next();
                    fields.put(e.getKey(), jsonValue(e.getValue()));
                }
            }
            draft.setFields(fields);

            draft.setMissing(readStringList(node, "missing"));
            draft.setWarnings(readStringList(node, "warnings"));

            if (node.has("need_human")) {
                draft.setNeedHuman(node.get("need_human").asBoolean());
            } else if (node.has("needHuman")) {
                draft.setNeedHuman(node.get("needHuman").asBoolean());
            } else {
                draft.setNeedHuman(true);
            }

            if (node.has("confidence") && !node.get("confidence").isNull()) {
                draft.setConfidence(node.get("confidence").asDouble());
            }
            return draft;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("草稿 JSON 非法: " + ex.getMessage(), ex);
        }
    }

    private List<String> readStringList(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (!node.has(field) || !node.get(field).isArray()) {
            return out;
        }
        for (JsonNode item : node.get(field)) {
            if (item != null && !item.isNull() && StringUtils.hasText(item.asText())) {
                out.add(item.asText());
            }
        }
        return out;
    }

    private static Object jsonValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            if (node.isIntegralNumber()) {
                return node.longValue();
            }
            return node.doubleValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        return node.asText();
    }

    static String extractJson(String raw) {
        if (raw.startsWith("```")) {
            int firstNl = raw.indexOf('\n');
            int lastFence = raw.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                return raw.substring(firstNl + 1, lastFence).trim();
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
