package com.erp.ai.eval.controller;

import com.erp.ai.eval.dto.EvalRunRequest;
import com.erp.ai.eval.dto.EvalRunResponse;
import com.erp.ai.eval.service.EvalRunnerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 轻量评测 HTTP 入口。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>对外暴露评测运行接口：按 classpath 上的 JSONL 套件批量执行用例</li>
 *   <li>请求体可选；缺省时使用空 {@link EvalRunRequest}（从而采用默认 suite）</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * 属于 {@code com.erp.ai.eval} 表现层。评测会真实调用 rag/chat/draft/tool/security/scenario
 * 等域服务（学习环境通常为 mock LLM），用于回归冒烟而非生产流量。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day25：轻量评测入口。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * 客户端 POST /api/ai/eval/run
 *   → EvalController.run
 *   → EvalRunnerService.run(request)
 *   → 加载 JSONL → 逐条 evaluate → 汇总 EvalRunResponse
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>{@code @RequestBody(required = false)}：允许空 body，避免强制客户端传 JSON</li>
 *   <li>本 Controller 不做套件路径校验；非法 suite 由 Service 抛出 IllegalArgumentException</li>
 * </ul>
 *
 * <p>路径：{@code POST /api/ai/eval/run}</p>
 */
@RestController
@RequestMapping("/api/ai/eval")
public class EvalController {

    /** 评测执行服务：加载 JSONL、分发类型、汇总通过/失败。 */
    private final EvalRunnerService evalRunnerService;

    /**
     * 构造注入评测服务。
     *
     * @param evalRunnerService 评测 runner，由 Spring 注入
     */
    public EvalController(EvalRunnerService evalRunnerService) {
        this.evalRunnerService = evalRunnerService;
    }

    /**
     * 运行指定（或默认）评测套件。
     * <p>
     * 业务含义：一键跑 month1-smoke 等 JSONL，返回每条用例的通过情况。
     * 边界：request 为 null 时用新的空请求（默认 suite）；不在此做 Bean Validation。
     * </p>
     *
     * @param request 可选；主要字段为 classpath 相对 suite 路径
     * @return 套件级汇总与逐条 CaseResult
     */
    @PostMapping("/run")
    public EvalRunResponse run(@RequestBody(required = false) EvalRunRequest request) {
        return evalRunnerService.run(request == null ? new EvalRunRequest() : request);
    }
}
