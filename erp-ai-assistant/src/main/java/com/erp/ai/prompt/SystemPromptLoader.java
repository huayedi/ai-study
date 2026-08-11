package com.erp.ai.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class SystemPromptLoader {

    private final String systemPrompt;

    public SystemPromptLoader() {
        this.systemPrompt = load();
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    private static String load() {
        ClassPathResource resource = new ClassPathResource("prompts/erp-system-prompt.txt");
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载系统提示词 prompts/erp-system-prompt.txt", ex);
        }
    }
}
