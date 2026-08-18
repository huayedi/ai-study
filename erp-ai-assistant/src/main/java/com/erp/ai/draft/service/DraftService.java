package com.erp.ai.draft.service;

import com.erp.ai.common.client.LlmClient;
import com.erp.ai.common.client.LlmResult;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.common.observability.AiCallLog;
import com.erp.ai.draft.dto.DraftRequest;
import com.erp.ai.draft.dto.DraftResponse;
import com.erp.ai.draft.prompt.DraftPromptLoader;
import com.erp.ai.common.observability.PromptVersions;
import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.ToolTrace;
import com.erp.ai.tool.service.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Day20：采购订单草稿辅助编排服务（draft 域核心）。
 * <p>
 * <b>职责</b>：把用户自然语言 {@code utterance} 转为结构化采购订单字段建议；
 * 含 LLM 调用、JSON 解析重试、相对日期规则、可选 queryItem 主数据校验、
 * 必填字段检查，以及审计日志与观测字段填充。
 * <p>
 * <b>在系统中的位置</b>：draft 域中枢；上游 {@link com.erp.ai.draft.controller.DraftController}，
 * 下游协作 {@link DraftPromptLoader}、{@link LlmClient}、{@link DraftParser}、
 * {@link ToolExecutor}（仅只读 queryItem）、{@link AiCallLog}。
 * <p>
 * <b>对应学习 Day</b>：Day20 草稿辅助；Day22 {@code promptVersion}。
 * <p>
 * <b>调用链（成功主路径）</b>：
 * <pre>
 * DraftController
 *   → 组装 [system(草稿提示), user(utterance)]（无历史、无 session）
 *   → LlmClient.chat → DraftParser.parse
 *   → applyRelativeDateRules → validateItemWithTool → enforceRequiredAndHuman
 *   → AiCallLog.success → 填满 DraftResponse 返回
 * </pre>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li><b>不写业务库</b>：无订单 insert；工具只读 queryItem 校验存货是否存在于学习假数据</li>
 *   <li><b>无 session</b>：审计用伪 sessionTag {@code draft/po/{trace前8位}}，不落 chat 表</li>
 *   <li>{@link #enforceRequiredAndHuman} 结束时强制 {@code needHuman=true}（建议 ≠ 过账）</li>
 *   <li>解析失败带 schema repair hint 重试，耗尽抛 {@link IllegalStateException}</li>
 * </ul>
 *
 * @see com.erp.ai.draft.controller.DraftController
 * @see DraftParser
 * @see DraftPromptLoader
 */
@Service
public class DraftService {

    /** 结构化日志：记录字段键、missing、warnings，便于手测对照 */
    private static final Logger log = LoggerFactory.getLogger(DraftService.class);

    /**
     * 建议校验的关键字段；缺则进入 missing。
     * 与提示词 schema 对齐：缺任一关键项即认为草稿不完整。
     */
    private static final List<String> REQUIRED_FIELDS = List.of(
            "供应商", "存货编码", "数量", "仓库", "含税单价", "交货日期"
    );

    /** LLM 客户端 */
    private final LlmClient llmClient;

    /** 草稿专用 system 提示加载器 */
    private final DraftPromptLoader draftPromptLoader;

    /** 模型 JSON → DraftResponse 业务字段 */
    private final DraftParser draftParser;

    /** 只读工具执行器：本服务仅用于 queryItem 后置校验 */
    private final ToolExecutor toolExecutor;

    /** AI 配置：重试、模型、单价等 */
    private final AiProperties properties;

    /** 调用成功/失败审计 */
    private final AiCallLog aiCallLog;

    /**
     * 构造注入协作组件。
     *
     * @param llmClient          LLM 客户端
     * @param draftPromptLoader  草稿提示词
     * @param draftParser        输出解析器
     * @param toolExecutor       工具执行器
     * @param properties         AI 配置
     * @param aiCallLog          审计日志
     */
    public DraftService(LlmClient llmClient,
                        DraftPromptLoader draftPromptLoader,
                        DraftParser draftParser,
                        ToolExecutor toolExecutor,
                        AiProperties properties,
                        AiCallLog aiCallLog) {
        this.llmClient = llmClient;
        this.draftPromptLoader = draftPromptLoader;
        this.draftParser = draftParser;
        this.toolExecutor = toolExecutor;
        this.properties = properties;
        this.aiCallLog = aiCallLog;
    }

    /**
     * 生成采购订单草稿建议（主入口）。
     * <p>
     * 业务含义：单轮、无会话；输出仅供人工确认，不创建真实采购订单。
     * <p>
     * 边界：
     * <ul>
     *   <li>utterance 为 null 时按空串处理（正常路径入站已 NotBlank）</li>
     *   <li>重试耗尽抛 {@link IllegalStateException}，消息含 traceId</li>
     * </ul>
     *
     * @param request 草稿入参（utterance）
     * @return 完整草稿响应（含观测字段）
     * @throws IllegalStateException 多次尝试后仍失败时
     */
    public DraftResponse draftPurchaseOrder(DraftRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        // 审计日志需要 session 维度：用 draft 前缀伪标签，明确非 chat 会话
        String sessionTag = "draft/po/" + traceId.substring(0, 8);
        long started = System.currentTimeMillis();

        String utterance = request.getUtterance() == null ? "" : request.getUtterance().trim();
        // 固定两轮消息：无历史，杜绝 Chat session 污染
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", draftPromptLoader.load()),
                new ChatMessage("user", utterance)
        );

        int maxAttempts = Math.max(1, properties.getMaxRetries() + 1);
        int attempts = 0;
        RuntimeException lastError = null;
        int promptTokens = 0;
        int completionTokens = 0;
        String modelName = properties.getModel();
        // working 可在失败后追加 repair hint；初始 messages 为不可变 List.of
        List<ChatMessage> working = new ArrayList<>(messages);

        while (attempts < maxAttempts) {
            attempts++;
            try {
                LlmResult result = llmClient.chat(working);
                promptTokens += result.getPromptTokens();
                completionTokens += result.getCompletionTokens();
                modelName = result.getModel();

                DraftResponse draft = draftParser.parse(result.getContent());
                // 规则层补「下周一」等相对日期，减少模型胡编日期
                applyRelativeDateRules(utterance, draft);
                // 有存货编码则查学习假主数据，不存在则 missing + warning
                List<ToolTrace> traces = validateItemWithTool(draft);
                // 必填检查 + 强制 needHuman
                enforceRequiredAndHuman(draft);

                log.info("draft_po utteranceLen={} fields={} missing={} warnings={} needHuman={}",
                        utterance.length(),
                        draft.getFields().keySet(),
                        draft.getMissing(),
                        draft.getWarnings(),
                        draft.isNeedHuman());

                double cost = estimateCost(promptTokens, completionTokens);
                long latency = System.currentTimeMillis() - started;
                aiCallLog.success(
                        traceId, sessionTag, llmClient.providerName(), modelName,
                        latency, attempts, promptTokens, completionTokens, cost
                );

                draft.setTraceId(traceId);
                draft.setProvider(llmClient.providerName());
                draft.setModel(modelName);
                draft.setLatencyMs(latency);
                draft.setAttempts(attempts);
                draft.setToolTraces(traces);
                draft.setPromptVersion(PromptVersions.of("draft-po", draftPromptLoader.load()));

                DraftResponse.Usage usage = new DraftResponse.Usage();
                usage.setPromptTokens(promptTokens);
                usage.setCompletionTokens(completionTokens);
                usage.setTotalTokens(promptTokens + completionTokens);
                usage.setEstimatedCostUsd(cost);
                draft.setUsage(usage);
                return draft;
            } catch (RuntimeException ex) {
                lastError = ex;
                working = withRepairHint(working, ex.getMessage());
            }
        }

        long latency = System.currentTimeMillis() - started;
        String reason = lastError == null ? "unknown" : lastError.getMessage();
        aiCallLog.failure(traceId, sessionTag, llmClient.providerName(), latency, attempts, reason);
        throw new IllegalStateException("草稿辅助失败(traceId=" + traceId + "): " + reason, lastError);
    }

    /**
     * 若草稿已填存货编码，则调用只读工具 {@code queryItem} 做学习假主数据存在性校验。
     * <p>
     * 为何后置而不是 tool_calls 循环：草稿路径保持「单次 chat + 确定性校验」，
     * 与 Chat Day18 路径 A 解耦，降低草稿场景复杂度。
     * <p>
     * 边界：编码缺失则跳过（返回空轨迹）；查不到则补 missing/warning 并压低 confidence。
     *
     * @param draft 已解析的草稿（会被就地修改）
     * @return 0 或 1 条工具轨迹
     */
    private List<ToolTrace> validateItemWithTool(DraftResponse draft) {
        Object codeObj = firstPresent(draft.getFields(), "存货编码", "物料编码", "itemCode");
        if (codeObj == null || !StringUtils.hasText(String.valueOf(codeObj))) {
            return List.of();
        }
        String itemCode = String.valueOf(codeObj).trim();
        ToolResult result = toolExecutor.run("queryItem", Map.of("itemCode", itemCode));
        ToolTrace trace = ToolTrace.from(result);
        if (!result.ok()) {
            addMissing(draft, "存货编码(主数据不存在)");
            addWarning(draft, "queryItem 未找到存货编码: " + itemCode + "（学习假数据）");
            draft.setNeedHuman(true);
            // 主数据都对不上，置信度不应虚高
            if (draft.getConfidence() == null || draft.getConfidence() > 0.5) {
                draft.setConfidence(0.45);
            }
        } else if (result.data() instanceof Map<?, ?> data) {
            Object name = data.get("name");
            if (name != null) {
                // putIfAbsent：不覆盖模型已给的存货名称
                draft.getFields().putIfAbsent("存货名称", name);
            }
        }
        return List.of(trace);
    }

    /**
     * 相对日期规则：当交货日期空白且用户话术含「下周一」时，用服务器本地日期换算下一周一。
     * <p>
     * 为何用规则而非全信模型：相对日期易算错；规则结果仍加 warning，强调以人工确认为准。
     *
     * @param utterance 用户原文
     * @param draft     草稿（就地改 fields / warnings / confidence）
     */
    private void applyRelativeDateRules(String utterance, DraftResponse draft) {
        Map<String, Object> fields = draft.getFields();
        if (fields == null) {
            fields = new LinkedHashMap<>();
            draft.setFields(fields);
        }
        Object date = fields.get("交货日期");
        boolean blankDate = date == null || !StringUtils.hasText(String.valueOf(date))
                || "null".equalsIgnoreCase(String.valueOf(date));
        if (blankDate && utterance != null && utterance.contains("下周一")) {
            // next(MONDAY)：若今天已是周一，则落到「下一个」周一，符合「下周一」口语
            LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            fields.put("交货日期", nextMonday.format(DateTimeFormatter.ISO_LOCAL_DATE));
            addWarning(draft, "交货日期由规则换算「下周一」→ " + fields.get("交货日期") + "（学习环境，以人工确认为准）");
            if (draft.getConfidence() == null) {
                draft.setConfidence(0.65);
            }
        }
    }

    /**
     * 扫描必填字段写入 missing，并最终强制 needHuman=true。
     * <p>
     * 为何最后仍强制人工：产品语义是「建议」，防止前端误把 needHuman=false 当可自动过账。
     *
     * @param draft 草稿（就地修改）
     */
    private void enforceRequiredAndHuman(DraftResponse draft) {
        Map<String, Object> fields = draft.getFields() == null ? Map.of() : draft.getFields();
        for (String key : REQUIRED_FIELDS) {
            Object v = fields.get(key);
            if (isBlank(v)) {
                addMissing(draft, key);
            }
        }
        if (!draft.getMissing().isEmpty()) {
            draft.setNeedHuman(true);
        }
        // 草稿默认人工确认：建议 ≠ 过账
        draft.setNeedHuman(true);
        if (!StringUtils.hasText(draft.getSuggestedDocType())) {
            draft.setSuggestedDocType("采购订单");
        }
    }

    /**
     * 按候选键顺序取第一个非空白字段值（兼容中英键名）。
     *
     * @param fields 字段 Map，可为 null
     * @param keys   候选键，如 存货编码 / itemCode
     * @return 第一个命中值；皆无则 null
     */
    private static Object firstPresent(Map<String, Object> fields, String... keys) {
        if (fields == null) {
            return null;
        }
        for (String key : keys) {
            if (fields.containsKey(key) && !isBlank(fields.get(key))) {
                return fields.get(key);
            }
        }
        return null;
    }

    /**
     * 判断字段值是否视为空白：null、trim 后空串、或字面量 {@code "null"}。
     *
     * @param v 字段值
     * @return true 表示缺失/无效
     */
    private static boolean isBlank(Object v) {
        if (v == null) {
            return true;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equalsIgnoreCase(s);
    }

    /**
     * 向 missing 追加一项（去重）；若列表为 null 则先创建。
     *
     * @param draft 草稿
     * @param item  缺失项文案
     */
    private static void addMissing(DraftResponse draft, String item) {
        if (draft.getMissing() == null) {
            draft.setMissing(new ArrayList<>());
        }
        if (!draft.getMissing().contains(item)) {
            draft.getMissing().add(item);
        }
    }

    /**
     * 向 warnings 追加一项（去重）；若列表为 null 则先创建。
     *
     * @param draft 草稿
     * @param item  警告文案
     */
    private static void addWarning(DraftResponse draft, String item) {
        if (draft.getWarnings() == null) {
            draft.setWarnings(new ArrayList<>());
        }
        if (!draft.getWarnings().contains(item)) {
            draft.getWarnings().add(item);
        }
    }

    /**
     * 追加草稿 JSON schema 修复提示，供下一轮重试。
     *
     * @param messages 当前工作消息列表
     * @param reason   失败原因
     * @return 带 repair user 消息的新列表
     */
    private List<ChatMessage> withRepairHint(List<ChatMessage> messages, String reason) {
        List<ChatMessage> copy = new ArrayList<>(messages);
        copy.add(new ChatMessage("user",
                "上一次输出不符合草稿 JSON schema（" + reason + "）。请只输出合法 JSON："
                        + "suggested_doc_type/fields/missing/need_human/warnings/confidence；未知字段放 missing，禁止编造单价与仓库。"));
        return copy;
    }

    /**
     * 按配置单价估算费用（美元）；不做额外四舍五入（与 ChatService 的 round 策略略有不同，保持原实现）。
     *
     * @param promptTokens     输入 token
     * @param completionTokens 输出 token
     * @return 估算美元费用
     */
    private double estimateCost(int promptTokens, int completionTokens) {
        return (promptTokens / 1000.0) * properties.getPriceInputPer1k()
                + (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
    }
}
