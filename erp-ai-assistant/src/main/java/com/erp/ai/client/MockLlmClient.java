package com.erp.ai.client;

import com.erp.ai.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;

/**
 * 由 {@link com.erp.ai.config.LlmClientConfig} 按 ai.provider 创建，不要再加 @Component，
 * 否则会和配置工厂重复注册。
 */
public class MockLlmClient implements LlmClient {

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
        String userText = latestUserText(messages).toLowerCase(Locale.ROOT);
        ObjectNode root = objectMapper.createObjectNode();

        if (containsAny(userText, "库存", "现存量", "有多少")) {
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

    private static String latestUserText(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if ("user".equals(message.getRole())) {
                return message.getContent() == null ? "" : message.getContent();
            }
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
