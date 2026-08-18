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
 * 草稿模型输出解析器（draft 域）。
 * <p>
 * <b>职责</b>：把 LLM 返回的原始文本（可能夹带 markdown 代码围栏）提取为 JSON，
 * 再映射到 {@link DraftResponse} 的业务字段部分（单据类型、fields、missing、warnings、
 * needHuman、confidence）。观测字段（trace、usage 等）由 {@link DraftService} 后续填充。
 * <p>
 * <b>在系统中的位置</b>：draft 域 service 辅助组件；位于 LLM 调用与后置业务规则之间。
 * 与 chat 域 {@code ReplyParser} 平行但 schema 不同（草稿字段 vs answer/need_human）。
 * <p>
 * <b>对应学习 Day</b>：Day20。
 * <p>
 * <b>调用链 / 上下游</b>：
 * {@code DraftService} 取得 {@code LlmResult.getContent()} → {@link #parse(String)} →
 * 部分填充的 {@link DraftResponse} → Service 再跑日期规则 / queryItem / 必填强制。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>只解析，不写库、不调工具、不改会话</li>
 *   <li>兼容 snake_case 与 camelCase 关键键（如 need_human / needHuman）</li>
 *   <li>缺 need_human 时默认 true（安全偏向人工确认）</li>
 *   <li>空内容或非法 JSON 抛 {@link IllegalArgumentException}，触发 Service 重试</li>
 * </ul>
 *
 * @see DraftService
 * @see DraftResponse
 */
@Component
public class DraftParser {

    /** Jackson：读树与标量转换 */
    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper Spring 注入的共享 ObjectMapper
     */
    public DraftParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将模型原始输出解析为草稿业务字段。
     * <p>
     * 步骤概要：非空校验 → 提取 JSON 子串 → readTree → 映射各字段。
     * <p>
     * 边界：
     * <ul>
     *   <li>空白 raw → IllegalArgumentException「模型返回为空」</li>
     *   <li>JSON 结构非法 → IllegalArgumentException「草稿 JSON 非法」</li>
     *   <li>已是 IllegalArgumentException 则原样抛出，避免二次包装丢失语义</li>
     * </ul>
     *
     * @param rawContent 模型返回的原始字符串，可含 ```json 围栏
     * @return 仅填了业务相关字段的 {@link DraftResponse}（trace/usage 等仍为空默认）
     * @throws IllegalArgumentException 内容为空或 JSON 无法解析/映射时
     */
    public DraftResponse parse(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalArgumentException("模型返回为空");
        }
        // 先剥代码围栏或截取首尾大括号，再交给 Jackson
        String json = extractJson(rawContent.trim());
        try {
            JsonNode node = objectMapper.readTree(json);
            DraftResponse draft = new DraftResponse();

            // 单据类型：优先 snake_case，其次 camelCase，都没有则默认采购订单
            if (node.hasNonNull("suggested_doc_type")) {
                draft.setSuggestedDocType(node.get("suggested_doc_type").asText());
            } else if (node.hasNonNull("suggestedDocType")) {
                draft.setSuggestedDocType(node.get("suggestedDocType").asText());
            } else {
                draft.setSuggestedDocType("采购订单");
            }

            // fields 必须是 object；其它类型忽略为空 Map，避免模型把 fields 输出成数组时炸解析
            Map<String, Object> fields = new LinkedHashMap<>();
            JsonNode fieldsNode = node.has("fields") ? node.get("fields") : null;
            if (fieldsNode != null && fieldsNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> it = fieldsNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> e = it.next();
                    // 标量转 Java 类型，便于后续规则用 Number/String 比较
                    fields.put(e.getKey(), jsonValue(e.getValue()));
                }
            }
            draft.setFields(fields);

            draft.setMissing(readStringList(node, "missing"));
            draft.setWarnings(readStringList(node, "warnings"));

            // need_human 缺省 true：草稿默认偏安全，要求人工看一眼
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

    /**
     * 读取 JSON 数组字段为去空白后的字符串列表；字段缺失或非数组时返回空列表。
     *
     * @param node  根 JsonNode
     * @param field 字段名（如 missing / warnings）
     * @return 字符串列表，不为 null
     */
    private List<String> readStringList(JsonNode node, String field) {
        List<String> out = new ArrayList<>();
        if (!node.has(field) || !node.get(field).isArray()) {
            return out;
        }
        for (JsonNode item : node.get(field)) {
            // 跳过 null 与空白，避免 missing 里出现空串干扰展示
            if (item != null && !item.isNull() && StringUtils.hasText(item.asText())) {
                out.add(item.asText());
            }
        }
        return out;
    }

    /**
     * 将 JsonNode 标量转为 Java 对象：整数→Long，小数→Double，布尔→Boolean，其它→文本；null→null。
     * <p>
     * 为何区分 integral：数量等字段后续可能当数字用，避免全变成字符串导致比较困难。
     *
     * @param node 字段值节点
     * @return Java 标量或 null
     */
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

    /**
     * 从模型原始输出中提取 JSON 子串。
     * <p>
     * 策略顺序：
     * <ol>
     *   <li>若以 markdown 代码围栏开头，取首行换行后到最后一个围栏之间</li>
     *   <li>否则取第一个 {@code '{'} 到最后一个 {@code '}'}（包容多余前后缀废话）</li>
     *   <li>再否则原样返回，交给 Jackson 报错</li>
     * </ol>
     * 包可见：便于单测直接覆盖提取逻辑。
     *
     * @param raw 已 trim 的原始文本
     * @return 尽可能干净的 JSON 文本
     */
    static String extractJson(String raw) {
        if (raw.startsWith("```")) {
            int firstNl = raw.indexOf('\n');
            int lastFence = raw.lastIndexOf("```");
            // 需要同时有「首行后内容」和「结束围栏」，否则不硬截，避免切坏
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
