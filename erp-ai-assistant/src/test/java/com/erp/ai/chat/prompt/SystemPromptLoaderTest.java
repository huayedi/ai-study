package com.erp.ai.chat.prompt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemPromptLoaderTest {

    @Test
    void loadsSystemPromptWithFewShot() {
        SystemPromptLoader loader = new SystemPromptLoader();
        String prompt = loader.getSystemPrompt();
        assertTrue(prompt.contains("术语摘要"));
        assertTrue(prompt.contains("少样本示范") || prompt.contains("few-shot") || prompt.contains("示范 1"));
        assertTrue(prompt.contains("物资请购") || prompt.contains("存货编码"));
    }
}
