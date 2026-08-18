package com.erp.ai.scenario.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * ERP 场景模式规则路由器（手册问答 / 报错 / 草稿 / 实时查询 / 安全拒答 / 审批摘要 / 通用聊天）。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>根据用户话术中的关键词，规则式判定 {@link Mode}</li>
 *   <li>返回 {@link Classification}：模式、人类可读 reason、置信度 confidence</li>
 *   <li>不做 LLM 调用，不做下游业务转发（转发由 {@link ScenarioService} 负责）</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * {@code scenario.service} 中的纯分类组件。被 {@link ScenarioService#handle} 首先调用；
 * 评测集中 {@code type=scenario} 的用例通过 ScenarioService 间接验证本类输出的 mode。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day27：场景模式库规则路由。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * ScenarioService.handle
 *   → ScenarioClassifier.classify(utterance)
 *   → Classification(mode, reason, confidence)
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>判定顺序固定：安全 → 草稿 → 只读工具 → 报错 → 审批摘要 → 手册 RAG → 默认 Chat</li>
 *   <li>先命中先返回，不做多标签并列</li>
 *   <li>空输入归为 {@link Mode#CHAT_GENERAL}，低置信度 0.3</li>
 *   <li>关键词匹配同时检查原始串与小写串，以覆盖大小写差异</li>
 * </ul>
 */
@Component
public class ScenarioClassifier {

    /**
     * 场景模式枚举：与下游 API / 编排分支一一对应。
     */
    public enum Mode {
        /** 手册/制度类问答 → RAG ask。 */
        RAG_MANUAL,
        /** 报错解释诉求 → 同样走 RAG ask。 */
        ERROR_EXPLAIN,
        /** 采购录单草稿 → draft purchase-order。 */
        DRAFT_PO,
        /** 需要实时/结构化只读查询 → tool invoke（库存/期间/物料）。 */
        TOOL_READONLY,
        /** 安全/注入类话术 → security probe 风格拒答。 */
        SECURITY_REFUSE,
        /** 审批摘要概念模式 → 内联固定摘要（不自动审批）。 */
        APPROVAL_SUMMARY,
        /** 兜底通用对话 → chat。 */
        CHAT_GENERAL
    }

    /**
     * 分类结果记录。
     *
     * @param mode       判定出的场景模式
     * @param reason     人类可读的判定理由（中文短句）
     * @param confidence 规则置信度（0~1，启发式，非概率校准）
     */
    public record Classification(Mode mode, String reason, double confidence) {
    }

    /**
     * 对单条话术做规则分类。
     * <p>
     * 业务含义：把自然语言意图映射到可编排的 Mode，供路由展示或 assist 转发。
     * 边界：null/空白 → CHAT_GENERAL；各 if 互斥，按代码顺序短路返回。
     * </p>
     *
     * @param utterance 用户输入；可为 null
     * @return 分类结果；永不返回 null
     */
    public Classification classify(String utterance) {
        // 空输入：无法抽取意图，落到通用 Chat，并给较低置信度
        if (utterance == null || utterance.isBlank()) {
            return new Classification(Mode.CHAT_GENERAL, "空输入", 0.3);
        }
        String t = utterance.trim();
        String lower = t.toLowerCase(Locale.ROOT);

        // 优先级最高：安全/注入类关键词 → 拒答模式
        if (containsAny(t, lower, "忽略规则", "系统提示", "绕过审批", "直接改库存", "ignore all", "jailbreak")) {
            return new Classification(Mode.SECURITY_REFUSE, "安全/注入类话术", 0.95);
        }
        // 录单草稿：同时具备「下单类」与「物料/供应商线索」关键词才判 DRAFT_PO
        if (containsAny(t, lower, "下采购", "买", "草稿", "录单", "帮我下单")
                && containsAny(t, lower, "供应", "A001", "ITEM-", "个")) {
            return new Classification(Mode.DRAFT_PO, "录单草稿诉求", 0.85);
        }
        // 库存/现存量/期间 → 只读工具查询
        if (containsAny(t, lower, "库存", "现存量", "还有多少", "期间状态", "期间是否打开")) {
            return new Classification(Mode.TOOL_READONLY, "需要实时/结构化只读查询", 0.85);
        }
        // 报错/失败/不平衡等 → 错误解释（走 RAG）
        if (containsAny(t, lower, "报错", "失败", "不平衡", "无法审核", "提示库存不足")) {
            return new Classification(Mode.ERROR_EXPLAIN, "报错解释诉求", 0.8);
        }
        // 审批摘要概念演示
        if (containsAny(t, lower, "审批摘要", "风险点", "这笔单风险")) {
            return new Classification(Mode.APPROVAL_SUMMARY, "审批摘要概念模式", 0.75);
        }
        // 手册/教材/主链路制度问答 → RAG
        if (containsAny(t, lower, "主链路", "教材", "单据有哪些", "期间关闭后", "采购订单保存前")) {
            return new Classification(Mode.RAG_MANUAL, "手册/制度依据问答", 0.85);
        }
        // 默认兜底
        return new Classification(Mode.CHAT_GENERAL, "通用 Chat", 0.55);
    }

    /**
     * 判断原文或小写串是否包含任一关键词。
     * <p>
     * 对每个 key：先 {@code raw.contains(key)}，再 {@code lower.contains(key 的小写)}，
     * 任一成功即 true。用于中英文混排关键词表。
     * </p>
     *
     * @param raw   修剪后的原始话术
     * @param lower 同一话术的 ROOT locale 小写形式
     * @param keys  待匹配关键词变长参数
     * @return 任一关键词命中则为 true；keys 为空时为 false
     */
    private static boolean containsAny(String raw, String lower, String... keys) {
        for (String key : keys) {
            if (raw.contains(key) || lower.contains(key.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
