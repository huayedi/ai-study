package com.erp.ai.controller;

import com.erp.ai.tool.ToolDefinition;
import com.erp.ai.tool.ToolExecutor;
import com.erp.ai.tool.ToolHandler;
import com.erp.ai.tool.ToolRegistry;
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
 * <ul>
 *   <li>{@code GET  /api/ai/tool/list} — 列出已注册工具定义</li>
 *   <li>{@code POST /api/ai/tool/invoke} — 执行白名单工具</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ai/tool")
public class ToolController {

    private final ToolRegistry registry;
    private final ToolExecutor executor;

    public ToolController(ToolRegistry registry, ToolExecutor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @GetMapping("/list")
    public List<ToolDefinition> list() {
        return registry.all().stream().map(ToolHandler::definition).toList();
    }

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

    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
