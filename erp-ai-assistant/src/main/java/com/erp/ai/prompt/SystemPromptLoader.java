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
 * 启动时组装：
 * <ol>
 *   <li>{@code prompts/erp-system-prompt.txt}（角色/规则/术语/输出格式）</li>
 *   <li>{@code prompts/erp-few-shot.txt}（少样本示范，可选文件；存在则追加）</li>
 * </ol>
 * 学习阶段改提示词后需要重启应用才会生效。
 */
@Component
public class SystemPromptLoader {

    private static final String SYSTEM_PROMPT_PATH = "prompts/erp-system-prompt.txt";
    private static final String FEW_SHOT_PATH = "prompts/erp-few-shot.txt";

    private final String systemPrompt;

    public SystemPromptLoader() {
        this.systemPrompt = load();
    }

    /** @return 作为 role=system 发送给模型的完整提示词 */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    private static String load() {
        String base = readRequired(SYSTEM_PROMPT_PATH);
        String fewShot = readOptional(FEW_SHOT_PATH);
        if (fewShot == null || fewShot.isBlank()) {
            return base;
        }
        return base + "\n\n" + fewShot.trim();
    }

    private static String readRequired(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载提示词 " + classpathLocation, ex);
        }
    }

    private static String readOptional(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载提示词 " + classpathLocation, ex);
        }
    }
}
