package com.erp.ai.common.observability;

import com.erp.ai.chat.prompt.SystemPromptLoader;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.mapper.AiCallAuditMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day22：AI 调用观测统计（来自 ai_call_audit），非业务清单接口。
 */
@Service
public class AiStatsService {

    private final AiCallAuditMapper aiCallAuditMapper;
    private final AiProperties properties;
    private final SystemPromptLoader systemPromptLoader;

    public AiStatsService(AiCallAuditMapper aiCallAuditMapper,
                          AiProperties properties,
                          SystemPromptLoader systemPromptLoader) {
        this.aiCallAuditMapper = aiCallAuditMapper;
        this.properties = properties;
        this.systemPromptLoader = systemPromptLoader;
    }

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
