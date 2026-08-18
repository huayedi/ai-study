package com.erp.ai.eval.controller;

import com.erp.ai.eval.dto.EvalRunRequest;
import com.erp.ai.eval.dto.EvalRunResponse;
import com.erp.ai.eval.service.EvalRunnerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day25：轻量评测入口。
 * <p>{@code POST /api/ai/eval/run}
 */
@RestController
@RequestMapping("/api/ai/eval")
public class EvalController {

    private final EvalRunnerService evalRunnerService;

    public EvalController(EvalRunnerService evalRunnerService) {
        this.evalRunnerService = evalRunnerService;
    }

    @PostMapping("/run")
    public EvalRunResponse run(@RequestBody(required = false) EvalRunRequest request) {
        return evalRunnerService.run(request == null ? new EvalRunRequest() : request);
    }
}
