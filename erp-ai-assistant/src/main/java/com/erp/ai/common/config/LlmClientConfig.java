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
 * LLM 客户端工厂配置类。
 *
 * <h2>职责</h2>
 * 根据 {@code ai.provider} 创建并注册<strong>唯一</strong>的 {@link LlmClient} Bean。
 *
 * <h2>设计要点</h2>
 * <ol>
 *   <li>整个应用只注册一个 {@link LlmClient}，供 ChatService 等注入</li>
 *   <li>{@code ai.provider} 决定用 mock 还是真实 OpenAI 兼容客户端</li>
 *   <li>厂商名（deepseek/qwen…）只是别名，协议仍是 openai-compatible</li>
 * </ol>
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 读取 {@link AiProperties}；依赖 {@link ObjectMapper} 与 {@link RestTemplate}（见 {@link AppConfig}）。
 * 未知 provider 时在<strong>启动期</strong>抛 {@link IllegalStateException}，快速失败。
 *
 * <h2>学习要点</h2>
 * 若把 provider 写成厂商展示名且不在别名列表里，启动会失败并给出明确错误——这是刻意的。
 */
@Configuration
public class LlmClientConfig {

    /** 启动期记录实际选用的 provider，便于核对 YAML / 环境变量是否生效 */
    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

    /**
     * 一律映射到 {@link OpenAiCompatibleLlmClient} 的别名集合。
     * <p>
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
     * <p>
     * <b>分支：</b>
     * <ol>
     *   <li>空 / mock → {@link MockLlmClient}</li>
     *   <li>别名集合内 → {@link OpenAiCompatibleLlmClient}</li>
     *   <li>其它 → 启动失败</li>
     * </ol>
     *
     * @param properties   ai.* 配置
     * @param objectMapper JSON 工具（mock 组装返回值时使用）
     * @param restTemplate 真实 HTTP 调用客户端
     * @return 非 {@code null} 的 LlmClient 实现
     * @throws IllegalStateException 不支持的 provider 时
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
     * <p>
     * 这样 {@code DeepSeek}、{@code deep_seek}、{@code deepseek} 都能识别。
     *
     * @param raw 原始配置值；可为 {@code null}
     * @return 规范化串；{@code null} 时返回空串
     */
    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
