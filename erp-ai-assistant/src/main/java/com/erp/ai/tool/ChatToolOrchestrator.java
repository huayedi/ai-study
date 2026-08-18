package com.erp.ai.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Day18 路径 B：规则编排 — 先只读查工具，再把结果塞进 messages。
 * <p>
 * 默认已切路径 A（tool_calls）；本类在 {@code ai.tool.chat-path=rule} 时启用。
 * 无写库存工具；写意图一律拦截。
 */
@Component
public class ChatToolOrchestrator {

    public static final String TOOL_RESULT_MARKER = "【系统只读查询结果】";
    public static final String WRITE_BLOCK_MARKER = "【系统写操作拦截】";

    private static final Pattern ITEM_CODE = Pattern.compile(
            "(?i)\\b(ITEM-[A-Z0-9]+|A\\d{3})\\b");
    private static final Pattern PERIOD = Pattern.compile("(20\\d{2}-\\d{2})");

    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    public ChatToolOrchestrator(ToolExecutor toolExecutor, ObjectMapper objectMapper) {
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据用户话术决定是否预查工具，并生成附加消息与轨迹。
     */
    public AugmentResult augment(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return AugmentResult.none();
        }
        String text = userMessage.trim();

        if (isWriteIntent(text)) {
            String injection = WRITE_BLOCK_MARKER
                    + "\n本助手只提供只读查询工具（queryItem/queryInventory/queryPeriodStatus），"
                    + "没有改库存/过账/删单/付款工具。"
                    + "\n请人工在 ERP 中操作；回答时 need_human 必须为 true；禁止声称已改账或已写入。";
            return new AugmentResult(true, true, List.of(injection), List.of());
        }

        List<ToolTrace> traces = new ArrayList<>();
        List<String> injections = new ArrayList<>();

        if (needsInventory(text)) {
            Map<String, Object> args = new LinkedHashMap<>();
            guessItemCode(text).ifPresent(c -> args.put("itemCode", c));
            guessWarehouse(text).ifPresent(w -> args.put("warehouse", w));
            ToolResult result = toolExecutor.run("queryInventory", args);
            traces.add(ToolTrace.from(result));
            injections.add(formatToolInjection("queryInventory", args, result));
        } else if (needsItem(text)) {
            Map<String, Object> args = new LinkedHashMap<>();
            guessItemCode(text).ifPresent(c -> args.put("itemCode", c));
            ToolResult result = toolExecutor.run("queryItem", args);
            traces.add(ToolTrace.from(result));
            injections.add(formatToolInjection("queryItem", args, result));
        } else if (needsPeriod(text)) {
            Map<String, Object> args = new LinkedHashMap<>();
            args.put("company", guessCompany(text));
            guessPeriod(text).ifPresentOrElse(
                    p -> args.put("period", p),
                    () -> args.put("period", "2026-08")
            );
            ToolResult result = toolExecutor.run("queryPeriodStatus", args);
            traces.add(ToolTrace.from(result));
            injections.add(formatToolInjection("queryPeriodStatus", args, result));
        }

        if (injections.isEmpty()) {
            return AugmentResult.none();
        }
        boolean forceHuman = traces.stream().anyMatch(t -> !t.ok());
        return new AugmentResult(false, forceHuman, injections, traces);
    }

    private String formatToolInjection(String toolName, Map<String, Object> args, ToolResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(TOOL_RESULT_MARKER).append('\n');
        sb.append("tool=").append(toolName).append('\n');
        sb.append("args=").append(toJson(args)).append('\n');
        sb.append("ok=").append(result.ok()).append('\n');
        if (result.ok()) {
            sb.append("data=").append(toJson(result.data())).append('\n');
            sb.append("请严格基于上述 data 回答；不要编造其它仓库/期间/存货数量。");
        } else {
            sb.append("error=").append(result.error()).append('\n');
            sb.append("工具失败：禁止编造具体数量或状态；说明缺参或未查到，并设 need_human=true。");
        }
        return sb.toString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    public static boolean isWriteIntent(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t,
                "改成", "改库存", "调库存", "写成", "调成",
                "过账", "删单", "删除单据", "付款", "关闭期间", "重开期间",
                "writeinventory", "updateinventory", "帮我改");
    }

    static boolean needsInventory(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t, "库存", "现存量", "有多少货", "还剩多少")
                || (containsAny(t, "多少") && containsAny(t, "仓"));
    }

    static boolean needsItem(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t, "存货", "规格", "物料名称", "叫什么名")
                && guessItemCode(text).isPresent();
    }

    static boolean needsPeriod(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t, "期间", "关账", "是否打开", "期间状态");
    }

    static java.util.Optional<String> guessItemCode(String text) {
        Matcher m = ITEM_CODE.matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1).toUpperCase(Locale.ROOT));
        }
        return java.util.Optional.empty();
    }

    static java.util.Optional<String> guessWarehouse(String text) {
        if (text.contains("原料仓")) {
            return java.util.Optional.of("原料仓");
        }
        if (text.contains("成品仓")) {
            return java.util.Optional.of("成品仓");
        }
        return java.util.Optional.empty();
    }

    static java.util.Optional<String> guessPeriod(String text) {
        Matcher m = PERIOD.matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1));
        }
        return java.util.Optional.empty();
    }

    static String guessCompany(String text) {
        if (text.contains("主公司")) {
            return "主公司";
        }
        return "主公司";
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k.toLowerCase(Locale.ROOT)) || text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param writeBlocked 写意图已拦截（无写工具）
     * @param forceNeedHuman 建议强制 need_human
     * @param injectionMessages 追加到 messages 的 user 附加段
     * @param traces 工具轨迹
     */
    public record AugmentResult(
            boolean writeBlocked,
            boolean forceNeedHuman,
            List<String> injectionMessages,
            List<ToolTrace> traces
    ) {
        public static AugmentResult none() {
            return new AugmentResult(false, false, List.of(), List.of());
        }

        public boolean hasInjections() {
            return injectionMessages != null && !injectionMessages.isEmpty();
        }
    }
}
