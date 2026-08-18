package com.erp.ai.draft.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Day20：采购订单草稿辅助入参。
 * <p>
 * 无 session：避免被 Chat 多轮历史带跑。
 */
public class DraftRequest {

    @NotBlank(message = "utterance 不能为空")
    private String utterance;

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }
}
