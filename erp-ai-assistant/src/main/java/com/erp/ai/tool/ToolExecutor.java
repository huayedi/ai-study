package com.erp.ai.tool;

import com.erp.ai.store.JdbcToolCallAuditRepository;
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
 * Day16：工具执行器 — 白名单 → 禁写硬拦 → 参数校验 → 超时 → 审计（日志 + MySQL）。
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
    private final JdbcToolCallAuditRepository auditRepository;
    private final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "tool-exec");
        t.setDaemon(true);
        return t;
    });

    public ToolExecutor(ToolRegistry registry, long timeoutMs) {
        this(registry, timeoutMs, null);
    }

    public ToolExecutor(ToolRegistry registry, long timeoutMs, JdbcToolCallAuditRepository auditRepository) {
        this.registry = registry;
        this.timeoutMs = Math.max(1L, timeoutMs);
        this.auditRepository = auditRepository;
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
        if (auditRepository != null) {
            try {
                auditRepository.insert(name, ok, argSummary, error, latencyMs);
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
