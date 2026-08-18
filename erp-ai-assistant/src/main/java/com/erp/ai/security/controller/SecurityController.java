package com.erp.ai.security.controller;

import com.erp.ai.security.dto.SecurityProbeRequest;
import com.erp.ai.security.dto.SecurityProbeResponse;
import com.erp.ai.security.service.InjectionGuard;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Day23：安全抽测入口（域内 controller）。
 * <p>{@code POST /api/ai/security/probe}
 */
@RestController
@RequestMapping("/api/ai/security")
public class SecurityController {

    private final InjectionGuard injectionGuard;

    public SecurityController(InjectionGuard injectionGuard) {
        this.injectionGuard = injectionGuard;
    }

    @PostMapping("/probe")
    public SecurityProbeResponse probe(@Valid @RequestBody SecurityProbeRequest request) {
        InjectionGuard.Verdict verdict = injectionGuard.inspect(request.getUtterance());
        SecurityProbeResponse response = new SecurityProbeResponse();
        response.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        response.setBlocked(verdict.blocked());
        response.setCategory(verdict.category().name());
        response.setReason(verdict.reason());
        response.setMatched(verdict.matched());
        response.setDefenseHint(
                "分层防线：提示词拒答 + JSON 校验 + 工具白名单(无写库存) + RAG 资料不可覆盖安全策略"
        );
        return response;
    }
}
