package com.erp.ai.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day16：工具白名单注册表。未注册名称一律拒绝（含一切写库存/过账类名字）。
 */
public class ToolRegistry {

    private final Map<String, ToolHandler> handlers = new LinkedHashMap<>();

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

    public boolean contains(String name) {
        return name != null && handlers.containsKey(name.trim());
    }

    public Collection<ToolHandler> all() {
        return handlers.values();
    }
}
