package com.erp.ai.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 通用基础设施 Bean 配置。
 * <p>
 * 这里集中创建 HTTP 客户端与 JSON 工具，避免各处重复 new。
 */
@Configuration
public class AppConfig {

    /**
     * 调用大模型 HTTP 接口的客户端。
     * 超时时间与 {@link AiProperties#getTimeoutMs()} 保持一致，防止请求长时间挂起。
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, AiProperties properties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    /**
     * Jackson ObjectMapper：负责 Java 对象 ↔ JSON 互转。
     * {@code findAndRegisterModules()} 可自动注册 JavaTime 等扩展模块。
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
