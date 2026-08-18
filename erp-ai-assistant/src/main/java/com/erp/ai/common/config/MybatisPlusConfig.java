package com.erp.ai.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus Mapper 扫描配置。
 *
 * <h2>职责</h2>
 * 声明需要被 MyBatis 注册的 Mapper 接口包路径，使 {@code @Mapper} /
 * {@code BaseMapper} 实现可被 Spring 注入。
 *
 * <h2>为何单独配置</h2>
 * 本项目按域分包（common / chat / tool / rag）；集中 {@code @MapperScan}
 * 避免遗漏包或在每个 Mapper 上手写繁琐注册。
 *
 * <h2>与 Spring / SQL 的关系</h2>
 * <ul>
 *   <li>接口在 {@code com.erp.ai.*.mapper}</li>
 *   <li>自定义 SQL 写在 {@code resources/mapper} 下的 XML 文件中</li>
 *   <li>表结构由 schema 脚本维护（如 {@code ai_call_audit}）</li>
 * </ul>
 *
 * <h2>学习要点</h2>
 * 扫不到 Mapper 时常见表现是启动报「找不到 Bean」；先核对本类包列表是否包含新域。
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
