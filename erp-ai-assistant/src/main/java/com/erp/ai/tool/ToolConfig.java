package com.erp.ai.tool;

import com.erp.ai.config.AiProperties;
import com.erp.ai.store.JdbcToolCallAuditRepository;
import com.erp.ai.store.LearningDataRepository;
import com.erp.ai.tool.tools.QueryInventoryTool;
import com.erp.ai.tool.tools.QueryItemTool;
import com.erp.ai.tool.tools.QueryPeriodStatusTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Day16：装配只读工具白名单与执行器（主数据来自学习库 MySQL）。
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
                                     JdbcToolCallAuditRepository toolCallAuditRepository) {
        return new ToolExecutor(registry, properties.getTool().getTimeoutMs(), toolCallAuditRepository);
    }
}
