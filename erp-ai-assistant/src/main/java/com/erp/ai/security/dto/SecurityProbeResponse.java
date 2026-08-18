package com.erp.ai.security.dto;

import java.util.List;

/**
 * 安全抽测结果响应体（学习接口，不接公司防火墙）。
 *
 * <h2>职责</h2>
 * <p>
 * 向客户端返回一次 {@code InjectionGuard} 检查的可观测结果：是否拦截、类别、原因、命中标签，
 * 以及固定的分层防线提示。
 * </p>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * {@code security.dto} 出站 DTO。由 {@code SecurityController} 组装；
 * {@code ScenarioService} 在 SECURITY_REFUSE 的 assist 分支也会构造本类型作为 payload。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day23：安全抽测出参与教学用 defenseHint。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * InjectionGuard.Verdict → SecurityController / ScenarioService 填充本对象 → JSON 响应
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>{@code matched} 在 setter 中做防御性拷贝，null 视为空列表</li>
 *   <li>{@code category} 存枚举名字符串，而非枚举类型，便于跨语言客户端消费</li>
 * </ul>
 */
public class SecurityProbeResponse {

    /** 本次请求追踪 ID（无连字符 UUID），用于日志与排障关联。 */
    private String traceId;

    /** 是否判定应拦截；true 表示命中至少一条注入/越权规则。 */
    private boolean blocked;

    /** 首个命中类别的枚举名（如 IGNORE_RULES）；未命中多为 NONE。 */
    private String category;

    /** 面向用户的拒答/说明文案；未拦截时通常为 null。 */
    private String reason;

    /** 所有命中规则的可读标签列表；默认空不可变列表。 */
    private List<String> matched = List.of();

    /** 分层防线教学提示文案（由 Controller/Service 写入固定字符串）。 */
    private String defenseHint;

    /**
     * 获取追踪 ID。
     *
     * @return traceId，可能为 null（未设置时）
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置追踪 ID。
     *
     * @param traceId 无连字符 UUID 字符串
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 是否已拦截。
     *
     * @return true 表示应拒答/阻断该话术
     */
    public boolean isBlocked() {
        return blocked;
    }

    /**
     * 设置拦截标志。
     *
     * @param blocked 是否拦截
     */
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    /**
     * 获取命中类别名。
     *
     * @return 类别枚举的 {@code name()} 字符串
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置命中类别名。
     *
     * @param category 如 IGNORE_RULES、NONE 等
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取拒答原因文案。
     *
     * @return 原因字符串；未拦截时可能为 null
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置拒答原因文案。
     *
     * @param reason 通常来自 {@code InjectionGuard.refusalMessage}
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取命中标签列表。
     *
     * @return 标签列表；默认空列表，不为 null（经 setter 规范化后）
     */
    public List<String> getMatched() {
        return matched;
    }

    /**
     * 设置命中标签列表。
     * <p>null 入参会被规范为空列表；非 null 则做不可变拷贝，避免外部修改内部状态。</p>
     *
     * @param matched 规则标签集合；可为 null
     */
    public void setMatched(List<String> matched) {
        this.matched = matched == null ? List.of() : List.copyOf(matched);
    }

    /**
     * 获取分层防线提示。
     *
     * @return 教学用提示字符串
     */
    public String getDefenseHint() {
        return defenseHint;
    }

    /**
     * 设置分层防线提示。
     *
     * @param defenseHint 固定或场景定制的防御说明
     */
    public void setDefenseHint(String defenseHint) {
        this.defenseHint = defenseHint;
    }
}
