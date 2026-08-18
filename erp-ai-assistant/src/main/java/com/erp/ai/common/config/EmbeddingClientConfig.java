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
 *
 * <h2>职责</h2>
 * 根据 {@code ai.rag.embedding-provider} 注册唯一 {@link EmbeddingClient} Bean。
 *
 * <h2>为何与 LLM 工厂分开</h2>
 * Chat 可走 DeepSeek（openai-compatible）；Embedding 默认 hash，
 * 因为 DeepSeek 通常不提供 {@code /embeddings}。两套 provider 独立配置，避免绑死。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 读取 {@link AiProperties#getRag()}；hash 维度取 {@code embedding-dimensions}（下限 8）。
 * 未知 provider → 启动期 {@link IllegalStateException}。
 *
 * <h2>学习要点</h2>
 * 索引与查询必须注入同一 EmbeddingClient；切换 provider 后需重建向量索引。
 */
@Configuration
public class EmbeddingClientConfig {

    /** 记录实际 Embedding 实现，便于排查「Chat 通了但向量 404」类问题 */
    private static final Logger log = LoggerFactory.getLogger(EmbeddingClientConfig.class);

    /**
     * 创建 Embedding 实现。
     * <p>
     * <b>分支：</b>
     * <ul>
     *   <li>空 / hash / mock → {@link HashEmbeddingClient}</li>
     *   <li>openai-compatible 及其短别名 → {@link OpenAiCompatibleEmbeddingClient}</li>
     *   <li>其它 → 启动失败并提示保持 hash</li>
     * </ul>
     *
     * @param properties   AI 配置（读 rag 子节点）
     * @param restTemplate 远程调用客户端
     * @param objectMapper JSON 工具
     * @return 非 {@code null} 的 EmbeddingClient
     * @throws IllegalStateException 不支持的 embedding-provider
     */
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
