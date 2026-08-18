package com.erp.ai.tool;

import java.util.Map;

/**
 * Day16：单个只读工具的执行接口。未注册的名字不得进入此接口。
 */
public interface ToolHandler {

    String name();

    ToolDefinition definition();

    /**
     * 执行查询。参数非法时应抛 {@link IllegalArgumentException}，
     * 由 {@link ToolExecutor} 统一记审计并返回失败结果。
     */
    Object execute(Map<String, Object> args);
}
