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
            "compatible"
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
                "不支持的 ai.provider='" + raw + "'。请使用 mock 或 openai-compatible"
                        + "（也可用别名 openai）。请检查 IDEA 环境变量 AI_PROVIDER / application.yml。"
        );
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
