package com.erp.ai.tool.service;

import com.erp.ai.tool.ToolHandler;
import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.entity.ToolCallAudit;
import com.erp.ai.tool.mapper.ToolCallAuditMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 工具执行器 — 白名单 → 禁写硬拦 → 校验 → 超时 → 审计（日志 + MyBatis XML）。
 */
public class ToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);

    private static final Set<String> FORBIDDEN_NAMES = Set.of(
            "writeinventory",
            "updateinventory",
            "postdocument",
            "deleteorder",
            "payinvoice",
            "closeperiod",
            "openperiod",
            "adjuststock"
    );

    private final ToolRegistry registry;
    private final long timeoutMs;
    private final ToolCallAuditMapper toolCallAuditMapper;
    private final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "tool-exec");
        t.setDaemon(true);
        return t;
    });

    public ToolExecutor(ToolRegistry registry, long timeoutMs) {
        this(registry, timeoutMs, null);
    }

    public ToolExecutor(ToolRegistry registry, long timeoutMs, ToolCallAuditMapper toolCallAuditMapper) {
        this.registry = registry;
        this.timeoutMs = Math.max(1L, timeoutMs);
        this.toolCallAuditMapper = toolCallAuditMapper;
    }

    public ToolResult run(String name, Map<String, Object> args) {
        long t0 = System.currentTimeMillis();
        Map<String, Object> safeArgs = args == null ? Map.of() : args;
        try {
            rejectForbidden(name);
            ToolHandler handler = registry.require(name);
            handler.definition();
            Object data = callWithTimeout(() -> handler.execute(safeArgs), timeoutMs);
            long cost = System.currentTimeMillis() - t0;
            audit(name, safeArgs, true, cost, null);
            return ToolResult.ok(name, data, cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - t0;
            String msg = rootMessage(e);
            audit(name, safeArgs, false, cost, msg);
            return ToolResult.fail(name == null ? "" : name, msg, cost);
        }
    }

    private static void rejectForbidden(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tool not allowed: (blank)");
        }
        String key = name.trim().toLowerCase(Locale.ROOT).replace("_", "");
        if (FORBIDDEN_NAMES.contains(key) || (key.contains("write") && key.contains("inventor"))) {
            throw new IllegalArgumentException("tool not allowed (forbidden): " + name);
        }
    }

    private Object callWithTimeout(Callable<Object> task, long timeoutMs) throws Exception {
        Future<Object> future = pool.submit(task);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new IllegalStateException("tool timeout after " + timeoutMs + "ms");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw new IllegalStateException(cause);
        }
    }

    private void audit(String name, Map<String, Object> args, boolean ok, long latencyMs, String error) {
        String argSummary = args.keySet().toString();
        if (ok) {
            log.info("tool audit name={} ok=true latencyMs={} argsKeys={}", name, latencyMs, argSummary);
        } else {
            log.warn("tool audit name={} ok=false latencyMs={} argsKeys={} error={}",
                    name, latencyMs, argSummary, error);
        }
        if (toolCallAuditMapper != null) {
            try {
                ToolCallAudit row = new ToolCallAudit();
                row.setToolName(name);
                row.setOk(ok ? 1 : 0);
                row.setArgsKeys(argSummary == null ? null : (argSummary.length() > 250 ? argSummary.substring(0, 250) : argSummary));
                row.setErrorMessage(error == null ? null : (error.length() > 500 ? error.substring(0, 500) : error));
                row.setLatencyMs(latencyMs);
                toolCallAuditMapper.insertAudit(row);
            } catch (Exception e) {
                log.warn("tool_call_audit insert failed: {}", e.getMessage());
            }
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable cur = e;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String m = cur.getMessage();
        return m == null || m.isBlank() ? cur.getClass().getSimpleName() : m;
    }
}
