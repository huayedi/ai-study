package com.erp.ai.client;

import com.erp.ai.model.ChatMessage;
import com.erp.ai.tool.ChatToolOrchestrator;
import com.erp.ai.tool.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 Mock：支持 Day18 路径 A（返回 tool_calls）与路径 B（识别注入标记）。
 */
public class MockLlmClient implements LlmClient {

    private static final Pattern QTY = Pattern.compile("\"qty\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern STATUS = Pattern.compile("\"status\"\\s*:\\s*\"([A-Z]+)\"");
    private static final Pattern WAREHOUSE = Pattern.compile("\"warehouse\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ITEM = Pattern.compile("\"itemCode\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ITEM_CODE = Pattern.compile("(?i)\\b(ITEM-[A-Z0-9]+|A\\d{3})\\b");

    private final ObjectMapper objectMapper;

    public MockLlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "mock";
    }

    @Override
    public LlmResult chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        String allText = joinAllText(messages);
        String userText = latestOriginalUserText(messages);
        String userLower = userText.toLowerCase(Locale.ROOT);
        boolean withTools = tools != null && !tools.isEmpty();

        // 路径 A：已有 tool 回填 → 产出最终 JSON
        if (hasToolRole(messages)) {
            return finalFromToolMessages(messages, allText);
        }

        // 路径 A：暴露了 tools 且是库存问句 → 先发 tool_calls
        if (withTools && needsInventory(userLower) && !isWriteIntent(userLower)) {
            String item = guessItem(userText).orElse("A001");
            String warehouse = guessWarehouse(userText).orElse(null);
            ObjectNode args = objectMapper.createObjectNode();
            args.put("itemCode", item);
            if (warehouse != null) {
                args.put("warehouse", warehouse);
            }
            LlmToolCall call = new LlmToolCall(
                    "call_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                    "queryInventory",
                    args.toString()
            );
            return new LlmResult(null, "mock-erp-assistant", 32, 16, List.of(call));
        }

        if (withTools && needsPeriod(userLower) && !isWriteIntent(userLower)) {
            ObjectNode args = objectMapper.createObjectNode();
            args.put("company", "主公司");
            args.put("period", guessPeriod(userText).orElse("2026-08"));
            LlmToolCall call = new LlmToolCall(
                    "call_mock_period",
                    "queryPeriodStatus",
                    args.toString()
            );
            return new LlmResult(null, "mock-erp-assistant", 32, 16, List.of(call));
        }

        // 写意图 / 路径 B 拦截标记：拒写
        if (allText.contains(ChatToolOrchestrator.WRITE_BLOCK_MARKER) || isWriteIntent(userLower)) {
            return jsonResult(writeBlockedJson());
        }

        // 路径 B 注入结果
        if (allText.contains(ChatToolOrchestrator.TOOL_RESULT_MARKER)) {
            return jsonResult(fillFromToolResultText(allText));
        }

        return jsonResult(keywordJson(userLower));
    }

    private LlmResult finalFromToolMessages(List<ChatMessage> messages, String allText) {
        String toolBlob = messages.stream()
                .filter(m -> "tool".equals(m.getRole()))
                .map(ChatMessage::getContent)
                .reduce("", (a, b) -> a + "\n" + b);
        boolean anyFail = toolBlob.contains("\"ok\":false") || toolBlob.contains("\"ok\": false");
        ObjectNode root;
        if (anyFail || toolBlob.contains("missing required")) {
            root = objectMapper.createObjectNode();
            root.put("answer", "只读工具未返回可用数据（可能缺仓库等参数）。我不会编造库存数量；请补全参数后重试。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.4);
        } else {
            root = fillFromToolResultText(toolBlob.isBlank() ? allText : toolBlob);
        }
        return jsonResult(root);
    }

    private ObjectNode writeBlockedJson() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("answer", "已拦截写意图：本助手没有改库存/过账工具，不会把数量改成你说的值。请有权限的同事在 ERP 中人工操作。");
        root.put("need_human", true);
        root.putNull("suggested_doc_type");
        root.putArray("required_fields");
        root.put("confidence", 0.9);
        return root;
    }

    private ObjectNode fillFromToolResultText(String allText) {
        ObjectNode root = objectMapper.createObjectNode();
        boolean ok = allText.contains("ok=true") || allText.contains("\"ok\":true") || allText.contains("\"qty\"");
        if (!ok && (allText.contains("ok=false") || allText.contains("\"ok\":false"))) {
            root.put("answer", "只读工具未查到可用结果（可能缺仓库/存货参数或假数据无此键）。我不会编造库存数量；请补全参数后重试，或人工在库存查询核对。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.4);
            return root;
        }

        Matcher qty = QTY.matcher(allText);
        if (qty.find()) {
            String item = matchOr(ITEM, allText, "存货");
            String wh = matchOr(WAREHOUSE, allText, "仓库");
            root.put("answer", "根据只读工具查询：" + item + " 在 " + wh + " 的现存量是 " + qty.group(1)
                    + "（学习假数据）。其它仓库数量未经查询，我不编造。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.85);
            return root;
        }

        Matcher status = STATUS.matcher(allText);
        if (status.find()) {
            root.put("answer", "根据只读工具查询：期间状态为 " + status.group(1) + "（学习假数据）。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.85);
            return root;
        }

        root.put("answer", "根据只读工具结果回答（mock）。");
        root.put("need_human", false);
        root.putNull("suggested_doc_type");
        root.putArray("required_fields");
        root.put("confidence", 0.8);
        return root;
    }

    private ObjectNode keywordJson(String userText) {
        ObjectNode root = objectMapper.createObjectNode();
        if (containsAny(userText, "【教材资料】", "教材资料")) {
            root.put("answer", "结论：根据提供的教材资料回答（mock）。若资料含采购主链路，则常见顺序为物资请购→采购订单→到货单→采购入库→采购发票。请以 sources 中的文件章节为准。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.7);
        } else if (containsAny(userText, "库存", "现存量", "有多少")) {
            root.put("answer", "我无法直接查询实时库存。请在【库存查询】按物料编码+仓库核对，或接入只读库存工具后再问我。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.55);
        } else if (containsAny(userText, "采购", "请购", "供应商")) {
            root.put("answer", "若要发起采购，建议先建采购申请/采购订单草稿。请确认供应商、物料、数量、交期、税率后由人工保存。");
            root.put("need_human", true);
            root.put("suggested_doc_type", "采购订单");
            ArrayNode fields = root.putArray("required_fields");
            fields.add("供应商");
            fields.add("物料编码");
            fields.add("数量");
            fields.add("含税单价");
            fields.add("交货日期");
            root.put("confidence", 0.72);
        } else if (containsAny(userText, "过账", "删除", "改库存", "付款")) {
            root.put("answer", "这是高风险操作。请先核对单据状态与期间是否打开，并由有权限的同事在系统中确认执行；我不会直接改账。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.8);
        } else if (containsAny(userText, "期间", "关账", "凭证不平衡")) {
            root.put("answer", "可按此排查：1) 确认会计期间是否打开；2) 检查借贷是否平衡；3) 查看是否有未审核凭证；4) 仍失败则截图报错给财务顾问。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.7);
        } else {
            root.put("answer", "我是 ERP 助手（mock 模式）。你可以问：采购录单需要哪些字段、期间关闭怎么排查、某类报错如何处理。配置真实 API 后回答会更灵活。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.6);
        }
        return root;
    }

    private LlmResult jsonResult(ObjectNode root) {
        String content = root.toString();
        int completionTokens = Math.max(16, content.length() / 4);
        return new LlmResult(content, "mock-erp-assistant", 48, completionTokens);
    }

    private static boolean hasToolRole(List<ChatMessage> messages) {
        return messages.stream().anyMatch(m -> "tool".equals(m.getRole()));
    }

    private static boolean needsInventory(String text) {
        return containsAny(text, "库存", "现存量", "有多少货", "还剩多少")
                || (containsAny(text, "多少") && containsAny(text, "仓"));
    }

    private static boolean needsPeriod(String text) {
        return containsAny(text, "期间", "关账", "是否打开", "期间状态");
    }

    private static boolean isWriteIntent(String text) {
        return containsAny(text,
                "改成", "改库存", "调库存", "写成", "调成",
                "过账", "删单", "删除单据", "付款", "关闭期间", "重开期间",
                "writeinventory", "帮我改");
    }

    private static java.util.Optional<String> guessItem(String text) {
        Matcher m = ITEM_CODE.matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1).toUpperCase(Locale.ROOT));
        }
        return java.util.Optional.empty();
    }

    private static java.util.Optional<String> guessWarehouse(String text) {
        if (text.contains("原料仓")) {
            return java.util.Optional.of("原料仓");
        }
        if (text.contains("成品仓")) {
            return java.util.Optional.of("成品仓");
        }
        return java.util.Optional.empty();
    }

    private static java.util.Optional<String> guessPeriod(String text) {
        Matcher m = Pattern.compile("(20\\d{2}-\\d{2})").matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1));
        }
        return java.util.Optional.empty();
    }

    private static String matchOr(Pattern p, String text, String fallback) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : fallback;
    }

    private static String joinAllText(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : messages) {
            if (message != null && message.getContent() != null) {
                sb.append(message.getContent()).append('\n');
            }
        }
        return sb.toString();
    }

    private static String latestOriginalUserText(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (!"user".equals(message.getRole()) || message.getContent() == null) {
                continue;
            }
            String c = message.getContent();
            if (c.contains(ChatToolOrchestrator.TOOL_RESULT_MARKER)
                    || c.contains(ChatToolOrchestrator.WRITE_BLOCK_MARKER)) {
                continue;
            }
            return c;
        }
        return "";
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT)) || text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
