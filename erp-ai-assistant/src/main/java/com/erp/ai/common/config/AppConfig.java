package com.erp.ai.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 通用基础设施 Bean 配置。
 *
 * <h2>职责</h2>
 * 集中创建跨域复用的 HTTP 客户端与 JSON 工具，避免各处 {@code new} 导致超时/模块不一致。
 *
 * <h2>为何抽象到独立 {@code @Configuration}</h2>
 * 基础设施与业务 Client 工厂解耦：改超时只动这里；LLM/Embedding 工厂只消费 Bean。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * <ul>
 *   <li>依赖 {@link AiProperties#getTimeoutMs()} 统一连接与读超时</li>
 *   <li>{@link RestTemplate} 被 OpenAI 兼容 LLM/Embedding 客户端注入</li>
 *   <li>{@link ObjectMapper} 被 Mock、解析器、各服务共享</li>
 * </ul>
 *
 * <h2>学习要点</h2>
 * RestTemplate 是同步阻塞客户端；超时必须配置，否则线程可能长时间挂起。
 */
@Configuration
public class AppConfig {

    /**
     * 调用大模型 / Embedding HTTP 接口的客户端。
     * <p>
     * 连接超时与读取超时均取自 {@link AiProperties#getTimeoutMs()}，防止请求长时间挂起。
     *
     * @param builder    Spring Boot 提供的 RestTemplateBuilder
     * @param properties AI 配置（读取 timeoutMs）
     * @return 已设置超时的 RestTemplate 单例 Bean
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, AiProperties properties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    /**
     * Jackson {@link ObjectMapper}：负责 Java 对象 ↔ JSON 互转。
     * <p>
     * {@code findAndRegisterModules()} 可自动注册 JavaTime 等扩展模块，
     * 避免 {@link java.time.LocalDateTime} 等类型序列化失败。
     *
     * @return 已注册模块的 ObjectMapper Bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
