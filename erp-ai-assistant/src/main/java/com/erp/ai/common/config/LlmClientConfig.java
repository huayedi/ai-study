package com.erp.ai.common.config;

import com.erp.ai.common.client.LlmClient;
import com.erp.ai.common.client.MockLlmClient;
import com.erp.ai.common.client.OpenAiCompatibleLlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Set;

/**
 * LLM 客户端工厂。
 * <p>
 * 设计要点：
 * <ol>
 *   <li>整个应用只注册一个 {@link LlmClient} Bean，供 {@code ChatService} 注入</li>
 *   <li>{@code ai.provider} 决定用 mock 还是真实 OpenAI 兼容客户端</li>
 *   <li>厂商名（deepseek/qwen…）只是别名，协议仍是 openai-compatible</li>
 * </ol>
 * 学习提示：如果你把 provider 写成厂商展示名且不在别名列表里，启动会失败并给出明确错误。
 */
@Configuration
public class LlmClientConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

    /**
     * 一律映射到 {@link OpenAiCompatibleLlmClient}。
     * 真正区分厂商的是 base-url 与 model，不是再写一套 Client。
     */
    private static final Set<String> OPENAI_ALIASES = Set.of(
            "openai",
            "openai-compatible",
            "openai_compatible",
            "compatible",
            // 以下是常见厂商名别名（大小写不敏感，见 normalize）
            "deepseek",
            "qwen",
            "dashscope",
            "moonshot",
            "zhipu",
            "glm"
    );

    /**
     * 根据配置创建唯一的 {@link LlmClient}。
     *
     * @param properties   ai.* 配置
     * @param objectMapper JSON 工具（mock 组装返回值时使用）
     * @param restTemplate 真实 HTTP 调用客户端
     */
    @Bean
    public LlmClient llmClient(AiProperties properties,
                               ObjectMapper objectMapper,
                               RestTemplate restTemplate) {
        String raw = properties.getProvider();
        String provider = normalize(raw);

        // 未配置或显式 mock：本地可跑通，无需 Key
        if (!StringUtils.hasText(provider) || "mock".equals(provider)) {
            log.info("AI provider=mock (raw='{}')", raw);
            return new MockLlmClient(objectMapper);
        }

        // DeepSeek / OpenAI / 通义等：同一套兼容协议
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

    /**
     * 规范化 provider 字符串：去空格、小写、下划线转横杠。
     * 这样 {@code DeepSeek}、{@code deep_seek}、{@code deepseek} 都能识别。
     */
    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
