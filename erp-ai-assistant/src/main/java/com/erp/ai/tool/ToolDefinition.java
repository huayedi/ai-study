package com.erp.ai.tool;

import java.util.Map;

/**
 * Day16：工具元数据（暴露给模型 / 控制台的「说明书」）。
 * <p>
 * <b>职责</b>：描述白名单工具的稳定名、自然语言说明、参数结构（JSON Schema 风格 Map），
 * 供 {@code GET /api/ai/tool/list}、模型 tool 列表、路径 A function-calling 使用。
 * <p>
 * <b>只读白名单 / 禁写约束</b>：{@code description} <strong>必须</strong>诚实写清
 * 「只读」「学习假数据」，避免模型以为能改库存、过账或关账。
 * 本仓库<strong>不注册</strong>任何 write* / update* / post* / delete* / pay* / closePeriod 类定义——
 * 因为助手职责是答疑与查数，写账必须由人工在 ERP 完成，防止误操作与责任不清。
 * <p>
 * <b>Day16 / Day18</b>：Day16 列表接口返回本记录集合；Day18 路径 A 把本定义交给模型选工具。
 * <p>
 * <b>上下游</b>：上游各 {@link ToolHandler#definition()}；下游 Controller list、Chat 编排、模型 SDK。
 *
 * @param name             工具稳定名（与 Handler.name / 白名单键一致）
 * @param description      给人与模型看的说明（须含只读语义）
 * @param parametersSchema 参数结构描述（type/object、required、properties 等）
 */
public record ToolDefinition(
        /** 工具名，如 queryItem */
        String name,
        /** 只读说明文案 */
        String description,
        /** 参数 Schema（Map 形式，非严格 JSON Schema 校验器） */
        Map<String, Object> parametersSchema
) {
}
