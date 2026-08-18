package com.erp.ai.common.parse;

import com.erp.ai.common.model.AssistantReply;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 把模型原始文本解析为业务对象 {@link AssistantReply}。
 *
 * <h2>职责</h2>
 * 在「传输层字符串」与「业务结构化回复」之间做提取、字段兼容与必填校验。
 *
 * <h2>为何单独做解析器</h2>
 * <ul>
 *   <li>模型可能包一层 Markdown 代码块（{@code ```json ... ```}）</li>
 *   <li>字段名可能是 snake_case 或 camelCase</li>
 *   <li>缺少关键字段时应失败，触发上层重试，而不是把脏数据返回前端</li>
 * </ul>
 *
 * <h2>与 Spring 的关系</h2>
 * {@code @Component}，注入共享 {@link ObjectMapper}；由 Chat 服务在拿到 {@code LlmResult#getContent()} 后调用。
 *
 * <h2>学习要点</h2>
 * 解析失败抛 {@link IllegalArgumentException}（偏「模型输出不合规」），
 * 与 LLM HTTP 失败的 {@link IllegalStateException} 区分，便于重试策略分流。
 */
@Component
public class ReplyParser {

    /** 共享 Jackson；用于 readTree 与集合反序列化 */
    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper Spring 容器中的 ObjectMapper
     */
    public ReplyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析并校验模型输出。
     * <p>
     * <b>算法步骤：</b>
     * <ol>
     *   <li>拒空：无文本直接失败</li>
     *   <li>{@link #extractJson} 去掉 Markdown 围栏或截取最外层花括号</li>
     *   <li>{@code readTree} 得到 JsonNode</li>
     *   <li>强制校验 {@code answer} 非空、{@code need_human}/{@code needHuman} 存在</li>
     *   <li>可选字段：单据类型、必填字段列表、confidence（兼容两种命名）</li>
     *   <li>映射到 {@link AssistantReply}</li>
     * </ol>
     * <b>失败语义：</b>
     * <ul>
     *   <li>空内容 / 缺必填 / JSON 非法 → {@link IllegalArgumentException}</li>
     *   <li>已是 IllegalArgumentException 则原样抛出，避免被包成「JSON 解析失败」掩盖原因</li>
     * </ul>
     *
     * @param rawContent 模型返回的原始字符串（可为带 markdown 的脏文本）
     * @return 结构化回复；不会为 {@code null}
     * @throws IllegalArgumentException 空内容、缺字段或 JSON 非法时抛出
     */
    public AssistantReply parse(String rawContent) {
        // ---- 步骤 1：空内容直接失败，触发上层重试 ----
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalArgumentException("模型返回为空");
        }

        // ---- 步骤 2：从脏文本提取纯 JSON 子串 ----
        String json = extractJson(rawContent.trim());
        try {
            // ---- 步骤 3：解析为树 ----
            JsonNode node = objectMapper.readTree(json);

            // ---- 步骤 4：必填字段校验 —— answer / need_human ----
            if (!node.hasNonNull("answer") || !StringUtils.hasText(node.get("answer").asText())) {
                throw new IllegalArgumentException("缺少 answer 字段");
            }
            if (!node.has("need_human") && !node.has("needHuman")) {
                throw new IllegalArgumentException("缺少 need_human 字段");
            }

            // ---- 步骤 5：映射必填 ----
            AssistantReply reply = new AssistantReply();
            reply.setAnswer(node.get("answer").asText());
            reply.setNeedHuman(node.has("need_human")
                    ? node.get("need_human").asBoolean()
                    : node.get("needHuman").asBoolean());

            // ---- 步骤 6：可选 —— 单据类型（snake / camel） ----
            if (node.has("suggested_doc_type") && !node.get("suggested_doc_type").isNull()) {
                reply.setSuggestedDocType(node.get("suggested_doc_type").asText());
            } else if (node.has("suggestedDocType") && !node.get("suggestedDocType").isNull()) {
                reply.setSuggestedDocType(node.get("suggestedDocType").asText());
            }

            // ---- 步骤 7：可选 —— 必填字段列表 ----
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

            // ---- 步骤 8：可选 —— 置信度 ----
            if (node.has("confidence") && !node.get("confidence").isNull()) {
                reply.setConfidence(node.get("confidence").asDouble());
            }
            return reply;
        } catch (IllegalArgumentException ex) {
            // 保留「缺字段」等明确语义，不要二次包装
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("JSON 解析失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 从原始文本中提取 JSON 子串。
     * <p>
     * <b>策略（按顺序）：</b>
     * <ol>
     *   <li>若以 {@code ```} 开头：去掉首行围栏与末尾围栏，取中间正文</li>
     *   <li>否则截取第一个 {@code '{'} 到最后一个 {@code '}'}</li>
     *   <li>若仍找不到花括号，原样返回（交给 readTree 失败）</li>
     * </ol>
     * <b>边界：</b>不处理嵌套代码块的复杂情况；学习项目以「能容错常见模型习惯」为准。
     *
     * @param raw 已 trim 的原始串
     * @return 期望为 JSON 对象文本
     */
    private static String extractJson(String raw) {
        // 策略 1：Markdown fenced code block
        if (raw.startsWith("```")) {
            int firstNewline = raw.indexOf('\n');
            int lastFence = raw.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return raw.substring(firstNewline + 1, lastFence).trim();
            }
        }
        // 策略 2：最外层花括号切片（模型前后可能有解释性废话）
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }
}
