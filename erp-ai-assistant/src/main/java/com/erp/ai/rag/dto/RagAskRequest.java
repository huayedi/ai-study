package com.erp.ai.rag.dto;

import jakarta.validation.constraints.NotBlank;

public class RagAskRequest {

    @NotBlank(message = "question 不能为空")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
