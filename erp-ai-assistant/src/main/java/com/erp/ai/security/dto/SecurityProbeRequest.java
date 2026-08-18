package com.erp.ai.security.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 安全抽测请求体。
 *
 * <h2>职责</h2>
 * <p>承载待检测的用户话术 {@code utterance}，供 {@code SecurityController#probe} 使用。</p>
 *
 * <h2>在系统中的位置</h2>
 * <p>{@code security.dto} 入站 DTO；仅用于 {@code POST /api/ai/security/probe}。</p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day23：安全抽测入参。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * HTTP JSON → SecurityProbeRequest → SecurityController.probe → InjectionGuard.inspect(utterance)
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>{@code utterance} 必须非空（{@link NotBlank}），校验失败由全局校验机制处理</li>
 *   <li>本类不含其他字段；不携带会话 ID 或用户身份</li>
 * </ul>
 */
public class SecurityProbeRequest {

    /**
     * 待安全检查的用户原始话术。
     * <p>不能为 null/空白；典型内容为中英文注入试探句或正常业务问句。</p>
     */
    @NotBlank(message = "utterance 不能为空")
    private String utterance;

    /**
     * 获取待检测话术。
     *
     * @return 当前 utterance；可能在校验前为 null
     */
    public String getUtterance() {
        return utterance;
    }

    /**
     * 设置待检测话术。
     *
     * @param utterance 用户输入文本；业务上应由调用方保证非空
     */
    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }
}
