package com.erp.ai.tool.config;

import com.erp.ai.common.config.AiProperties;
import com.erp.ai.tool.handler.QueryInventoryTool;
import com.erp.ai.tool.handler.QueryItemTool;
import com.erp.ai.tool.handler.QueryPeriodStatusTool;
import com.erp.ai.tool.mapper.ToolCallAuditMapper;
import com.erp.ai.tool.service.LearningDataRepository;
import com.erp.ai.tool.service.ToolExecutor;
import com.erp.ai.tool.service.ToolRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tool 域 Spring 装配：只读白名单注册表 + 带超时/审计的执行器。
 * <p>
 * <b>职责</b>：把三个只读 Handler 挂到 {@link ToolRegistry}，并把超时配置、审计 Mapper
 * 注入 {@link ToolExecutor}。主数据经 MyBatis XML → MySQL {@code learning_*}。
 * <p>
 * <b>只读白名单 / 禁写约束</b>：此处<strong>仅</strong>注册
 * {@code queryItem} / {@code queryInventory} / {@code queryPeriodStatus}。
 * <strong>故意不注册</strong>任何写库存、过账、删单、付款、开关期间的 Bean——
 * 原因：学习助手边界是「查假数据答疑」，写账责任在人工 ERP，避免 AI/手测误写生产。
 * <p>
 * <b>Day16 / Day18</b>：Day16 HTTP 与 Day18 Chat（路径 A/B）共用本配置产出的 Registry/Executor。
 * <p>
 * <b>上下游</b>：上游 Spring 容器与 {@link AiProperties}；下游 Controller、Orchestrator、Handlers。
 */
@Configuration
public class ToolConfig {

    /**
     * 构建只读工具白名单：三工具顺序固定，便于 list 接口稳定展示。
     *
     * @param learningDataRepository 学习主数据仓储（通常为 MySQL 实现）
     * @return 已注册三只读工具的 Registry
     */
    @Bean
    public ToolRegistry toolRegistry(LearningDataRepository learningDataRepository) {
        ToolRegistry registry = new ToolRegistry();
        // 只读：存货主数据
        registry.register(new QueryItemTool(learningDataRepository));
        // 只读：现存量
        registry.register(new QueryInventoryTool(learningDataRepository));
        // 只读：期间状态 — 无写工具注册点
        registry.register(new QueryPeriodStatusTool(learningDataRepository));
        return registry;
    }

    /**
     * 构建执行器：白名单解析 + 禁写硬拦 + 超时 + 审计落库。
     *
     * @param registry             只读白名单
     * @param properties           AI 配置（读取 tool.timeoutMs）
     * @param toolCallAuditMapper  审计表 Mapper（可为运行时注入）
     * @return ToolExecutor Bean
     */
    @Bean
    public ToolExecutor toolExecutor(ToolRegistry registry,
                                     AiProperties properties,
                                     ToolCallAuditMapper toolCallAuditMapper) {
        // timeoutMs 下限由 Executor 构造器再钳制为至少 1ms
        return new ToolExecutor(registry, properties.getTool().getTimeoutMs(), toolCallAuditMapper);
    }
}
