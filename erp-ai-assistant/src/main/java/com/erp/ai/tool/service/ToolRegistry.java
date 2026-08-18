package com.erp.ai.tool.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.erp.ai.tool.ToolHandler;

/**
 * 工具白名单注册表（tool 域）。
 * <p>
 * <b>职责</b>：维护「已允许执行」的 {@link ToolHandler} 集合；未注册名一律拒绝。
 * 与 {@link ToolExecutor} 的禁写名单配合，形成「只注册只读 + 再硬拦写名」双层防护。
 * <p>
 * <b>为何没有写工具</b>：{@code ToolConfig} 只 register 三个 query* Handler；
 * 本类不提供「按字符串动态加载写工具」的能力，避免运行期偷偷挂上写实现。
 * <p>
 * <b>Day16 / Day18</b>：两日路径共用同一 Registry Bean。
 * <p>
 * <b>上下游</b>：上游 ToolConfig 注册；下游 Executor.require / Controller.list。
 */
public class ToolRegistry {

    /** name → Handler；LinkedHashMap 保持注册顺序供 list 稳定输出 */
    private final Map<String, ToolHandler> handlers = new LinkedHashMap<>();

    /**
     * 注册一个只读工具；重名拒绝。
     *
     * @param handler 非空且 name 非空白的 Handler
     * @throws IllegalArgumentException handler 或 name 非法
     * @throws IllegalStateException      同名已存在
     */
    public void register(ToolHandler handler) {
        if (handler == null || handler.name() == null || handler.name().isBlank()) {
            throw new IllegalArgumentException("tool handler name required");
        }
        String name = handler.name().trim();
        if (handlers.containsKey(name)) {
            throw new IllegalStateException("duplicate tool: " + name);
        }
        handlers.put(name, handler);
    }

    /**
     * 按名取 Handler；空白或未注册抛 IllegalArgumentException（文案含 tool not allowed）。
     *
     * @param name 工具名
     * @return 已注册 Handler
     * @throws IllegalArgumentException 空白或未在白名单
     */
    public ToolHandler require(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("tool not allowed: (blank)");
        }
        ToolHandler handler = handlers.get(name.trim());
        if (handler == null) {
            throw new IllegalArgumentException("tool not allowed: " + name);
        }
        return handler;
    }

    /**
     * 是否已注册（空白名视为 false）。
     *
     * @param name 工具名
     * @return true 表示白名单内
     */
    public boolean contains(String name) {
        return name != null && handlers.containsKey(name.trim());
    }

    /**
     * 全部已注册 Handler（注册顺序）。
     *
     * @return handlers 的 values 视图
     */
    public Collection<ToolHandler> all() {
        return handlers.values();
    }
}
