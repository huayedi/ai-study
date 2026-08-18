package com.erp.ai.scenario.controller;

import com.erp.ai.scenario.dto.ScenarioRequest;
import com.erp.ai.scenario.dto.ScenarioResponse;
import com.erp.ai.scenario.service.ScenarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day27：场景模式库入口。
 * <p>{@code POST /api/ai/scenario/route} — classify 或 assist}
 */
@RestController
@RequestMapping("/api/ai/scenario")
public class ScenarioController {

    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping("/route")
    public ScenarioResponse route(@Valid @RequestBody ScenarioRequest request) {
        return scenarioService.handle(request);
    }
}
