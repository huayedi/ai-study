package com.erp.ai;

import com.erp.ai.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class ErpAiAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpAiAssistantApplication.class, args);
    }
}
