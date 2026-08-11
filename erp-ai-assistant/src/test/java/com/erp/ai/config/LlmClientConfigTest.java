package com.erp.ai.config;

import com.erp.ai.client.LlmClient;
import com.erp.ai.client.MockLlmClient;
import com.erp.ai.client.OpenAiCompatibleLlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class LlmClientConfigTest {

    @SpringBootTest
    @TestPropertySource(properties = "ai.provider=mock")
    static class MockProvider {
        @Autowired
        LlmClient llmClient;

        @Test
        void createsMockClient() {
            assertInstanceOf(MockLlmClient.class, llmClient);
        }
    }

    @SpringBootTest
    @TestPropertySource(properties = {
            "ai.provider=deepseek",
            "ai.api-key=test-key",
            "ai.base-url=https://api.deepseek.com",
            "ai.model=deepseek-chat"
    })
    static class DeepSeekAliasProvider {
        @Autowired
        LlmClient llmClient;

        @Test
        void acceptsDeepSeekAlias() {
            assertInstanceOf(OpenAiCompatibleLlmClient.class, llmClient);
        }
    }
}
