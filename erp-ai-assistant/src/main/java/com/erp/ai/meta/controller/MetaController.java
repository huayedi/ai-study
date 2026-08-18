package com.erp.ai.meta.controller;

import com.erp.ai.meta.service.MetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Day21/22/26/28/29/30：元信息与收官接口（域内 controller）。
 */
@RestController
@RequestMapping("/api/ai")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    /** Day21：能力边界（含无写工具） */
    @GetMapping("/meta/capabilities")
    public Map<String, Object> capabilities() {
        return metaService.capabilities();
    }

    /** Day26：架构总复习 */
    @GetMapping("/meta/architecture")
    public Map<String, Object> architecture() {
        return metaService.architecture();
    }

    /** Day28：可维护性检查清单 */
    @GetMapping("/meta/health-checklist")
    public Map<String, Object> healthChecklist() {
        return metaService.healthChecklist();
    }

    /** Day29：口述自测 15 题 */
    @GetMapping("/meta/oral-quiz")
    public Map<String, Object> oralQuiz() {
        return metaService.oralQuiz();
    }

    /** Day30：第1月收官清单 */
    @GetMapping("/meta/month1-checklist")
    public Map<String, Object> month1Checklist() {
        return metaService.month1Checklist();
    }

    /** Day22：观测统计 + 配置快照 */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return metaService.stats();
    }
}
