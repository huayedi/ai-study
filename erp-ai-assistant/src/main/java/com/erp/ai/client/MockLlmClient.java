package com.erp.ai.client;

import com.erp.ai.model.ChatMessage;
import com.erp.ai.tool.ChatToolOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 Mock 实现：不访问外网，按关键词返回固定 JSON。
 * <p>
 * Day18：识别路径 B 注入的「系统只读查询结果 / 写操作拦截」，避免无依据编数量。
 */
public class MockLlmClient implements LlmClient {

    private static final Pattern QTY = Pattern.compile("\"qty\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern STATUS = Pattern.compile("\"status\"\\s*:\\s*\"([A-Z]+)\"");
    private static final Pattern WAREHOUSE = Pattern.compile("\"warehouse\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ITEM = Pattern.compile("\"itemCode\"\\s*:\\s*\"([^\"]+)\"");

    private final ObjectMapper objectMapper;

    public MockLlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "mock";
    }

    @Override
    public LlmResult chat(List<ChatMessage> messages) {
        String allText = joinAllText(messages);
        // 关键词路由只用「用户原话」，避免 system 提示词里的「改库存」等词误触发
        String userText = latestOriginalUserText(messages).toLowerCase(Locale.ROOT);
        ObjectNode root = objectMapper.createObjectNode();

        if (allText.contains(ChatToolOrchestrator.WRITE_BLOCK_MARKER)) {
            root.put("answer", "已拦截写意图：本助手没有改库存/过账工具，不会把数量改成你说的值。请有权限的同事在 ERP 中人工操作。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.9);
        } else if (allText.contains(ChatToolOrchestrator.TOOL_RESULT_MARKER)) {
            fillFromToolResult(root, allText);
        } else if (containsAny(userText, "【教材资料】", "教材资料")) {
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

        String content = root.toString();
        int promptTokens = Math.max(16, messages.stream().mapToInt(m -> m.getContent() == null ? 0 : m.getContent().length() / 4).sum());
        int completionTokens = Math.max(16, content.length() / 4);
        return new LlmResult(content, "mock-erp-assistant", promptTokens, completionTokens);
    }

    private void fillFromToolResult(ObjectNode root, String allText) {
        boolean ok = allText.contains("ok=true");
        if (!ok) {
            root.put("answer", "只读工具未查到可用结果（可能缺仓库/存货参数或假数据无此键）。我不会编造库存数量；请补全参数后重试，或人工在库存查询核对。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.4);
            return;
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
            return;
        }

        Matcher status = STATUS.matcher(allText);
        if (status.find()) {
            root.put("answer", "根据只读工具查询：期间状态为 " + status.group(1) + "（学习假数据）。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.85);
            return;
        }

        // queryItem 等其它成功结果：尽量引用 data 片段
        int dataIdx = allText.indexOf("data=");
        String snippet = dataIdx >= 0 ? allText.substring(dataIdx, Math.min(allText.length(), dataIdx + 180)) : "已查到主数据";
        root.put("answer", "根据只读工具结果回答：" + snippet.replace('\n', ' '));
        root.put("need_human", false);
        root.putNull("suggested_doc_type");
        root.putArray("required_fields");
        root.put("confidence", 0.8);
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

    /** 最近一条非工具注入的 user 消息（用于 mock 关键词分支） */
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
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
