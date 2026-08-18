package com.erp.ai.tool;

import java.util.Map;

/**
 * Day16：工具元数据（暴露给模型/控制台的「说明书」）。
 * <p>
 * description 必须诚实写清「只读」「学习假数据」，避免模型以为能改库存。
 */
public record ToolDefinition(
        String name,
        String description,
        Map<String, Object> parametersSchema
) {
}
