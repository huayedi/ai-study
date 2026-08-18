package com.erp.ai.draft.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Day20：加载采购草稿专用 system 提示（与 Chat system 隔离）。
 */
@Component
public class DraftPromptLoader {

    public static final String MARKER = "【草稿辅助·采购订单】";
    private static final String PATH = "prompts/draft-purchase-order-prompt.txt";

    private final String prompt;

    public DraftPromptLoader() {
        try {
            ClassPathResource resource = new ClassPathResource(PATH);
            this.prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("无法加载草稿提示词: " + PATH, e);
        }
    }

    public String load() {
        return prompt;
    }
}
