package com.erp.ai.rag.controller;

import com.erp.ai.rag.service.RagService;
import com.erp.ai.rag.dto.RagAskRequest;
import com.erp.ai.rag.dto.RagAskResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 问答 HTTP 入口：接收用户问题并返回带 sources / gate 可观测字段的回答。
 * <p>
 * <b>职责</b>：薄控制器，参数校验后委托 {@link RagService} 完成完整流水线。
 * <p>
 * <b>RAG 流水线位置</b>：API 边界（流水线外层）；内部依次触发
 * retrieve → gate → prompt → generate。
 * <p>
 * <b>对应 Day</b>：Day10+（RAG API）；与 Day13 Gate、Day24 降级字段一并暴露于响应。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>入参 {@link RagAskRequest}、出参 {@link RagAskResponse}；</li>
 *   <li>唯一业务依赖 {@link RagService}。</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    /** RAG 编排服务 */
    private final RagService ragService;

    /**
     * @param ragService RAG 业务编排 Bean
     */
    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 教材增强问答接口。
     * <p>
     * 成功时返回回答、引用 sources、gate 强度等；空命中可能跳过 LLM（由服务配置决定）。
     *
     * @param request 含非空 {@code question} 的请求体（{@code @Valid} 校验）
     * @return 完整 RAG 响应
     * @throws IllegalStateException 当 LLM 重试耗尽等服务层失败时（由全局异常处理转换）
     */
    @PostMapping("/ask")
    public RagAskResponse ask(@Valid @RequestBody RagAskRequest request) {
        return ragService.ask(request);
    }
}
