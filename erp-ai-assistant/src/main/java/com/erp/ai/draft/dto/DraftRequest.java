package com.erp.ai.draft.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Day20：采购订单草稿辅助入参 DTO（draft 域）。
 * <p>
 * <b>职责</b>：承载 {@code POST /api/ai/draft/purchase-order} 的请求体；
 * 仅含自然语言 {@link #utterance}，不做会话续聊。
 * <p>
 * <b>在系统中的位置</b>：由 {@link com.erp.ai.draft.controller.DraftController} 接收，
 * 交 {@link com.erp.ai.draft.service.DraftService} 消费。
 * <p>
 * <b>对应学习 Day</b>：Day20。
 * <p>
 * <b>调用链 / 上下游</b>：HTTP JSON → 本类 → {@code DraftService.draftPurchaseOrder} →
 * 作为单轮 user 消息发给草稿专用 system 提示词。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li><b>无 session</b>：刻意不提供 sessionId，避免被 Chat 多轮历史带跑</li>
 *   <li><b>不写业务库</b>：入参只驱动建议生成，不触发单据持久化</li>
 *   <li>{@code utterance} 必填（{@code @NotBlank}）</li>
 * </ul>
 *
 * @see DraftResponse
 * @see com.erp.ai.draft.controller.DraftController
 */
public class DraftRequest {

    /**
     * 用户自然语言采购描述，必填。
     * <p>
     * 示例含义：「向华联采购 100 件 A001 到主仓，含税单价 12，下周一交货」。
     * 校验失败消息：{@code utterance 不能为空}。
     * 下游会 trim；空串在校验层即被拦截。
     */
    @NotBlank(message = "utterance 不能为空")
    private String utterance;

    /**
     * 获取用户自然语言输入。
     *
     * @return 原始 utterance；校验通过后不为空白
     */
    public String getUtterance() {
        return utterance;
    }

    /**
     * 设置用户自然语言输入。
     *
     * @param utterance 采购意图描述；调用方应保证非空
     */
    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }
}
