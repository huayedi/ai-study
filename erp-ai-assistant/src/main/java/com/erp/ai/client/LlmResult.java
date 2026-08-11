package com.erp.ai.client;

public class LlmResult {

    private final String content;
    private final String model;
    private final int promptTokens;
    private final int completionTokens;

    public LlmResult(String content, String model, int promptTokens, int completionTokens) {
        this.content = content;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    public String getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }
}
