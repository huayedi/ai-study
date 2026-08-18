package com.erp.ai.eval.service;

import com.erp.ai.chat.dto.ChatRequest;
import com.erp.ai.chat.dto.ChatResponse;
import com.erp.ai.chat.service.ChatService;
import com.erp.ai.draft.dto.DraftRequest;
import com.erp.ai.draft.dto.DraftResponse;
import com.erp.ai.draft.service.DraftService;
import com.erp.ai.eval.dto.EvalRunRequest;
import com.erp.ai.eval.dto.EvalRunResponse;
import com.erp.ai.rag.dto.RagAskRequest;
import com.erp.ai.rag.dto.RagAskResponse;
import com.erp.ai.rag.service.RagService;
import com.erp.ai.scenario.dto.ScenarioRequest;
import com.erp.ai.scenario.dto.ScenarioResponse;
import com.erp.ai.scenario.service.ScenarioService;
import com.erp.ai.security.service.InjectionGuard;
import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.service.ToolExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Day25：轻量 JSONL 评测 runner（学习版，mock 友好）。
 */
@Service
public class EvalRunnerService {

    private final ObjectMapper objectMapper;
    private final RagService ragService;
    private final ChatService chatService;
    private final DraftService draftService;
    private final ToolExecutor toolExecutor;
    private final InjectionGuard injectionGuard;
    private final ScenarioService scenarioService;

    public EvalRunnerService(ObjectMapper objectMapper,
                             RagService ragService,
                             ChatService chatService,
                             DraftService draftService,
                             ToolExecutor toolExecutor,
                             InjectionGuard injectionGuard,
                             ScenarioService scenarioService) {
        this.objectMapper = objectMapper;
        this.ragService = ragService;
        this.chatService = chatService;
        this.draftService = draftService;
        this.toolExecutor = toolExecutor;
        this.injectionGuard = injectionGuard;
        this.scenarioService = scenarioService;
    }

    public EvalRunResponse run(EvalRunRequest request) {
        String suite = request.getSuite() == null || request.getSuite().isBlank()
                ? "evals/month1-smoke.jsonl"
                : request.getSuite().trim();
        List<JsonNode> cases = loadCases(suite);
        EvalRunResponse response = new EvalRunResponse();
        response.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        response.setSuite(suite);
        List<EvalRunResponse.CaseResult> results = new ArrayList<>();
        int passed = 0;
        for (JsonNode node : cases) {
            EvalRunResponse.CaseResult one = evaluate(node);
            results.add(one);
            if (one.isPassed()) {
                passed++;
            }
        }
        response.setResults(results);
        response.setTotal(results.size());
        response.setPassed(passed);
        response.setFailed(results.size() - passed);
        return response;
    }

    private EvalRunResponse.CaseResult evaluate(JsonNode node) {
        String id = text(node, "id", "unknown");
        String type = text(node, "type", "chat");
        try {
            return switch (type) {
                case "rag" -> evalRag(id, type, node);
                case "security" -> evalSecurity(id, type, node);
                case "tool" -> evalTool(id, type, node);
                case "draft" -> evalDraft(id, type, node);
                case "chat" -> evalChat(id, type, node);
                case "scenario" -> evalScenario(id, type, node);
                default -> new EvalRunResponse.CaseResult(id, type, false, "unknown type");
            };
        } catch (Exception e) {
            return new EvalRunResponse.CaseResult(id, type, false, "exception: " + e.getMessage());
        }
    }

    private EvalRunResponse.CaseResult evalRag(String id, String type, JsonNode node) {
        RagAskRequest req = new RagAskRequest();
        req.setQuestion(text(node, "question", ""));
        RagAskResponse resp = ragService.ask(req);
        boolean ok = true;
        StringBuilder detail = new StringBuilder();
        if (node.has("expectDocs") && node.get("expectDocs").isArray()) {
            for (JsonNode d : node.get("expectDocs")) {
                String expect = d.asText();
                boolean hit = resp.getSources().stream()
                        .anyMatch(s -> s.getDocId() != null && s.getDocId().contains(expect));
                if (!hit) {
                    ok = false;
                    detail.append("missing doc ").append(expect).append("; ");
                }
            }
        }
        if (node.has("expectNeedHuman")) {
            boolean expect = node.get("expectNeedHuman").asBoolean();
            if (resp.getReply() == null || resp.getReply().isNeedHuman() != expect) {
                // EMPTY gate may force needHuman; soft-fail only if expect false but sources empty is ok for smoke
                if (expect && (resp.getReply() == null || !resp.getReply().isNeedHuman())) {
                    ok = false;
                    detail.append("needHuman mismatch; ");
                }
            }
        }
        if (ok) {
            detail.append("sources=").append(resp.getSources().size())
                    .append(" gate=").append(resp.getGate());
        }
        return new EvalRunResponse.CaseResult(id, type, ok, detail.toString().trim());
    }

    private EvalRunResponse.CaseResult evalSecurity(String id, String type, JsonNode node) {
        InjectionGuard.Verdict v = injectionGuard.inspect(text(node, "question", ""));
        boolean expectBlocked = !node.has("expectBlocked") || node.get("expectBlocked").asBoolean();
        boolean ok = v.blocked() == expectBlocked;
        return new EvalRunResponse.CaseResult(id, type, ok,
                "blocked=" + v.blocked() + " category=" + v.category());
    }

    private EvalRunResponse.CaseResult evalTool(String id, String type, JsonNode node) {
        String toolName = text(node, "toolName", "");
        @SuppressWarnings("unchecked")
        Map<String, Object> args = objectMapper.convertValue(node.get("args"), Map.class);
        ToolResult result = toolExecutor.run(toolName, args == null ? Map.of() : args);
        boolean ok = true;
        if (node.has("expectOk") && result.ok() != node.get("expectOk").asBoolean()) {
            ok = false;
        }
        if (ok && node.has("expectQty") && result.data() instanceof Map<?, ?> data) {
            Object qty = data.get("qty");
            double expect = node.get("expectQty").asDouble();
            if (qty == null || Math.abs(((Number) qty).doubleValue() - expect) > 0.001) {
                ok = false;
            }
        }
        return new EvalRunResponse.CaseResult(id, type, ok,
                "ok=" + result.ok() + " error=" + result.error());
    }

    private EvalRunResponse.CaseResult evalDraft(String id, String type, JsonNode node) {
        DraftRequest req = new DraftRequest();
        req.setUtterance(text(node, "utterance", ""));
        DraftResponse draft = draftService.draftPurchaseOrder(req);
        boolean ok = true;
        StringBuilder detail = new StringBuilder();
        if (node.has("expectFields") && node.get("expectFields").isArray()) {
            for (JsonNode f : node.get("expectFields")) {
                Object v = draft.getFields().get(f.asText());
                if (v == null || String.valueOf(v).isBlank()) {
                    ok = false;
                    detail.append("missing field ").append(f.asText()).append("; ");
                }
            }
        }
        if (node.has("expectMissingContains") && node.get("expectMissingContains").isArray()) {
            for (JsonNode m : node.get("expectMissingContains")) {
                if (draft.getMissing().stream().noneMatch(x -> x.contains(m.asText()))) {
                    ok = false;
                    detail.append("missing list lacks ").append(m.asText()).append("; ");
                }
            }
        }
        if (node.has("expectNeedHuman") && draft.isNeedHuman() != node.get("expectNeedHuman").asBoolean()) {
            ok = false;
            detail.append("needHuman mismatch; ");
        }
        if (ok) {
            detail.append("fields=").append(draft.getFields().keySet());
        }
        return new EvalRunResponse.CaseResult(id, type, ok, detail.toString().trim());
    }

    private EvalRunResponse.CaseResult evalChat(String id, String type, JsonNode node) {
        ChatRequest req = new ChatRequest();
        req.setMessage(text(node, "question", ""));
        ChatResponse resp = chatService.chat(req);
        boolean ok = true;
        if (node.has("expectNeedHuman")
                && resp.getReply().isNeedHuman() != node.get("expectNeedHuman").asBoolean()) {
            ok = false;
        }
        if (node.has("expectDocTypeContains")) {
            String needle = node.get("expectDocTypeContains").asText();
            String doc = resp.getReply().getSuggestedDocType();
            if (doc == null || !doc.contains(needle)) {
                ok = false;
            }
        }
        return new EvalRunResponse.CaseResult(id, type, ok,
                "needHuman=" + resp.getReply().isNeedHuman()
                        + " docType=" + resp.getReply().getSuggestedDocType());
    }

    private EvalRunResponse.CaseResult evalScenario(String id, String type, JsonNode node) {
        ScenarioRequest req = new ScenarioRequest();
        req.setUtterance(text(node, "question", ""));
        req.setAction("classify");
        ScenarioResponse resp = scenarioService.handle(req);
        String expectMode = text(node, "expectMode", "");
        boolean ok = expectMode.isBlank() || expectMode.equals(resp.getMode());
        return new EvalRunResponse.CaseResult(id, type, ok,
                "mode=" + resp.getMode() + " api=" + resp.getRoutedApi());
    }

    private List<JsonNode> loadCases(String suite) {
        try {
            ClassPathResource resource = new ClassPathResource(suite);
            String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            List<JsonNode> list = new ArrayList<>();
            for (String line : body.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                list.add(objectMapper.readTree(trimmed));
            }
            return list;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法加载评测集: " + suite + " — " + e.getMessage(), e);
        }
    }

    private static String text(JsonNode node, String field, String defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asText();
    }
}
