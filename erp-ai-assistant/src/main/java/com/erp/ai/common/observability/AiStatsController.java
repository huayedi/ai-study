package com.erp.ai.common.observability;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Day22：AI 调用统计 HTTP 入口。
 *
 * <h2>职责</h2>
 * 暴露 {@code GET /api/ai/stats}，返回审计聚合、提示词版本与关键配置快照。
 *
 * <h2>为何放在 common.observability</h2>
 * 观测属于横切能力，不属于单一业务域（chat/rag/tool）。
 *
 * <h2>与 Spring 的关系</h2>
 * 标准 {@code @RestController}；委托 {@link AiStatsService#snapshot()}，本类不含业务计算。
 *
 * <h2>学习要点</h2>
 * 这是运维/学习观察接口，不是业务清单 API；勿与库存查询等混淆。
 */
@RestController
@RequestMapping("/api/ai")
public class AiStatsController {

    /** 统计领域服务 */
    private final AiStatsService aiStatsService;

    /**
     * @param aiStatsService 统计服务；由 Spring 注入
     */
    public AiStatsController(AiStatsService aiStatsService) {
        this.aiStatsService = aiStatsService;
    }

    /**
     * 返回当前 AI 观测快照。
     * <p>
     * <b>失败语义：</b>服务内部对 DB 聚合失败会写入 error 字段而不是抛 500，
     * 以保证「至少能看到配置与 prompt 版本」。
     *
     * @return 含 {@code aiCallAudit} / {@code promptVersion} / {@code configSnapshot} 的 Map
     */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return aiStatsService.snapshot();
    }
}
