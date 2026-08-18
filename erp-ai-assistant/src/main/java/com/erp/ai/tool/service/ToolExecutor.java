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
 * <p>
 * <b>职责</b>：所有 Day16 HTTP 与 Day18 Chat 工具调用的唯一执行入口，保证：
 * <ol>
 *   <li>禁写名在进 Registry 前即拒绝；</li>
 *   <li>仅已注册只读 Handler 可执行；</li>
 *   <li>整次 execute 受 {@code timeoutMs} 墙钟超时保护；</li>
 *   <li>成败写入日志与 {@code tool_call_audit} 表。</li>
 * </ol>
 * <p>
 * <b>为何没有写工具</b>：学习助手只回答查询，不改 ERP 账；若开放 writeInventory 等，
 * 模型幻觉或手测误触会导致脏数据与责任不清。禁写集合 + 仅注册三只读工具双保险。
 * <p>
 * <b>Day16 / Day18</b>：Day16 {@code ToolController}；Day18 路径 A tool_calls 与路径 B Orchestrator 均调用 {@link #run}。
 * <p>
 * <b>上下游</b>：上游 Controller/Orchestrator；下游 Registry → Handler → LearningDataRepository；旁路 AuditMapper。
 */
public class ToolExecutor {

    /** 审计与超时日志 */
    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);

    /**
     * 显式禁写工具名集合（小写、去下划线后匹配）。
     * 即使未来误注册同名 Handler，也会在此被硬拦。
     */
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

    /** 只读白名单注册表 */
    private final ToolRegistry registry;
    /** 单次工具执行超时毫秒（构造时至少钳为 1） */
    private final long timeoutMs;
    /** 审计落库 Mapper；单测可为 null（仅打日志） */
    private final ToolCallAuditMapper toolCallAuditMapper;
    /** 守护线程池：把 Handler.execute 放到可取消 Future 上以支持超时 */
    private final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "tool-exec");
        t.setDaemon(true);
        return t;
    });

    /**
     * 无审计 Mapper 的构造（单测便捷入口）。
     *
     * @param registry  白名单
     * @param timeoutMs 超时毫秒（小于 1 时升为 1）
     */
    public ToolExecutor(ToolRegistry registry, long timeoutMs) {
        this(registry, timeoutMs, null);
    }

    /**
     * 完整构造。
     *
     * @param registry             白名单
     * @param timeoutMs            超时毫秒
     * @param toolCallAuditMapper  审计 Mapper，可为 null
     */
    public ToolExecutor(ToolRegistry registry, long timeoutMs, ToolCallAuditMapper toolCallAuditMapper) {
        this.registry = registry;
        this.timeoutMs = Math.max(1L, timeoutMs);
        this.toolCallAuditMapper = toolCallAuditMapper;
    }

    /**
     * 执行一次工具调用并返回统一结果（永不抛给调用方业务异常：全部转为 fail 结果）。
     * <p>
     * <b>超时语义</b>：超过 {@code timeoutMs} 取消 Future，返回 error=
     * {@code tool timeout after Nms}，并记失败审计。
     * <p>
     * <b>校验语义</b>：空白名、禁写名、未注册名、Handler 内 IllegalArgumentException
     * 均转为 fail；根因消息经 {@link #rootMessage(Throwable)} 抽取。
     *
     * @param name 工具名
     * @param args 参数 Map，null 视为空 Map
     * @return 成功或失败的 {@link ToolResult}（含 latencyMs）
     */
    public ToolResult run(String name, Map<String, Object> args) {
        // 1) 记录起点，用于 latencyMs
        long t0 = System.currentTimeMillis();
        // 2) 规范化参数，避免 Handler 收到 null Map
        Map<String, Object> safeArgs = args == null ? Map.of() : args;
        try {
            // 3) 禁写硬拦（先于白名单，防止误注册写工具被执行）
            rejectForbidden(name);
            // 4) 白名单解析：未注册即 IllegalArgumentException
            ToolHandler handler = registry.require(name);
            // 5) 触达 definition（保证说明书可加载；当前无副作用）
            handler.definition();
            // 6) 带超时执行只读查询
            Object data = callWithTimeout(() -> handler.execute(safeArgs), timeoutMs);
            // 7) 成功：审计 + ok 结果
            long cost = System.currentTimeMillis() - t0;
            audit(name, safeArgs, true, cost, null);
            return ToolResult.ok(name, data, cost);
        } catch (Exception e) {
            // 8) 任意失败（禁写/缺参/超时/查无）：审计 + fail 结果，不向上抛
            long cost = System.currentTimeMillis() - t0;
            String msg = rootMessage(e);
            audit(name, safeArgs, false, cost, msg);
            return ToolResult.fail(name == null ? "" : name, msg, cost);
        }
    }

    /**
     * 拒绝空白名与禁写名；名称匹配前做 trim、小写、去下划线。
     * 额外规则：同时含 write 与 inventor 子串的名字一律禁止（防 write_inventory 变体）。
     *
     * @param name 原始工具名
     * @throws IllegalArgumentException 空白或禁写
     */
    private static void rejectForbidden(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tool not allowed: (blank)");
        }
        String key = name.trim().toLowerCase(Locale.ROOT).replace("_", "");
        if (FORBIDDEN_NAMES.contains(key) || (key.contains("write") && key.contains("inventor"))) {
            throw new IllegalArgumentException("tool not allowed (forbidden): " + name);
        }
    }

    /**
     * 在线程池提交任务并以指定超时阻塞获取结果。
     *
     * @param task      可调用任务（Handler.execute）
     * @param timeoutMs 超时毫秒
     * @return 任务返回值
     * @throws Exception                任务自身异常（解除包装后的业务异常）
     * @throws IllegalStateException    超时或非 Exception 的 cause
     */
    private Object callWithTimeout(Callable<Object> task, long timeoutMs) throws Exception {
        // 提交到守护线程，便于超时 cancel(true) 中断
        Future<Object> future = pool.submit(task);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // 超时：尝试取消并转为明确 IllegalStateException，供 run 记 fail
            future.cancel(true);
            throw new IllegalStateException("tool timeout after " + timeoutMs + "ms");
        } catch (ExecutionException e) {
            // 解开 Future 包装，把 Handler 抛出的校验异常原样抛回
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw new IllegalStateException(cause);
        }
    }

    /**
     * 写审计：先打日志；若 Mapper 非空则插入 tool_call_audit（失败仅 warn，不影响主流程）。
     * 参数只记 key 集合摘要，避免把完整业务值写入审计造成噪音/泄露。
     *
     * @param name      工具名
     * @param args      参数
     * @param ok        是否成功
     * @param latencyMs 耗时
     * @param error     失败信息，成功为 null
     */
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
                // 列宽保护：argsKeys 最长 250，error 最长 500
                row.setArgsKeys(argSummary == null ? null : (argSummary.length() > 250 ? argSummary.substring(0, 250) : argSummary));
                row.setErrorMessage(error == null ? null : (error.length() > 500 ? error.substring(0, 500) : error));
                row.setLatencyMs(latencyMs);
                toolCallAuditMapper.insertAudit(row);
            } catch (Exception e) {
                // 审计失败不拖垮查询主路径
                log.warn("tool_call_audit insert failed: {}", e.getMessage());
            }
        }
    }

    /**
     * 沿 cause 链取最深非空消息；无消息则用简单类名。
     *
     * @param e 任意异常
     * @return 适合放入 ToolResult.error 的短文案
     */
    private static String rootMessage(Throwable e) {
        Throwable cur = e;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        String m = cur.getMessage();
        return m == null || m.isBlank() ? cur.getClass().getSimpleName() : m;
    }
}
