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
 * 场景模式库编排服务 — 分类后可选转发到 chat / rag / draft / tool / security。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>调用 {@link ScenarioClassifier} 得到模式与置信度</li>
 *   <li>填充 {@link ScenarioResponse} 的 mode / reason / confidence / routedApi</li>
 *   <li>当 action=assist 时，按 Mode 调用下游并写入 payload；否则仅返回分类信息</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * {@code scenario.service} 的核心编排器，被 {@code ScenarioController} 与
 * {@code EvalRunnerService}（scenario 用例）调用。是跨域「轻量路由器」，
 * 不替代各域自身的完整 API 契约。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day27：场景模式库编排。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * ScenarioController.route / EvalRunnerService.evalScenario
 *   → ScenarioService.handle
 *   → classifier.classify
 *   →（action!=assist）直接返回分类响应
 *   →（action=assist）dispatch(mode) → rag/draft/tool/security/chat/内联审批摘要
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>action 缺省或非 assist 时只分类，不产生副作用式下游调用</li>
 *   <li>TOOL_READONLY 用启发式猜工具与参数（物料码正则、仓库关键词），失败则有默认值</li>
 *   <li>APPROVAL_SUMMARY 为内联固定 Map，明确 needHuman=true，不自动审批</li>
 *   <li>routedApi 仅为教学映射字符串，assist 实际走的是 Java 方法调用而非 HTTP 自调用</li>
 * </ul>
 */
@Service
public class ScenarioService {

    /**
     * 从话术中抽取物料编码的正则。
     * <p>
     * 匹配示例：ITEM-xxx、A001 形式的 A+三位数字、或字母+至少三位数字。
     * 大小写不敏感（{@code (?i)}）。
     * </p>
     */
    private static final Pattern ITEM = Pattern.compile("(?i)\\b(ITEM-[A-Z0-9]+|A\\d{3}|[A-Z]\\d{3,})\\b");

    /** 规则分类器：utterance → Mode。 */
    private final ScenarioClassifier classifier;
    /** 通用聊天下游。 */
    private final ChatService chatService;
    /** RAG 问答下游（手册 / 报错解释共用）。 */
    private final RagService ragService;
    /** 采购订单草稿下游。 */
    private final DraftService draftService;
    /** 只读工具执行器（白名单内）。 */
    private final ToolExecutor toolExecutor;
    /** 安全规则引擎：SECURITY_REFUSE 分支使用。 */
    private final InjectionGuard injectionGuard;

    /**
     * 构造注入各下游依赖。
     *
     * @param classifier     场景分类器
     * @param chatService    聊天服务
     * @param ragService     RAG 服务
     * @param draftService   草稿服务
     * @param toolExecutor   工具执行器
     * @param injectionGuard 注入防护
     */
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

    /**
     * 处理场景请求：始终分类；仅 assist 时转发。
     * <p>
     * 业务含义：一次请求完成「模式识别 +（可选）能力编排」。
     * 边界：action 为 null 时按 classify；比较时 trim + 小写，仅精确等于 {@code assist} 才转发。
     * </p>
     *
     * @param request 含 utterance 与可选 action；调用方应已保证 utterance 非空（Controller 校验）
     * @return 含 traceId / mode / reason / confidence / routedApi，以及可选 payload
     */
    public ScenarioResponse handle(ScenarioRequest request) {
        // 每次请求独立 trace，便于与下游各域日志对齐（下游可能另有自己的 trace）
        String traceId = UUID.randomUUID().toString().replace("-", "");
        ScenarioClassifier.Classification c = classifier.classify(request.getUtterance());
        ScenarioResponse response = new ScenarioResponse();
        response.setTraceId(traceId);
        response.setMode(c.mode().name());
        response.setReason(c.reason());
        response.setConfidence(c.confidence());
        // 教学用：告知「若走 HTTP 应对应哪条 API」
        response.setRoutedApi(apiFor(c.mode()));

        // action 缺省视为 classify；仅 assist 才 dispatch
        String action = request.getAction() == null ? "classify" : request.getAction().trim().toLowerCase(Locale.ROOT);
        if (!"assist".equals(action)) {
            return response;
        }
        response.setPayload(dispatch(c.mode(), request.getUtterance()));
        return response;
    }

    /**
     * 按模式调用对应下游能力，返回可作为 JSON 序列化的 payload 对象。
     * <p>
     * 不做 HTTP 回环，直接注入调用各 Service / ToolExecutor / InjectionGuard。
     * </p>
     *
     * @param mode      已分类的模式
     * @param utterance 原用户话术，透传给下游作为 question/message/utterance
     * @return 下游响应对象或内联 Map；由 switch 穷尽 Mode
     */
    private Object dispatch(ScenarioClassifier.Mode mode, String utterance) {
        return switch (mode) {
            // 手册与报错解释都走 RAG ask
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
                // 复用 InjectionGuard，组装与安全探针类似的结构（无独立 traceId）
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

    /**
     * 根据话术启发式选择只读工具并执行。
     * <p>
     * 业务含义：学习版「猜工具」——含「期间」走 queryPeriodStatus；含库存类词走 queryInventory；
     * 否则 queryItem。物料码与仓库有缺省值（A001 / 原料仓）。
     * </p>
     *
     * @param utterance 用户话术
     * @return 有序 Map：tool / ok / data / error，便于前端或评测查看
     */
    private Map<String, Object> invokeReadonlyGuess(String utterance) {
        Map<String, Object> out = new LinkedHashMap<>();
        // 期间状态：学习假数据固定公司与期间
        if (utterance.contains("期间")) {
            ToolResult r = toolExecutor.run("queryPeriodStatus", Map.of("company", "主公司", "period", "2026-08"));
            out.put("tool", "queryPeriodStatus");
            out.put("ok", r.ok());
            out.put("data", r.data());
            out.put("error", r.error());
            return out;
        }
        // 抽取物料码；找不到则默认 A001
        Matcher m = ITEM.matcher(utterance);
        String item = m.find() ? m.group(1).toUpperCase(Locale.ROOT) : "A001";
        // 仓库：提到成品仓则用成品仓，否则原料仓
        String warehouse = utterance.contains("成品仓") ? "成品仓" : "原料仓";
        if (utterance.contains("库存") || utterance.contains("现存量") || utterance.contains("多少")) {
            ToolResult r = toolExecutor.run("queryInventory", Map.of("itemCode", item, "warehouse", warehouse));
            out.put("tool", "queryInventory");
            out.put("ok", r.ok());
            out.put("data", r.data());
            out.put("error", r.error());
            return out;
        }
        // 默认查物料主数据
        ToolResult r = toolExecutor.run("queryItem", Map.of("itemCode", item));
        out.put("tool", "queryItem");
        out.put("ok", r.ok());
        out.put("data", r.data());
        out.put("error", r.error());
        return out;
    }

    /**
     * 将 Mode 映射为教学用「应对 HTTP API」字符串。
     * <p>实际 assist 并不发起这些 HTTP，仅用于响应中的 routedApi 字段展示。</p>
     *
     * @param mode 场景模式
     * @return 对应 API 路径或 inline 标识
     */
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
