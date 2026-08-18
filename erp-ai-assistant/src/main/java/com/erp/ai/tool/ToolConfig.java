package com.erp.ai.tool;

import com.erp.ai.config.AiProperties;
import com.erp.ai.tool.tools.QueryInventoryTool;
import com.erp.ai.tool.tools.QueryItemTool;
import com.erp.ai.tool.tools.QueryPeriodStatusTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Day16：装配只读工具白名单与执行器。
 */
@Configuration
public class ToolConfig {

    @Bean
    public ToolRegistry toolRegistry() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new QueryItemTool());
        registry.register(new QueryInventoryTool());
        registry.register(new QueryPeriodStatusTool());
        return registry;
    }

    @Bean
    public ToolExecutor toolExecutor(ToolRegistry registry, AiProperties properties) {
        return new ToolExecutor(registry, properties.getTool().getTimeoutMs());
    }
}
