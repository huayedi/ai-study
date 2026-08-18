package com.erp.ai.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus：按域扫描 Mapper。SQL 写在 resources/mapper 下 XML。
 */
@Configuration
@MapperScan({
        "com.erp.ai.common.mapper",
        "com.erp.ai.chat.mapper",
        "com.erp.ai.tool.mapper",
        "com.erp.ai.rag.mapper"
})
public class MybatisPlusConfig {
}
