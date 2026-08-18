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
 * 轻量 JSONL 评测 Runner（学习版，mock 友好）。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>从 classpath 加载 JSONL 评测套件（每行一个 JSON 用例）</li>
 *   <li>按 {@code type} 分发到 rag / security / tool / draft / chat / scenario 断言逻辑</li>
 *   <li>汇总通过/失败并返回 {@link EvalRunResponse}</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * {@code eval.service} 核心。被 {@code EvalController} 调用；真实调用各域 Service，
 * 用于月度冒烟与回归，而非生产用户路径。依赖的 LLM 在学习环境中通常为 Mock。
 * </p>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day25：轻量 JSONL 评测 runner。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * EvalController.run
 *   → EvalRunnerService.run
 *   → loadCases(suite) 读 ClassPathResource
 *   → 对每行 evaluate(node) → switch(type) → evalRag/evalSecurity/...
 *   → 统计 passed/failed → EvalRunResponse
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>suite 缺省为 {@code evals/month1-smoke.jsonl}</li>
 *   <li>未知 type 记失败；任意异常捕获后记失败（detail 含 exception 消息）</li>
 *   <li>JSONL 空行与 {@code #} 开头行跳过</li>
 *   <li>各断言字段均为「有则校验」：JSON 节点缺字段时多数断言跳过或采用软默认</li>
 *   <li>不修改业务代码行为，只观察并比对期望</li>
 * </ul>
 */
@Service
public class EvalRunnerService {

    /** JSON 解析与 JsonNode→Map 转换。 */
    private final ObjectMapper objectMapper;
    /** RAG 问答服务（type=rag）。 */
    private final RagService ragService;
    /** 聊天服务（type=chat）。 */
    private final ChatService chatService;
    /** 采购草稿服务（type=draft）。 */
    private final DraftService draftService;
    /** 只读工具执行器（type=tool）。 */
    private final ToolExecutor toolExecutor;
    /** 注入防护（type=security）。 */
    private final InjectionGuard injectionGuard;
    /** 场景编排（type=scenario，固定 action=classify）。 */
    private final ScenarioService scenarioService;

    /**
     * 构造注入评测所需的全部下游依赖。
     *
     * @param objectMapper     Jackson ObjectMapper
     * @param ragService       RAG 服务
     * @param chatService      聊天服务
     * @param draftService     草稿服务
     * @param toolExecutor     工具执行器
     * @param injectionGuard   安全规则引擎
     * @param scenarioService  场景服务
     */
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

    /**
     * 运行一整套评测。
     * <p>
     * 业务含义：加载 suite → 逐条断言 → 汇总。
     * 边界：request.suite 为 null/空白时回退默认路径；加载失败抛 IllegalArgumentException（由 loadCases）。
     * </p>
     *
     * @param request 含 suite 路径；调用方应保证非 null（Controller 已兜底）
     * @return 含 traceId、suite、total/passed/failed、results 的完整响应
     * @throws IllegalArgumentException 当 classpath 无法加载套件时（来自 {@link #loadCases}）
     */
    public EvalRunResponse run(EvalRunRequest request) {
        // 套件路径：空白则用内置默认 month1-smoke
        String suite = request.getSuite() == null || request.getSuite().isBlank()
                ? "evals/month1-smoke.jsonl"
                : request.getSuite().trim();
        List<JsonNode> cases = loadCases(suite);
        EvalRunResponse response = new EvalRunResponse();
        response.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        response.setSuite(suite);
        List<EvalRunResponse.CaseResult> results = new ArrayList<>();
        int passed = 0;
        // 顺序执行：学习版不做并行，保证结果可复现、日志易读
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

    /**
     * 解析单条 JSON 用例并按 type 分发；捕获异常转为失败 CaseResult。
     *
     * @param node 一行 JSONL 解析得到的对象节点
     * @return 该条的通过/失败结果；永不抛出到 run 循环外
     */
    private EvalRunResponse.CaseResult evaluate(JsonNode node) {
        String id = text(node, "id", "unknown");
        // 缺省 type 按 chat 处理
        String type = text(node, "type", "chat");
        try {
            return switch (type) {
                case "rag" -> evalRag(id, type, node);
                case "security" -> evalSecurity(id, type, node);
                case "tool" -> evalTool(id, type, node);
                case "draft" -> evalDraft(id, type, node);
                case "chat" -> evalChat(id, type, node);
                case "scenario" -> evalScenario(id, type, node);
                // 未识别类型直接失败，避免静默跳过
                default -> new EvalRunResponse.CaseResult(id, type, false, "unknown type");
            };
        } catch (Exception e) {
            // 下游抛错不中断整套评测
            return new EvalRunResponse.CaseResult(id, type, false, "exception: " + e.getMessage());
        }
    }

    /**
     * RAG 用例：调用 {@link RagService#ask}，校验期望文档 id 片段与可选 needHuman。
     * <p>
     * 期望字段：
     * <ul>
     *   <li>{@code expectDocs}：数组，每个元素须被某条 source.docId 包含（substring）</li>
     *   <li>{@code expectNeedHuman}：仅当期望为 true 而实际未 needHuman 时判失败
     *       （注释说明 EMPTY gate 可能强制 needHuman，故期望 false 时不硬失败）</li>
     * </ul>
     * </p>
     *
     * @param id   用例 id
     * @param type 类型字符串（固定传入 "rag"）
     * @param node 用例 JSON
     * @return CaseResult
     */
    private EvalRunResponse.CaseResult evalRag(String id, String type, JsonNode node) {
        RagAskRequest req = new RagAskRequest();
        req.setQuestion(text(node, "question", ""));
        RagAskResponse resp = ragService.ask(req);
        boolean ok = true;
        StringBuilder detail = new StringBuilder();
        // 校验召回源是否覆盖期望 docId 子串
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
                // EMPTY gate 可能强制 needHuman；仅当期望 true 而实际未置位时硬失败
                // （期望 false 但因 gate 变成 true 时，冒烟场景下不据此失败）
                if (expect && (resp.getReply() == null || !resp.getReply().isNeedHuman())) {
                    ok = false;
                    detail.append("needHuman mismatch; ");
                }
            }
        }
        if (ok) {
            // 通过时写入观测信息，便于人工扫一眼召回规模与 gate
            detail.append("sources=").append(resp.getSources().size())
                    .append(" gate=").append(resp.getGate());
        }
        return new EvalRunResponse.CaseResult(id, type, ok, detail.toString().trim());
    }

    /**
     * 安全用例：对 question 跑 {@link InjectionGuard#inspect}，比对 blocked。
     * <p>
     * {@code expectBlocked} 缺省视为 true（即默认期望拦截）。
     * </p>
     *
     * @param id   用例 id
     * @param type 类型字符串
     * @param node 用例 JSON
     * @return CaseResult；detail 含 blocked 与 category
     */
    private EvalRunResponse.CaseResult evalSecurity(String id, String type, JsonNode node) {
        InjectionGuard.Verdict v = injectionGuard.inspect(text(node, "question", ""));
        // 无 expectBlocked 字段时默认期望拦截
        boolean expectBlocked = !node.has("expectBlocked") || node.get("expectBlocked").asBoolean();
        boolean ok = v.blocked() == expectBlocked;
        return new EvalRunResponse.CaseResult(id, type, ok,
                "blocked=" + v.blocked() + " category=" + v.category());
    }

    /**
     * 工具用例：按 toolName + args 调用 {@link ToolExecutor#run}，可选校验 ok 与 qty。
     * <p>
     * 期望字段：{@code expectOk}、{@code expectQty}（仅当 data 为 Map 且含 qty 数值时比较，容差 0.001）。
     * </p>
     *
     * @param id   用例 id
     * @param type 类型字符串
     * @param node 用例 JSON
     * @return CaseResult
     */
    private EvalRunResponse.CaseResult evalTool(String id, String type, JsonNode node) {
        String toolName = text(node, "toolName", "");
        @SuppressWarnings("unchecked")
        Map<String, Object> args = objectMapper.convertValue(node.get("args"), Map.class);
        // args 节点缺失或转换结果为 null 时用空 Map
        ToolResult result = toolExecutor.run(toolName, args == null ? Map.of() : args);
        boolean ok = true;
        if (node.has("expectOk") && result.ok() != node.get("expectOk").asBoolean()) {
            ok = false;
        }
        // 仅在仍通过且声明了 expectQty 时核对数量
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

    /**
     * 草稿用例：调用 {@link DraftService#draftPurchaseOrder}，校验字段/缺失列表/needHuman。
     * <p>
     * 期望字段：{@code expectFields}、{@code expectMissingContains}、{@code expectNeedHuman}。
     * </p>
     *
     * @param id   用例 id
     * @param type 类型字符串
     * @param node 用例 JSON
     * @return CaseResult
     */
    private EvalRunResponse.CaseResult evalDraft(String id, String type, JsonNode node) {
        DraftRequest req = new DraftRequest();
        req.setUtterance(text(node, "utterance", ""));
        DraftResponse draft = draftService.draftPurchaseOrder(req);
        boolean ok = true;
        StringBuilder detail = new StringBuilder();
        // 期望已抽出的字段键存在且值非空白
        if (node.has("expectFields") && node.get("expectFields").isArray()) {
            for (JsonNode f : node.get("expectFields")) {
                Object v = draft.getFields().get(f.asText());
                if (v == null || String.valueOf(v).isBlank()) {
                    ok = false;
                    detail.append("missing field ").append(f.asText()).append("; ");
                }
            }
        }
        // 期望 missing 列表中至少有一条包含指定子串
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

    /**
     * 聊天用例：调用 {@link ChatService#chat}，可选校验 needHuman 与 suggestedDocType 子串。
     *
     * @param id   用例 id
     * @param type 类型字符串
     * @param node 用例 JSON（question 字段）
     * @return CaseResult；detail 含 needHuman 与 docType
     */
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
            // docType 为 null 或不包含期望子串 → 失败
            if (doc == null || !doc.contains(needle)) {
                ok = false;
            }
        }
        return new EvalRunResponse.CaseResult(id, type, ok,
                "needHuman=" + resp.getReply().isNeedHuman()
                        + " docType=" + resp.getReply().getSuggestedDocType());
    }

    /**
     * 场景用例：固定 action=classify，比对 {@code expectMode}（空白则不做模式断言，视为通过）。
     *
     * @param id   用例 id
     * @param type 类型字符串
     * @param node 用例 JSON（question、可选 expectMode）
     * @return CaseResult；detail 含 mode 与 routedApi
     */
    private EvalRunResponse.CaseResult evalScenario(String id, String type, JsonNode node) {
        ScenarioRequest req = new ScenarioRequest();
        req.setUtterance(text(node, "question", ""));
        // 评测只验证分类，不触发 assist 下游
        req.setAction("classify");
        ScenarioResponse resp = scenarioService.handle(req);
        String expectMode = text(node, "expectMode", "");
        // expectMode 空白：不比对 mode，直接 ok
        boolean ok = expectMode.isBlank() || expectMode.equals(resp.getMode());
        return new EvalRunResponse.CaseResult(id, type, ok,
                "mode=" + resp.getMode() + " api=" + resp.getRoutedApi());
    }

    /**
     * 从 classpath 加载 JSONL 套件。
     * <p>
     * 按任意换行符拆分；跳过空行与 {@code #} 注释行；每行解析为一个 JsonNode。
     * </p>
     *
     * @param suite classpath 相对路径
     * @return 用例节点列表（可能为空，若文件仅有注释）
     * @throws IllegalArgumentException 资源不存在或解析失败时包装原因抛出
     */
    private List<JsonNode> loadCases(String suite) {
        try {
            ClassPathResource resource = new ClassPathResource(suite);
            String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            List<JsonNode> list = new ArrayList<>();
            // \\R 匹配 Unicode 换行，兼容 CRLF/LF
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

    /**
     * 安全读取 JSON 文本字段；缺失或 null 节点时返回默认值。
     *
     * @param node         用例节点；可为 null
     * @param field        字段名
     * @param defaultValue 缺省文本
     * @return 字段文本或 defaultValue
     */
    private static String text(JsonNode node, String field, String defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asText();
    }
}
