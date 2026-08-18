package com.erp.ai.scenario.dto;

import jakarta.validation.constraints.NotBlank;

public class ScenarioRequest {

    @NotBlank(message = "utterance 不能为空")
    private String utterance;

    /** classify=只分类；assist=分类后转发到对应能力（学习编排） */
    private String action = "classify";

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
