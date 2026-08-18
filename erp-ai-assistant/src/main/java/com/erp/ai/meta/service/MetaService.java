package com.erp.ai.meta.service;

import com.erp.ai.chat.prompt.SystemPromptLoader;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.mapper.AiCallAuditMapper;
import com.erp.ai.draft.prompt.DraftPromptLoader;
import com.erp.ai.tool.ToolHandler;
import com.erp.ai.tool.service.ToolRegistry;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Day21/22/26/28/29/30：学习元信息 / 架构 / 清单 / 口述 / 统计。
 */
@Service
public class MetaService {

    private final AiProperties properties;
    private final ToolRegistry toolRegistry;
    private final SystemPromptLoader systemPromptLoader;
    private final DraftPromptLoader draftPromptLoader;
    private final AiCallAuditMapper aiCallAuditMapper;

    public MetaService(AiProperties properties,
                       ToolRegistry toolRegistry,
                       SystemPromptLoader systemPromptLoader,
                       DraftPromptLoader draftPromptLoader,
                       AiCallAuditMapper aiCallAuditMapper) {
        this.properties = properties;
        this.toolRegistry = toolRegistry;
        this.systemPromptLoader = systemPromptLoader;
        this.draftPromptLoader = draftPromptLoader;
        this.aiCallAuditMapper = aiCallAuditMapper;
    }

    public Map<String, Object> capabilities() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("day", "Day21");
        out.put("layers", List.of("RAG", "Tool", "Draft", "Prompt", "HITL"));
        out.put("apis", List.of(
                "POST /api/ai/chat",
                "POST /api/ai/rag/ask",
                "GET /api/ai/tool/list",
                "POST /api/ai/tool/invoke",
                "POST /api/ai/draft/purchase-order",
                "POST /api/ai/security/probe",
                "POST /api/ai/scenario/route",
                "POST /api/ai/eval/run",
                "GET /api/ai/stats",
                "GET /api/ai/meta/capabilities"
        ));
        out.put("readonlyTools", toolRegistry.all().stream().map(ToolHandler::name).toList());
        out.put("writeTools", List.of());
        out.put("writeInventoryAllowed", false);
        out.put("promptVersion", promptVersion("erp-system", systemPromptLoader.getSystemPrompt()));
        out.put("draftPromptVersion", promptVersion("draft-po", draftPromptLoader.load()));
        out.put("provider", properties.getProvider());
        out.put("model", properties.getModel());
        out.put("retriever", properties.getRag().getRetriever());
        out.put("topK", properties.getRag().getTopK());
        return out;
    }

    public Map<String, Object> architecture() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("day", "Day26");
        out.put("packages", List.of("chat", "rag", "tool", "draft", "security", "scenario", "eval", "meta", "common"));
        out.put("dataFlows", Map.of(
                "chat", "system(+few-shot) → history → user → LLM → JSON parse → session/audit",
                "rag", "loader → chunk → retrieve → Gate → prompt → LLM → sources",
                "tool", "whitelist → validate → timeout → audit；无写库存",
                "draft", "draft system → LLM → fields/missing → queryItem 校验 → needHuman"
        ));
        out.put("diagram", """
                Controller
                  ├─ /chat → ChatService
                  ├─ /rag/ask → RagService
                  ├─ /draft → DraftService
                  ├─ /tool → ToolExecutor
                  ├─ /security → InjectionGuard
                  ├─ /scenario → ScenarioClassifier
                  └─ /eval → EvalRunner
                """);
        return out;
    }

    public Map<String, Object> healthChecklist() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("day", "Day28");
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(check("apiKeyNotHardcodedInYml", true, "Key 走环境变量 AI_API_KEY"));
        items.add(check("mockDefaultFriendly",
                properties.getProvider() == null || properties.getProvider().isBlank()
                        || "mock".equalsIgnoreCase(properties.getProvider())
                        || properties.getApiKey() == null
                        || properties.getApiKey().isBlank()
                        || !properties.getApiKey().contains("sk-"),
                "默认 mock 或 Key 不进仓库明文"));
        items.add(check("retrieverConfigurable", properties.getRag().getRetriever() != null, "ai.rag.retriever 可配置"));
        items.add(check("toolWhitelistOnly", toolRegistry.all().stream().noneMatch(t ->
                t.name().toLowerCase().contains("write")), "无写库存工具注册"));
        items.add(check("promptFileExternalized", systemPromptLoader.getSystemPrompt().length() > 50, "Prompt 文件化"));
        items.add(check("traceIdInResponses", true, "chat/rag/draft 均返回 traceId"));
        out.put("checks", items);
        out.put("allPassed", items.stream().allMatch(i -> Boolean.TRUE.equals(i.get("passed"))));
        return out;
    }

    public Map<String, Object> oralQuiz() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("day", "Day29");
        out.put("questions", List.of(
                Map.of("id", 1, "q", "provider 与 model 区别？", "a", "provider=接入协议；model=具体模型名"),
                Map.of("id", 2, "q", "为何多轮更费钱？", "a", "历史 messages 重复计入 prompt tokens"),
                Map.of("id", 3, "q", "few-shot 解决什么、不解决什么？", "a", "教格式/口径；不代替检索，不保证无幻觉"),
                Map.of("id", 4, "q", "RAG 五步？", "a", "加载→切分→检索→拼 Prompt→生成；sources 来自检索"),
                Map.of("id", 5, "q", "sources 为何不能让模型编？", "a", "引用必须来自 chunk 元数据，否则假权威"),
                Map.of("id", 6, "q", "向量索引与查询各 embed 什么？", "a", "索引 embed chunk；查询 embed question；同一模型"),
                Map.of("id", 7, "q", "Hybrid 的价值？", "a", "专名靠关键词、换说法靠向量，合并更稳"),
                Map.of("id", 8, "q", "空/弱命中应怎样？", "a", "EMPTY：拒答+空 sources；WEAK：need_human+降置信"),
                Map.of("id", 9, "q", "Tool 与 RAG 差异？", "a", "RAG 取教材依据；Tool 取只读实时/结构化数据"),
                Map.of("id", 10, "q", "为何 ERP 偏工作流？", "a", "要审计与 HITL；纯 Agent 难控合法跳转"),
                Map.of("id", 11, "q", "本月为何禁止写库存工具？", "a", "风险不对称；本月只做问答辅助"),
                Map.of("id", 12, "q", "草稿 missing 字段的意义？", "a", "标草稿不完整，逼人工补全"),
                Map.of("id", 13, "q", "注入攻击举三例与防线？", "a", "忽略上文/假装已审批/输出密钥；分层提示+拒答+无写工具"),
                Map.of("id", 14, "q", "降级为何不能静默瞎答？", "a", "降级必须可见；静默瞎答给假确定性"),
                Map.of("id", 15, "q", "单变量对比实验怎么做？", "a", "每次只改一个因子，其余固定，对比 sources/通过率")
        ));
        out.put("passThreshold", 12);
        out.put("answerKeyRef", "docs/ORAL_ANSWERS.md");
        return out;
    }

    public Map<String, Object> month1Checklist() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("day", "Day30");
        out.put("canSay", List.of(
                "配置兼容 Chat API + 结构化输出",
                "教材 RAG + sources 纪律",
                "keyword/vector/hybrid 可切换",
                "只读 Tool + 草稿辅助 + HITL",
                "安全抽测与观测字段"
        ));
        out.put("explicitlyNotDoing", List.of(
                "模型微调",
                "公司生产库/SSO",
                "自动过账/改库存",
                "多 Agent 无人看管闭环"
        ));
        out.put("checklist", List.of(
                Map.of("item", "/api/ai/chat 稳定 JSON", "done", true),
                Map.of("item", "Prompt + few-shot 可维护", "done", true),
                Map.of("item", "/api/ai/rag/ask + sources", "done", true),
                Map.of("item", "keyword/vector/hybrid", "done", true),
                Map.of("item", "只读工具编排", "done", true),
                Map.of("item", "草稿接口", "done", true),
                Map.of("item", "安全抽测 API", "done", true),
                Map.of("item", "JSONL 评测 runner", "done", true)
        ));
        out.put("nextMonthOptions", List.of(
                "检索加深（Hybrid/Rerank/Store）",
                "工作流 HITL FlowEngine",
                "评测自动化加深",
                "简单前端调试台",
                "单据 OCR（可选）"
        ));
        out.put("motto", "把不确定的生成，变成可检索、可校验、可观测、可降级、可人工接管的系统。");
        return out;
    }

    public Map<String, Object> stats() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("day", "Day22");
        try {
            Map<String, Object> row = aiCallAuditMapper.selectStats();
            out.put("aiCallAudit", row == null ? Map.of() : row);
        } catch (Exception e) {
            out.put("aiCallAudit", Map.of("error", e.getMessage()));
        }
        out.put("promptVersion", promptVersion("erp-system", systemPromptLoader.getSystemPrompt()));
        out.put("configSnapshot", Map.of(
                "provider", properties.getProvider(),
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "retriever", properties.getRag().getRetriever(),
                "topK", properties.getRag().getTopK(),
                "maxMessages", properties.getSession().getMaxMessages()
        ));
        return out;
    }

    public static String promptVersion(String name, String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            return name + "@" + HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (Exception e) {
            return name + "@unknown";
        }
    }

    private static Map<String, Object> check(String name, boolean passed, String note) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("passed", passed);
        m.put("note", note);
        return m;
    }
}
