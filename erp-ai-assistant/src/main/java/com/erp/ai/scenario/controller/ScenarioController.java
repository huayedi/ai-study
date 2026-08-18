package com.erp.ai.scenario.controller;

import com.erp.ai.scenario.dto.ScenarioRequest;
import com.erp.ai.scenario.dto.ScenarioResponse;
import com.erp.ai.scenario.service.ScenarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 场景模式库 HTTP 入口。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>对外暴露场景路由接口：对用户话术做模式分类，可选地转发到 chat/rag/draft/tool/security</li>
 *   <li>协议层只做入参校验与委托，编排逻辑在 {@link ScenarioService}</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * 属于 {@code com.erp.ai.scenario} 表现层。是 Day27「场景模式库」的对外门面，
 * 内部依赖分类器与各域 Service（chat/rag/draft/tool/security）。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day27：场景模式库 — classify 或 assist。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * 客户端 POST /api/ai/scenario/route
 *   → ScenarioController.route
 *   → ScenarioService.handle(request)
 *   → ScenarioClassifier.classify +（assist 时）dispatch 到下游能力
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>默认 action=classify 只返回模式信息；assist 才真正调用下游并填充 payload</li>
 *   <li>Controller 不解析业务模式，不直接依赖 InjectionGuard / RagService 等</li>
 * </ul>
 *
 * <p>路径：{@code POST /api/ai/scenario/route} — classify 或 assist</p>
 */
@RestController
@RequestMapping("/api/ai/scenario")
public class ScenarioController {

    /** 场景编排服务：分类 + 按模式可选转发。 */
    private final ScenarioService scenarioService;

    /**
     * 构造注入场景服务。
     *
     * @param scenarioService 场景编排实现，由 Spring 注入
     */
    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    /**
     * 场景路由：分类，并按 action 决定是否 assist。
     * <p>
     * 业务含义：客户端提交 utterance 与可选 action；返回 mode、confidence、routedApi，
     * 以及 assist 时的下游 payload。
     * 边界：utterance 经 {@code @Valid} 非空校验；具体 action 语义由 Service 解释。
     * </p>
     *
     * @param request 含 utterance、action 的请求体
     * @return 分类结果及可选编排载荷
     */
    @PostMapping("/route")
    public ScenarioResponse route(@Valid @RequestBody ScenarioRequest request) {
        return scenarioService.handle(request);
    }
}
