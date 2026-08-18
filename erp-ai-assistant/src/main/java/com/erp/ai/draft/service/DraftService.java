package com.erp.ai.draft.service;

import com.erp.ai.common.client.LlmClient;
import com.erp.ai.common.client.LlmResult;
import com.erp.ai.common.config.AiProperties;
import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.common.observability.AiCallLog;
import com.erp.ai.draft.dto.DraftRequest;
import com.erp.ai.draft.dto.DraftResponse;
import com.erp.ai.draft.prompt.DraftPromptLoader;
import com.erp.ai.meta.service.MetaService;
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
 * Day20：采购订单草稿辅助 — 自然语言 → 结构化建议；不写业务库。
 */
@Service
public class DraftService {

    private static final Logger log = LoggerFactory.getLogger(DraftService.class);

    /** 建议校验的关键字段；缺则进 missing，并强制 needHuman */
    private static final List<String> REQUIRED_FIELDS = List.of(
            "供应商", "存货编码", "数量", "仓库", "含税单价", "交货日期"
    );

    private final LlmClient llmClient;
    private final DraftPromptLoader draftPromptLoader;
    private final DraftParser draftParser;
    private final ToolExecutor toolExecutor;
    private final AiProperties properties;
    private final AiCallLog aiCallLog;

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

    public DraftResponse draftPurchaseOrder(DraftRequest request) {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        String sessionTag = "draft/po/" + traceId.substring(0, 8);
        long started = System.currentTimeMillis();

        String utterance = request.getUtterance() == null ? "" : request.getUtterance().trim();
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
        List<ChatMessage> working = new ArrayList<>(messages);

        while (attempts < maxAttempts) {
            attempts++;
            try {
                LlmResult result = llmClient.chat(working);
                promptTokens += result.getPromptTokens();
                completionTokens += result.getCompletionTokens();
                modelName = result.getModel();

                DraftResponse draft = draftParser.parse(result.getContent());
                applyRelativeDateRules(utterance, draft);
                List<ToolTrace> traces = validateItemWithTool(draft);
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
                draft.setPromptVersion(MetaService.promptVersion("draft-po", draftPromptLoader.load()));

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
            if (draft.getConfidence() == null || draft.getConfidence() > 0.5) {
                draft.setConfidence(0.45);
            }
        } else if (result.data() instanceof Map<?, ?> data) {
            Object name = data.get("name");
            if (name != null) {
                draft.getFields().putIfAbsent("存货名称", name);
            }
        }
        return List.of(trace);
    }

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
            LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            fields.put("交货日期", nextMonday.format(DateTimeFormatter.ISO_LOCAL_DATE));
            addWarning(draft, "交货日期由规则换算「下周一」→ " + fields.get("交货日期") + "（学习环境，以人工确认为准）");
            if (draft.getConfidence() == null) {
                draft.setConfidence(0.65);
            }
        }
    }

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

    private static boolean isBlank(Object v) {
        if (v == null) {
            return true;
        }
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equalsIgnoreCase(s);
    }

    private static void addMissing(DraftResponse draft, String item) {
        if (draft.getMissing() == null) {
            draft.setMissing(new ArrayList<>());
        }
        if (!draft.getMissing().contains(item)) {
            draft.getMissing().add(item);
        }
    }

    private static void addWarning(DraftResponse draft, String item) {
        if (draft.getWarnings() == null) {
            draft.setWarnings(new ArrayList<>());
        }
        if (!draft.getWarnings().contains(item)) {
            draft.getWarnings().add(item);
        }
    }

    private List<ChatMessage> withRepairHint(List<ChatMessage> messages, String reason) {
        List<ChatMessage> copy = new ArrayList<>(messages);
        copy.add(new ChatMessage("user",
                "上一次输出不符合草稿 JSON schema（" + reason + "）。请只输出合法 JSON："
                        + "suggested_doc_type/fields/missing/need_human/warnings/confidence；未知字段放 missing，禁止编造单价与仓库。"));
        return copy;
    }

    private double estimateCost(int promptTokens, int completionTokens) {
        return (promptTokens / 1000.0) * properties.getPriceInputPer1k()
                + (completionTokens / 1000.0) * properties.getPriceOutputPer1k();
    }
}
