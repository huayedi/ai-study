package com.erp.ai;

import com.erp.ai.common.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ERP AI 助手启动类（第 1–2 周学习项目入口）。
 * <p>
 * {@code @SpringBootApplication}：开启组件扫描、自动配置。<br>
 * {@code @EnableConfigurationProperties}：启用 {@link AiProperties}，
 * 把 {@code application.yml} 里 {@code ai.*} 配置绑定到 Java 对象。
 */
@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class ErpAiAssistantApplication {

    /**
     * 标准 Spring Boot 启动入口。IDEA 直接运行本方法即可。
     */
    public static void main(String[] args) {
        SpringApplication.run(ErpAiAssistantApplication.class, args);
    }
}
