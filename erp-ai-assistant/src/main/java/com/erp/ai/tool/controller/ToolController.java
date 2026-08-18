package com.erp.ai.tool.controller;

import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.service.ToolExecutor;
import com.erp.ai.tool.ToolHandler;
import com.erp.ai.tool.service.ToolRegistry;
import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.dto.ToolInvokeRequest;
import com.erp.ai.tool.dto.ToolInvokeResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Day16：只读工具手测入口（白名单内查询；无写库存工具）。
 * <p>
 * <b>职责</b>：提供控制台/curl 可调用的 list / invoke，不经过大模型，直接验证 Registry+Executor。
 * <p>
 * <b>只读白名单 / 禁写约束</b>：invoke 一律走 {@link ToolExecutor}，未注册名与禁写名会被拒绝。
 * 本 Controller <strong>不暴露</strong>任何写库存、过账、删单、付款、开关期间的 API——
 * 因为学习环境禁止 AI 改账，写操作须人工在 ERP 完成。
 * <p>
 * <b>Day16 路径</b>：HTTP → 本类 → Executor → Handler → LearningDataRepository。
 * Day18 Chat 不经过本类（走 Orchestrator / tool_calls），但共用同一 Executor。
 * <p>
 * <b>上下游</b>：上游 HTTP 客户端；下游 Registry / Executor。
 * <ul>
 *   <li>{@code GET  /api/ai/tool/list} — 列出已注册工具定义</li>
 *   <li>{@code POST /api/ai/tool/invoke} — 执行白名单工具</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ai/tool")
public class ToolController {

    /** 只读白名单注册表 */
    private final ToolRegistry registry;
    /** 带超时与审计的执行器 */
    private final ToolExecutor executor;

    /**
     * @param registry 工具白名单
     * @param executor 工具执行器
     */
    public ToolController(ToolRegistry registry, ToolExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    /**
     * 列出全部已注册只读工具的 {@link ToolDefinition}。
     *
     * @return 定义列表（顺序与注册顺序一致）
     */
    @GetMapping("/list")
    public List<ToolDefinition> list() {
        return registry.all().stream().map(ToolHandler::definition).toList();
    }

    /**
     * 手动执行白名单工具。校验失败 / 禁写 / 超时均体现在响应 {@code ok=false}。
     * <p>
     * <b>校验</b>：{@link ToolInvokeRequest#getToolName()} 由 Bean Validation 保证非空；
     * 进一步白名单与禁写由 Executor 负责。
     * <p>
     * <b>超时</b>：由 Executor 的 {@code ai.tool.timeout-ms} 控制，超时返回失败而非挂起。
     *
     * @param request 工具名 + 参数 Map
     * @return 含 traceId 的统一响应
     */
    @PostMapping("/invoke")
    public ToolInvokeResponse invoke(@Valid @RequestBody ToolInvokeRequest request) {
        ToolResult result = executor.run(request.getToolName(), request.getArgs());
        return new ToolInvokeResponse(
                newTraceId(),
                result.toolName(),
                result.ok(),
                result.data(),
                result.error(),
                result.latencyMs()
        );
    }

    /**
     * 生成无横杠 UUID 作为本次 HTTP 调用追踪号（非 DB 主键）。
     *
     * @return 32 位 hex 字符串
     */
    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
