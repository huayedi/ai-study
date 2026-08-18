package com.erp.ai.common.config;

import com.erp.ai.common.client.EmbeddingClient;
import com.erp.ai.common.client.HashEmbeddingClient;
import com.erp.ai.common.client.OpenAiCompatibleEmbeddingClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

/**
 * Embedding 客户端工厂（Day11）。
 * <p>
 * Chat 可走 DeepSeek（openai-compatible）；Embedding 默认 hash，
 * 因为 DeepSeek 通常不提供 /embeddings。
 */
@Configuration
public class EmbeddingClientConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingClientConfig.class);

    @Bean
    public EmbeddingClient embeddingClient(AiProperties properties,
                                           RestTemplate restTemplate,
                                           ObjectMapper objectMapper) {
        String raw = properties.getRag().getEmbeddingProvider();
        String provider = raw == null ? "hash" : raw.trim().toLowerCase(Locale.ROOT);

        if (!StringUtils.hasText(provider) || "hash".equals(provider) || "mock".equals(provider)) {
            int dims = Math.max(8, properties.getRag().getEmbeddingDimensions());
            log.info("Embedding provider=hash (dims={})", dims);
            return new HashEmbeddingClient(dims);
        }

        if ("openai-compatible".equals(provider)
                || "openai".equals(provider)
                || "compatible".equals(provider)) {
            log.info("Embedding provider=openai-compatible (model={}, baseUrl={})",
                    properties.getRag().getEmbeddingModel(),
                    StringUtils.hasText(properties.getRag().getEmbeddingBaseUrl())
                            ? properties.getRag().getEmbeddingBaseUrl()
                            : properties.getBaseUrl());
            return new OpenAiCompatibleEmbeddingClient(properties, restTemplate, objectMapper);
        }

        throw new IllegalStateException(
                "不支持的 ai.rag.embedding-provider='" + raw + "'。请使用 hash 或 openai-compatible。"
                        + " DeepSeek Chat 可继续用；无 embeddings 时请保持 hash。"
        );
    }
}
