package com.erp.ai.tool.service;

import com.erp.ai.tool.ToolResult;
import com.erp.ai.tool.ToolTrace;
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
 * <b>职责</b>：根据用户话术识别写意图 / 查询意图，预调白名单只读工具，生成注入消息与 {@link ToolTrace}。
 * 默认生产已切路径 A（模型 tool_calls）；本类在 {@code ai.tool.chat-path=rule} 时启用。
 * <p>
 * <b>为何没有写工具</b>：写意图一律 {@link #isWriteIntent(String)} 拦截并注入
 * {@link #WRITE_BLOCK_MARKER}，要求回答 need_human=true，禁止声称已改账。
 * 不注册写工具是为了：学习助手不能改库存/过账/删单/付款/开关期间，避免误操作。
 * <p>
 * <b>Day18 路径</b>：路径 B 专用；路径 A 不走本类预查。仍通过 {@link ToolExecutor} 执行只读三工具。
 * <p>
 * <b>上下游</b>：上游 Chat 服务在调模型前调用 {@link #augment(String)}；
 * 下游 ToolExecutor → Handler → 学习库。
 */
@Component
public class ChatToolOrchestrator {

    /** 注入到 messages 的只读查询结果段标题 */
    public static final String TOOL_RESULT_MARKER = "【系统只读查询结果】";
    /** 写意图拦截段标题 */
    public static final String WRITE_BLOCK_MARKER = "【系统写操作拦截】";

    /** 从话术中抓存货编码：ITEM-xxx 或 A### */
    private static final Pattern ITEM_CODE = Pattern.compile(
            "(?i)\\b(ITEM-[A-Z0-9]+|A\\d{3})\\b");
    /** 从话术中抓会计期间 yyyy-MM（20xx-xx） */
    private static final Pattern PERIOD = Pattern.compile("(20\\d{2}-\\d{2})");

    /** 统一执行器（含禁写/超时/审计） */
    private final ToolExecutor toolExecutor;
    /** 将 args/data 序列化为注入文本中的 JSON */
    private final ObjectMapper objectMapper;

    /**
     * @param toolExecutor 工具执行器
     * @param objectMapper JSON 序列化
     */
    public ChatToolOrchestrator(ToolExecutor toolExecutor, ObjectMapper objectMapper) {
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据用户话术决定是否预查工具，并生成附加消息与轨迹。
     * <p>
     * <b>校验</b>：空白消息直接 {@link AugmentResult#none()}；写意图不调任何工具。
     * <b>超时</b>：预查仍受 ToolExecutor 超时约束；失败轨迹会触发 forceNeedHuman。
     *
     * @param userMessage 用户原始输入
     * @return 编排结果（可能无注入）
     */
    public AugmentResult augment(String userMessage) {
        // —— 空输入：无需编排 ——
        if (userMessage == null || userMessage.isBlank()) {
            return AugmentResult.none();
        }
        String text = userMessage.trim();

        // —— 写意图：硬拦，不调用任何工具（系统无写工具） ——
        if (isWriteIntent(text)) {
            String injection = WRITE_BLOCK_MARKER
                    + "\n本助手只提供只读查询工具（queryItem/queryInventory/queryPeriodStatus），"
                    + "没有改库存/过账/删单/付款工具。"
                    + "\n请人工在 ERP 中操作；回答时 need_human 必须为 true；禁止声称已改账或已写入。";
            return new AugmentResult(true, true, List.of(injection), List.of());
        }

        List<ToolTrace> traces = new ArrayList<>();
        List<String> injections = new ArrayList<>();

        // —— 互斥优先级：库存 > 存货主数据 > 期间 ——
        if (needsInventory(text)) {
            // 猜 itemCode / warehouse，缺参则交给 Handler 抛错并由 Executor 记 fail
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
            // 话术无期间时默认学习种子 2026-08，便于手测
            guessPeriod(text).ifPresentOrElse(
                    p -> args.put("period", p),
                    () -> args.put("period", "2026-08")
            );
            ToolResult result = toolExecutor.run("queryPeriodStatus", args);
            traces.add(ToolTrace.from(result));
            injections.add(formatToolInjection("queryPeriodStatus", args, result));
        }

        // —— 未识别查询意图：不注入 ——
        if (injections.isEmpty()) {
            return AugmentResult.none();
        }
        // 任一工具失败则建议 Chat 层强制 need_human，避免模型编造数量
        boolean forceHuman = traces.stream().anyMatch(t -> !t.ok());
        return new AugmentResult(false, forceHuman, injections, traces);
    }

    /**
     * 把一次工具结果格式化为可追加到 messages 的系统说明段。
     *
     * @param toolName 工具名
     * @param args     实际传入参数
     * @param result   执行结果
     * @return 注入文本
     */
    private String formatToolInjection(String toolName, Map<String, Object> args, ToolResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(TOOL_RESULT_MARKER).append('\n');
        sb.append("tool=").append(toolName).append('\n');
        sb.append("args=").append(toJson(args)).append('\n');
        sb.append("ok=").append(result.ok()).append('\n');
        if (result.ok()) {
            // 成功：强制模型只基于 data，禁止编造其它仓/期间/数量
            sb.append("data=").append(toJson(result.data())).append('\n');
            sb.append("请严格基于上述 data 回答；不要编造其它仓库/期间/存货数量。");
        } else {
            // 失败：禁止编造，并要求 need_human
            sb.append("error=").append(result.error()).append('\n');
            sb.append("工具失败：禁止编造具体数量或状态；说明缺参或未查到，并设 need_human=true。");
        }
        return sb.toString();
    }

    /**
     * 安全 JSON 序列化；失败时退回 String.valueOf。
     *
     * @param value 任意对象
     * @return JSON 或 toString
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    /**
     * 判断用户是否表达写账 / 改库存等意图。
     * 命中则不得调用任何工具（系统无写工具）。
     *
     * @param text 用户话术
     * @return true 表示写意图
     */
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

    /**
     * 是否像在问现存量。
     *
     * @param text 话术
     * @return true 则优先走 queryInventory
     */
    static boolean needsInventory(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t, "库存", "现存量", "有多少货", "还剩多少")
                || (containsAny(t, "多少") && containsAny(t, "仓"));
    }

    /**
     * 是否像在问存货名称/规格（且话术中能猜出编码）。
     *
     * @param text 话术
     * @return true 则走 queryItem
     */
    static boolean needsItem(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t, "存货", "规格", "物料名称", "叫什么名")
                && guessItemCode(text).isPresent();
    }

    /**
     * 是否像在问期间开关状态。
     *
     * @param text 话术
     * @return true 则走 queryPeriodStatus
     */
    static boolean needsPeriod(String text) {
        String t = text.toLowerCase(Locale.ROOT);
        return containsAny(t, "期间", "关账", "是否打开", "期间状态");
    }

    /**
     * 正则猜测存货编码并规范为大写。
     *
     * @param text 话术
     * @return 编码 Optional
     */
    static java.util.Optional<String> guessItemCode(String text) {
        Matcher m = ITEM_CODE.matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1).toUpperCase(Locale.ROOT));
        }
        return java.util.Optional.empty();
    }

    /**
     * 猜测仓库名（学习假数据仅原料仓 / 成品仓）。
     *
     * @param text 话术
     * @return 仓库 Optional
     */
    static java.util.Optional<String> guessWarehouse(String text) {
        if (text.contains("原料仓")) {
            return java.util.Optional.of("原料仓");
        }
        if (text.contains("成品仓")) {
            return java.util.Optional.of("成品仓");
        }
        return java.util.Optional.empty();
    }

    /**
     * 猜测会计期间 yyyy-MM。
     *
     * @param text 话术
     * @return 期间 Optional
     */
    static java.util.Optional<String> guessPeriod(String text) {
        Matcher m = PERIOD.matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1));
        }
        return java.util.Optional.empty();
    }

    /**
     * 猜测公司名；学习种子仅「主公司」，未提及时也回落主公司。
     *
     * @param text 话术
     * @return 公司名
     */
    static String guessCompany(String text) {
        if (text.contains("主公司")) {
            return "主公司";
        }
        return "主公司";
    }

    /**
     * 大小写不敏感地检测是否包含任一关键词。
     *
     * @param text     已小写或原文
     * @param keywords 关键词列表
     * @return 命中任一则为 true
     */
    private static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k.toLowerCase(Locale.ROOT)) || text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 编排输出：是否拦写、是否强制人工、注入段、工具轨迹。
     *
     * @param writeBlocked      写意图已拦截（无写工具）
     * @param forceNeedHuman    建议强制 need_human
     * @param injectionMessages 追加到 messages 的 user 附加段
     * @param traces            工具轨迹
     */
    public record AugmentResult(
            /** true=写意图已拦截，未调工具 */
            boolean writeBlocked,
            /** true=建议 Chat 强制 need_human */
            boolean forceNeedHuman,
            /** 注入到模型上下文的文本段 */
            List<String> injectionMessages,
            /** 本次预查轨迹 */
            List<ToolTrace> traces
    ) {
        /**
         * 空结果：无拦截、无注入、无轨迹。
         *
         * @return 空 AugmentResult
         */
        public static AugmentResult none() {
            return new AugmentResult(false, false, List.of(), List.of());
        }

        /**
         * @return 是否存在可追加的注入消息
         */
        public boolean hasInjections() {
            return injectionMessages != null && !injectionMessages.isEmpty();
        }
    }
}
