package com.erp.ai.config;

import com.erp.ai.client.LlmClient;
import com.erp.ai.client.MockLlmClient;
import com.erp.ai.client.OpenAiCompatibleLlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Set;

@Configuration
public class LlmClientConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);
    private static final Set<String> OPENAI_ALIASES = Set.of(
            "openai",
            "openai-compatible",
            "openai_compatible",
            "compatible",
            // 这些都是“厂商名”，实际协议仍是 OpenAI Compatible
            "deepseek",
            "qwen",
            "dashscope",
            "moonshot",
            "zhipu",
            "glm"
    );

    @Bean
    public LlmClient llmClient(AiProperties properties,
                               ObjectMapper objectMapper,
                               RestTemplate restTemplate) {
        String raw = properties.getProvider();
        String provider = normalize(raw);

        if (!StringUtils.hasText(provider) || "mock".equals(provider)) {
            log.info("AI provider=mock (raw='{}')", raw);
            return new MockLlmClient(objectMapper);
        }

        if (OPENAI_ALIASES.contains(provider)) {
            log.info("AI provider=openai-compatible (raw='{}', baseUrl={}, model={})",
                    raw, properties.getBaseUrl(), properties.getModel());
            return new OpenAiCompatibleLlmClient(properties, restTemplate, objectMapper);
        }

        throw new IllegalStateException(
                "不支持的 ai.provider='" + raw + "'。请使用 mock 或 openai-compatible。"
                        + " DeepSeek/通义等厂商请把 provider 设为 openai-compatible（或别名 deepseek），"
                        + "真正区分厂商的是 base-url 与 model，不是另写一套 provider。"
        );
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
