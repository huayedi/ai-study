package com.erp.ai.scenario.dto;

/**
 * 场景分类 / 轻量编排响应体。
 *
 * <h2>职责</h2>
 * <p>
 * 返回一次场景路由的结果：追踪 ID、判定模式、理由、置信度、教学用 routedApi，
 * 以及 assist 时下游返回的 {@code payload}。
 * </p>
 *
 * <h2>在系统中的位置</h2>
 * <p>{@code scenario.dto} 出站 DTO；由 {@code ScenarioService#handle} 填充。</p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day27：场景分类 / 轻量编排响应。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * ScenarioService.handle → 填充本对象 → ScenarioController / EvalRunnerService
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>{@code mode} 存枚举名字符串（如 RAG_MANUAL）</li>
 *   <li>{@code payload} 类型为 Object，实际可能是 RagAskResponse、ChatResponse、Map 等</li>
 *   <li>classify 模式下 payload 通常为 null</li>
 * </ul>
 */
public class ScenarioResponse {

    /** 本次场景请求追踪 ID（无连字符 UUID）。 */
    private String traceId;

    /** 分类得到的模式名，对应 {@code ScenarioClassifier.Mode#name()}。 */
    private String mode;

    /** 分类理由的中文短句（来自 Classification.reason）。 */
    private String reason;

    /** 规则启发式置信度（0~1）。 */
    private double confidence;

    /** 教学映射：该模式应对应的 HTTP API 或 inline 标识。 */
    private String routedApi;

    /**
     * assist 时下游能力的返回体；classify 时一般为 null。
     * <p>序列化为 JSON 时由 Jackson 按实际运行时类型写出。</p>
     */
    private Object payload;

    /**
     * 获取追踪 ID。
     *
     * @return traceId
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置追踪 ID。
     *
     * @param traceId 无连字符 UUID
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 获取场景模式名。
     *
     * @return 如 CHAT_GENERAL、TOOL_READONLY
     */
    public String getMode() {
        return mode;
    }

    /**
     * 设置场景模式名。
     *
     * @param mode Mode 枚举名
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 获取分类理由。
     *
     * @return 中文 reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置分类理由。
     *
     * @param reason 人类可读说明
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * 获取置信度。
     *
     * @return 0~1 的 double
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * 设置置信度。
     *
     * @param confidence 规则给出的启发式分数
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    /**
     * 获取映射的 API 路径说明。
     *
     * @return 如 {@code POST /api/ai/chat} 或 {@code inline:approval-summary}
     */
    public String getRoutedApi() {
        return routedApi;
    }

    /**
     * 设置映射的 API 路径说明。
     *
     * @param routedApi 教学用路由字符串
     */
    public void setRoutedApi(String routedApi) {
        this.routedApi = routedApi;
    }

    /**
     * 获取 assist 载荷。
     *
     * @return 下游对象或 null
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * 设置 assist 载荷。
     *
     * @param payload 下游响应；可为 null
     */
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
