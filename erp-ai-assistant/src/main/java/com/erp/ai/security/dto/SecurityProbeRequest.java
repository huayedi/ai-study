package com.erp.ai.security.dto;

import jakarta.validation.constraints.NotBlank;

public class SecurityProbeRequest {

    @NotBlank(message = "utterance 不能为空")
    private String utterance;

    public String getUtterance() {
        return utterance;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }
}
