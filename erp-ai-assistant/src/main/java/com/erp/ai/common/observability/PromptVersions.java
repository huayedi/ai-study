package com.erp.ai.common.observability;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Day22：提示词版本号工具（内容 hash）。
 *
 * <h2>职责</h2>
 * 根据提示词正文计算短 SHA-256 前缀，生成形如 {@code name@deadbeef} 的版本串，
 * 便于改 Prompt 后对比效果与审计。
 *
 * <h2>为何用内容 hash 而不是手写版本号</h2>
 * 避免「改了文件却忘了 bump 版本」；内容变则版本变。
 *
 * <h2>与 Spring 的关系</h2>
 * 纯静态工具，无 Bean；由 {@link AiStatsService} 等直接调用。
 *
 * <h2>学习要点</h2>
 * 仅取 hash 前 8 位十六进制，碰撞概率对学习项目可接受；不是安全签名场景。
 */
public final class PromptVersions {

    /**
     * 禁止实例化：工具类。
     */
    private PromptVersions() {
    }

    /**
     * 计算提示词版本标识。
     * <p>
     * 算法：UTF-8 字节 → SHA-256 → 取 hex 前 8 位 → {@code name@xxxxxxxx}。
     * 若 JRE 异常缺失算法（极端），回退 {@code name@unknown}，不抛到调用方。
     *
     * @param name    逻辑名称，如 {@code erp-system}
     * @param content 提示词全文；{@code null} 视为空串
     * @return 版本字符串；永不为 {@code null}
     */
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
