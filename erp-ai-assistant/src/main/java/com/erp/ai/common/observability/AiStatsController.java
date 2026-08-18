package com.erp.ai.common.observability;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Day22：AI 调用统计。
 * <p>{@code GET /api/ai/stats}
 */
@RestController
@RequestMapping("/api/ai")
public class AiStatsController {

    private final AiStatsService aiStatsService;

    public AiStatsController(AiStatsService aiStatsService) {
        this.aiStatsService = aiStatsService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return aiStatsService.snapshot();
    }
}
