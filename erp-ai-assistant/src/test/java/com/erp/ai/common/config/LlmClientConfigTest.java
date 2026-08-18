package com.erp.ai.common.config;

import com.erp.ai.common.client.LlmClient;
import com.erp.ai.common.client.MockLlmClient;
import com.erp.ai.common.client.OpenAiCompatibleLlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * 验证 {@link LlmClientConfig} 能按 provider 选出正确实现。
 * <p>
 * 使用静态内部类 + 独立 {@code @SpringBootTest}，以便为每个场景注入不同配置。
 */
class LlmClientConfigTest {

    /** provider=mock → MockLlmClient */
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

    /** provider=deepseek（厂商别名）→ OpenAiCompatibleLlmClient */
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
