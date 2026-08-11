package com.erp.ai.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 系统提示词加载器。
 * <p>
 * 启动时从 classpath {@code prompts/erp-system-prompt.txt} 读取一次并缓存。
 * 学习阶段改提示词后需要重启应用才会生效（后续可做成可热更新的 Prompt 版本管理）。
 */
@Component
public class SystemPromptLoader {

    /** 缓存的系统提示词全文 */
    private final String systemPrompt;

    public SystemPromptLoader() {
        this.systemPrompt = load();
    }

    /** @return 作为 role=system 发送给模型的提示词 */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * 从资源文件加载提示词；文件缺失时直接让应用启动失败，避免带着空约束上线。
     */
    private static String load() {
        ClassPathResource resource = new ClassPathResource("prompts/erp-system-prompt.txt");
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载系统提示词 prompts/erp-system-prompt.txt", ex);
        }
    }
}
