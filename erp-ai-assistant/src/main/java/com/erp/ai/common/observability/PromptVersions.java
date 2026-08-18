package com.erp.ai.common.observability;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Day22：提示词版本号（内容 hash），便于改 Prompt 后对比效果。
 */
public final class PromptVersions {

    private PromptVersions() {
    }

    public static String of(String name, String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
            return name + "@" + HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (Exception e) {
            return name + "@unknown";
        }
    }
}
