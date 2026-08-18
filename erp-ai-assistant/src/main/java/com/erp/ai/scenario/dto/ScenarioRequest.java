package com.erp.ai.scenario.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 场景路由请求体。
 *
 * <h2>职责</h2>
 * <p>携带用户话术与动作类型（classify / assist），供场景模式库入口使用。</p>
 *
 * <h2>在系统中的位置</h2>
 * <p>{@code scenario.dto} 入站 DTO；用于 {@code POST /api/ai/scenario/route}，
 * 评测 {@code EvalRunnerService} 也会构造本类做 classify。</p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day27：场景模式库入参。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * HTTP JSON / Eval 构造 → ScenarioRequest → ScenarioService.handle
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>{@code utterance} 必填（{@link NotBlank}）</li>
 *   <li>{@code action} 默认 {@code classify}；仅值为 assist（忽略大小写与首尾空白后）时才转发下游</li>
 * </ul>
 */
public class ScenarioRequest {

    /**
     * 待分类 / 待编排的用户话术。
     * <p>不能为空；内容决定 ScenarioClassifier 的 Mode。</p>
     */
    @NotBlank(message = "utterance 不能为空")
    private String utterance;

    /**
     * 动作类型：classify=只分类；assist=分类后转发到对应能力（学习编排）。
     * <p>字段默认值为 {@code classify}；null 时 Service 侧仍按 classify 处理。</p>
     */
    private String action = "classify";

    /**
     * 获取用户话术。
     *
     * @return utterance；校验前可能为 null
     */
    public String getUtterance() {
        return utterance;
    }

    /**
     * 设置用户话术。
     *
     * @param utterance 自然语言输入
     */
    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    /**
     * 获取动作类型。
     *
     * @return classify / assist 等；可能为 null 或带空白
     */
    public String getAction() {
        return action;
    }

    /**
     * 设置动作类型。
     *
     * @param action 期望 {@code classify} 或 {@code assist}；其他值在 Service 中等同于只分类
     */
    public void setAction(String action) {
        this.action = action;
    }
}
