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
 * Tool 域装配：只读白名单 + 执行器（主数据 MyBatis XML → MySQL）。
 */
@Configuration
public class ToolConfig {

    @Bean
    public ToolRegistry toolRegistry(LearningDataRepository learningDataRepository) {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new QueryItemTool(learningDataRepository));
        registry.register(new QueryInventoryTool(learningDataRepository));
        registry.register(new QueryPeriodStatusTool(learningDataRepository));
        return registry;
    }

    @Bean
    public ToolExecutor toolExecutor(ToolRegistry registry,
                                     AiProperties properties,
                                     ToolCallAuditMapper toolCallAuditMapper) {
        return new ToolExecutor(registry, properties.getTool().getTimeoutMs(), toolCallAuditMapper);
    }
}
