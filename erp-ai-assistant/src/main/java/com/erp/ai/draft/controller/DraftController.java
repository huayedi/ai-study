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
 * Day20：单据草稿辅助 HTTP 入口（域内 controller，与 chat/tool/rag 隔离）。
 * <p>
 * {@code POST /api/ai/draft/purchase-order} — 自然语言 → 采购订单字段建议；不写业务库。
 */
@RestController
@RequestMapping("/api/ai/draft")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping("/purchase-order")
    public DraftResponse purchaseOrder(@Valid @RequestBody DraftRequest request) {
        return draftService.draftPurchaseOrder(request);
    }
}
