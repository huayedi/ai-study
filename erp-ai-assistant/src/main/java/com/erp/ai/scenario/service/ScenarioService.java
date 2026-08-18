package com.erp.ai.scenario.service;

import com.erp.ai.chat.dto.ChatRequest;
import com.erp.ai.chat.service.ChatService;
import com.erp.ai.draft.dto.DraftRequest;
import com.erp.ai.draft.service.DraftService;
import com.erp.ai.rag.dto.RagAskRequest;
import com.erp.ai.rag.service.RagService;
import com.erp.ai.scenario.dto.ScenarioRequest;
import com.erp.ai.scenario.dto.ScenarioResponse;
import com.erp.ai.security.service.InjectionGuard;
import com.erp.ai.security.dto.SecurityProbeResponse;
import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.service.ToolExecutor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Day27：场景模式库编排 — 分类后可选转发到 chat/rag/draft/tool/security。
 */
@Service
public class ScenarioService {

    private static final Pattern ITEM = Pattern.compile("(?i)\\b(ITEM-[A-Z0-9]+|A\\d{3}|[A-Z]\\d{3,})\\b");

    private final ScenarioClassifier classifier;
    private final ChatService chatService;
    private final RagService ragService;
    private final DraftService draftService;
    private final ToolExecutor toolExecutor;
    private final InjectionGuard injectionGuard;

    public ScenarioService(ScenarioClassifier classifier,
                           ChatService chatService,
                           RagService ragService,
                           DraftService draftService,
                           ToolExecutor toolExecutor,
                           InjectionGuard injectionGuard) {
        this.classifier = classifier;
        this.chatService = chatService;
        this.ragService = ragService;
        this.draftService = draftService;
        this.toolExecutor = toolExecutor;
        this.injectionGuard = injectionGuard;
    }

    public ScenarioResponse handle(ScenarioRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        ScenarioClassifier.Classification c = classifier.classify(request.getUtterance());
        ScenarioResponse response = new ScenarioResponse();
        response.setTraceId(traceId);
        response.setMode(c.mode().name());
        response.setReason(c.reason());
        response.setConfidence(c.confidence());
        response.setRoutedApi(apiFor(c.mode()));

        String action = request.getAction() == null ? "classify" : request.getAction().trim().toLowerCase(Locale.ROOT);
        if (!"assist".equals(action)) {
            return response;
        }
        response.setPayload(dispatch(c.mode(), request.getUtterance()));
        return response;
    }

    private Object dispatch(ScenarioClassifier.Mode mode, String utterance) {
        return switch (mode) {
            case RAG_MANUAL, ERROR_EXPLAIN -> {
                RagAskRequest req = new RagAskRequest();
                req.setQuestion(utterance);
                yield ragService.ask(req);
            }
            case DRAFT_PO -> {
                DraftRequest req = new DraftRequest();
                req.setUtterance(utterance);
                yield draftService.draftPurchaseOrder(req);
            }
            case TOOL_READONLY -> invokeReadonlyGuess(utterance);
            case SECURITY_REFUSE -> {
                InjectionGuard.Verdict v = injectionGuard.inspect(utterance);
                SecurityProbeResponse probe = new SecurityProbeResponse();
                probe.setBlocked(v.blocked());
                probe.setCategory(v.category().name());
                probe.setReason(v.reason());
                probe.setMatched(v.matched());
                probe.setDefenseHint("无写工具 + 规则早拦 + need_human");
                yield probe;
            }
            case APPROVAL_SUMMARY -> Map.of(
                    "summary", "学习版审批摘要：请人工核对金额/期间/权限；本助手不自动审批、不过账。",
                    "needHuman", true,
                    "riskPoints", java.util.List.of("金额异常", "期间状态", "越权审批")
            );
            case CHAT_GENERAL -> {
                ChatRequest req = new ChatRequest();
                req.setMessage(utterance);
                yield chatService.chat(req);
            }
        };
    }

    private Map<String, Object> invokeReadonlyGuess(String utterance) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (utterance.contains("期间")) {
            ToolResult r = toolExecutor.run("queryPeriodStatus", Map.of("company", "主公司", "period", "2026-08"));
            out.put("tool", "queryPeriodStatus");
            out.put("ok", r.ok());
            out.put("data", r.data());
            out.put("error", r.error());
            return out;
        }
        Matcher m = ITEM.matcher(utterance);
        String item = m.find() ? m.group(1).toUpperCase(Locale.ROOT) : "A001";
        String warehouse = utterance.contains("成品仓") ? "成品仓" : "原料仓";
        if (utterance.contains("库存") || utterance.contains("现存量") || utterance.contains("多少")) {
            ToolResult r = toolExecutor.run("queryInventory", Map.of("itemCode", item, "warehouse", warehouse));
            out.put("tool", "queryInventory");
            out.put("ok", r.ok());
            out.put("data", r.data());
            out.put("error", r.error());
            return out;
        }
        ToolResult r = toolExecutor.run("queryItem", Map.of("itemCode", item));
        out.put("tool", "queryItem");
        out.put("ok", r.ok());
        out.put("data", r.data());
        out.put("error", r.error());
        return out;
    }

    private static String apiFor(ScenarioClassifier.Mode mode) {
        return switch (mode) {
            case RAG_MANUAL, ERROR_EXPLAIN -> "POST /api/ai/rag/ask";
            case DRAFT_PO -> "POST /api/ai/draft/purchase-order";
            case TOOL_READONLY -> "POST /api/ai/tool/invoke";
            case SECURITY_REFUSE -> "POST /api/ai/security/probe";
            case APPROVAL_SUMMARY -> "inline:approval-summary";
            case CHAT_GENERAL -> "POST /api/ai/chat";
        };
    }
}
