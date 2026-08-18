package com.erp.ai.common.observability;

import com.erp.ai.chat.prompt.SystemPromptLoader;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.mapper.AiCallAuditMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day22：AI 调用观测统计服务（数据来自 {@code ai_call_audit}）。
 *
 * <h2>职责</h2>
 * 组装观测快照：DB 聚合 + 当前 system prompt 版本 + 关键配置摘录。
 * <b>不是</b>业务清单接口。
 *
 * <h2>为何独立 Service</h2>
 * Controller 保持薄；统计失败可降级（返回 error 字段）而不拖垮整个 HTTP 层约定。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 注入 {@link AiCallAuditMapper}、{@link AiProperties}、{@link SystemPromptLoader}。
 *
 * <h2>学习要点</h2>
 * {@code LinkedHashMap} 保持字段输出顺序稳定，便于演示与 diff。
 */
@Service
public class AiStatsService {

    /** 审计表 Mapper，提供 selectStats 聚合 */
    private final AiCallAuditMapper aiCallAuditMapper;

    /** 读取当前运行配置快照 */
    private final AiProperties properties;

    /** 加载当前 system prompt 正文以计算版本 hash */
    private final SystemPromptLoader systemPromptLoader;

    /**
     * @param aiCallAuditMapper  审计 Mapper
     * @param properties         AI 配置
     * @param systemPromptLoader 系统提示词加载器
     */
    public AiStatsService(AiCallAuditMapper aiCallAuditMapper,
                          AiProperties properties,
                          SystemPromptLoader systemPromptLoader) {
        this.aiCallAuditMapper = aiCallAuditMapper;
        this.properties = properties;
        this.systemPromptLoader = systemPromptLoader;
    }

    /**
     * 构建观测快照。
     * <p>
     * <b>失败语义：</b>{@code selectStats} 抛错时，{@code aiCallAudit} 变为
     * {@code {error: message}}，方法仍返回 200 级结果给 Controller；
     * promptVersion 与 configSnapshot 照常填充。
     *
     * @return 有序 Map，键包括 {@code aiCallAudit}、{@code promptVersion}、{@code configSnapshot}
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Map<String, Object> row = aiCallAuditMapper.selectStats();
            out.put("aiCallAudit", row == null ? Map.of() : row);
        } catch (Exception e) {
            out.put("aiCallAudit", Map.of("error", e.getMessage()));
        }
        out.put("promptVersion", PromptVersions.of("erp-system", systemPromptLoader.getSystemPrompt()));
        out.put("configSnapshot", Map.of(
                "provider", properties.getProvider(),
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "retriever", properties.getRag().getRetriever(),
                "topK", properties.getRag().getTopK(),
                "maxMessages", properties.getSession().getMaxMessages()
        ));
        return out;
    }
}
