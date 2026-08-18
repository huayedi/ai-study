package com.erp.ai;

import com.erp.ai.common.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ERP AI 助手 Spring Boot 启动入口（第 1–2 周学习项目主类）。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>作为 JVM 进程入口，启动嵌入式 Web 容器与 Spring 应用上下文</li>
 *   <li>触发组件扫描：默认扫描本类所在包 {@code com.erp.ai} 及其子包
 *       （chat / rag / tool / common 等）</li>
 *   <li>启用 {@link AiProperties} 配置绑定，使 {@code application.yml} 中 {@code ai.*}
 *       可注入到业务 Bean</li>
 * </ul>
 *
 * <h2>为何放在根包</h2>
 * Spring Boot 约定：{@code @SpringBootApplication} 所在包是扫描根。
 * 若把启动类放进过深的子包，会导致其它域的 {@code @Service}/{@code @Mapper} 扫不到。
 *
 * <h2>与配置的关系</h2>
 * <ul>
 *   <li>{@code @SpringBootApplication}：等价于
 *       {@code @Configuration} + {@code @EnableAutoConfiguration} + {@code @ComponentScan}</li>
 *   <li>{@code @EnableConfigurationProperties(AiProperties.class)}：
 *       显式注册类型安全配置；即使尚未被其它 Bean 注入，也会创建并绑定</li>
 *   <li>自动配置会按 classpath 装配 Web、MyBatis、Jackson 等；本项目再通过
 *       {@code common.config.*} 补充 {@code RestTemplate}/{@code LlmClient} 等</li>
 * </ul>
 *
 * <h2>学习要点</h2>
 * <ol>
 *   <li>改配置优先改 YAML / 环境变量，而不是改本类</li>
 *   <li>启动失败时先看控制台绑定错误（如未知 {@code ai.provider}）</li>
 *   <li>IDEA 直接运行 {@link #main(String[])} 即可本地联调</li>
 * </ol>
 */
@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class ErpAiAssistantApplication {

    /**
     * 标准 Spring Boot 启动入口。
     * <p>
     * 调用 {@link SpringApplication#run(Class, String...)} 后会：
     * <ol>
     *   <li>创建 {@code ApplicationContext}</li>
     *   <li>加载 {@code application.yml} 与环境变量</li>
     *   <li>实例化并注入所有 {@code @Bean}/{@code @Component}</li>
     *   <li>启动内嵌 Tomcat（或其它容器）监听 HTTP</li>
     * </ol>
     *
     * @param args 命令行参数；可覆盖部分配置（如 {@code --server.port=8081}）
     */
    public static void main(String[] args) {
        SpringApplication.run(ErpAiAssistantApplication.class, args);
    }
}
