package com.erp.ai.scenario.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Day27：ERP 场景模式规则路由（手册问答 / 报错 / 草稿 / 实时查询 / 安全拒答 / 审批摘要）。
 */
@Component
public class ScenarioClassifier {

    public enum Mode {
        RAG_MANUAL,
        ERROR_EXPLAIN,
        DRAFT_PO,
        TOOL_READONLY,
        SECURITY_REFUSE,
        APPROVAL_SUMMARY,
        CHAT_GENERAL
    }

    public record Classification(Mode mode, String reason, double confidence) {
    }

    public Classification classify(String utterance) {
        if (utterance == null || utterance.isBlank()) {
            return new Classification(Mode.CHAT_GENERAL, "空输入", 0.3);
        }
        String t = utterance.trim();
        String lower = t.toLowerCase(Locale.ROOT);

        if (containsAny(t, lower, "忽略规则", "系统提示", "绕过审批", "直接改库存", "ignore all", "jailbreak")) {
            return new Classification(Mode.SECURITY_REFUSE, "安全/注入类话术", 0.95);
        }
        if (containsAny(t, lower, "下采购", "买", "草稿", "录单", "帮我下单")
                && containsAny(t, lower, "供应", "A001", "ITEM-", "个")) {
            return new Classification(Mode.DRAFT_PO, "录单草稿诉求", 0.85);
        }
        if (containsAny(t, lower, "库存", "现存量", "还有多少", "期间状态", "期间是否打开")) {
            return new Classification(Mode.TOOL_READONLY, "需要实时/结构化只读查询", 0.85);
        }
        if (containsAny(t, lower, "报错", "失败", "不平衡", "无法审核", "提示库存不足")) {
            return new Classification(Mode.ERROR_EXPLAIN, "报错解释诉求", 0.8);
        }
        if (containsAny(t, lower, "审批摘要", "风险点", "这笔单风险")) {
            return new Classification(Mode.APPROVAL_SUMMARY, "审批摘要概念模式", 0.75);
        }
        if (containsAny(t, lower, "主链路", "教材", "单据有哪些", "期间关闭后", "采购订单保存前")) {
            return new Classification(Mode.RAG_MANUAL, "手册/制度依据问答", 0.85);
        }
        return new Classification(Mode.CHAT_GENERAL, "通用 Chat", 0.55);
    }

    private static boolean containsAny(String raw, String lower, String... keys) {
        for (String key : keys) {
            if (raw.contains(key) || lower.contains(key.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
