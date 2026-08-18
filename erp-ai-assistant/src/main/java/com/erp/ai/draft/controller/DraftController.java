package com.erp.ai.draft.controller;

import com.erp.ai.draft.dto.DraftRequest;
import com.erp.ai.draft.dto.DraftResponse;
import com.erp.ai.draft.service.DraftService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day20：单据草稿辅助 HTTP 入口（draft 域 Controller）。
 * <p>
 * <b>职责</b>：对外暴露「自然语言 → 采购订单字段建议」的 REST 接口；
 * 只做协议层（路径映射、Bean Validation、转发），业务编排在 {@link DraftService}。
 * <p>
 * <b>在系统中的位置</b>：{@code com.erp.ai.draft} 域内 controller，与 {@code chat} / {@code tool} /
 * {@code rag} / {@code security} 等包隔离，避免草稿逻辑与多轮聊天耦合。
 * 统一前缀 {@code /api/ai/draft}，当前动作为 {@code POST /api/ai/draft/purchase-order}。
 * <p>
 * <b>对应学习 Day</b>：Day20 采购订单草稿辅助。
 * <p>
 * <b>调用链 / 上下游</b>：
 * <ul>
 *   <li>上游：HTTP 客户端提交 {@link DraftRequest}（字段 {@code utterance}）</li>
 *   <li>本层：校验后委托 {@link DraftService#draftPurchaseOrder(DraftRequest)}</li>
 *   <li>下游：DraftService → DraftPromptLoader / LlmClient / DraftParser / 可选 queryItem 校验</li>
 *   <li>出参：{@link DraftResponse}（建议字段、missing、warnings、needHuman 等）</li>
 * </ul>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li><b>不写业务库</b>：只返回字段建议，不过账、不落采购订单表</li>
 *   <li><b>无 session</b>：与 Chat 多轮历史隔离，避免上下文带跑草稿</li>
 *   <li>默认 needHuman：建议 ≠ 正式单据，必须人工确认</li>
 * </ul>
 *
 * @see DraftService
 * @see DraftRequest
 * @see DraftResponse
 */
@RestController
@RequestMapping("/api/ai/draft")
public class DraftController {

    /** 草稿业务编排服务：本 Controller 唯一依赖 */
    private final DraftService draftService;

    /**
     * 构造注入 {@link DraftService}。
     *
     * @param draftService 采购草稿编排服务，不可为 null
     */
    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    /**
     * 根据自然语言生成采购订单草稿字段建议。
     * <p>
     * 业务含义：用户用一句话描述采购意图，服务端返回结构化字段建议、缺失项与警告，
     * 供人工确认后在真实 ERP 中录入；本接口本身不创建单据。
     * <p>
     * 边界条件：
     * <ul>
     *   <li>{@code utterance} 为空时入站校验失败（400）</li>
     *   <li>模型输出多次非法时下游可能抛 {@link IllegalStateException}</li>
     * </ul>
     *
     * @param request {@code @Valid} 触发 Bean Validation（utterance 非空）
     * @return 草稿建议及观测字段（trace、usage、toolTraces、promptVersion）
     */
    @PostMapping("/purchase-order")
    public DraftResponse purchaseOrder(@Valid @RequestBody DraftRequest request) {
        return draftService.draftPurchaseOrder(request);
    }
}
