package com.erp.ai.common.client;

import com.erp.ai.common.model.ChatMessage;
import com.erp.ai.tool.service.ChatToolOrchestrator;
import com.erp.ai.tool.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 Mock LLM：无 API Key 时模拟真实模型的决策路径。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>按用户意图返回结构化 JSON（库存 / 采购 / 拒写 / 草稿字段等）</li>
 *   <li>Day18 路径 A：在暴露 tools 时先返回 {@link LlmToolCall}，收到 tool 回填后再给终答</li>
 *   <li>路径 B：识别编排器注入的标记文本并填答</li>
 *   <li>Day20：识别草稿辅助 system 标记，输出 fields/missing schema</li>
 * </ul>
 *
 * <h2>为何需要 Mock</h2>
 * 学习项目要保证「克隆即跑」：不依赖外网与计费。Mock 刻意模仿真实编排分支，
 * 以便日后切到 {@link OpenAiCompatibleLlmClient} 时业务代码不变。
 *
 * <h2>与 Spring / 配置的关系</h2>
 * 由 {@code LlmClientConfig} 在 {@code ai.provider=mock}（或未配置）时注册为唯一 {@link LlmClient} Bean。
 * 依赖注入的 {@link ObjectMapper} 用于组装 JSON 节点。
 *
 * <h2>学习要点</h2>
 * <ol>
 *   <li>分支顺序很重要：草稿 → 已有 tool 回填 → 发起 tool_calls → 拒写 → 路径 B → 关键词兜底</li>
 *   <li>Mock 禁止编造库存数量：没有工具结果就引导补参或人工核对</li>
 *   <li>写意图一律拦截，体现 ERP 助手「只读」边界</li>
 * </ol>
 */
public class MockLlmClient implements LlmClient {

    /** 从工具结果 JSON 文本中提取 qty 数值 */
    private static final Pattern QTY = Pattern.compile("\"qty\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    /** 从工具结果中提取期间 status 枚举字符串 */
    private static final Pattern STATUS = Pattern.compile("\"status\"\\s*:\\s*\"([A-Z]+)\"");

    /** 从工具结果中提取 warehouse 字段 */
    private static final Pattern WAREHOUSE = Pattern.compile("\"warehouse\"\\s*:\\s*\"([^\"]+)\"");

    /** 从工具结果 JSON 中提取 itemCode */
    private static final Pattern ITEM = Pattern.compile("\"itemCode\"\\s*:\\s*\"([^\"]+)\"");

    /** 从自然语言中猜测存货编码（ITEM-xxx 或 A001 形态） */
    private static final Pattern ITEM_CODE = Pattern.compile("(?i)\\b(ITEM-[A-Z0-9]+|A\\d{3})\\b");

    /** Jackson：组装 mock 返回的 JSON 对象树 */
    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper Spring 容器中的共享 ObjectMapper；不得为 {@code null}
     */
    public MockLlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     *
     * @return 固定为 {@code mock}，便于审计区分真实网关
     */
    @Override
    public String providerName() {
        return "mock";
    }

    /**
     * Mock 决策主流程：按消息内容与是否暴露 tools 走不同分支。
     * <p>
     * <b>失败语义：</b>本方法不抛业务异常；非法/未知意图落到关键词兜底 JSON。
     * 与真实客户端不同，Mock 把「能力边界」写进 answer，而不是 HTTP 错误。
     *
     * @param messages 完整对话消息；不应为 {@code null}
     * @param tools    本轮暴露的工具；空或 {@code null} 表示不走路径 A 的 tool_calls 发起
     * @return 始终非 {@code null}；可能带 toolCalls 或带 JSON content
     */
    @Override
    public LlmResult chat(List<ChatMessage> messages, List<ToolDefinition> tools) {
        // ---- 步骤 1：汇总文本，便于标记检测与关键词判断 ----
        String allText = joinAllText(messages);
        // 取「原始用户话」，跳过编排器注入的工具结果/拒写标记，避免误判意图
        String userText = latestOriginalUserText(messages);
        String userLower = userText.toLowerCase(Locale.ROOT);
        boolean withTools = tools != null && !tools.isEmpty();

        // ---- 步骤 2：Day20 草稿辅助专用 system 标记 ----
        // 标记与 draft-purchase-order-prompt.txt / DraftPromptLoader.MARKER 保持一致
        // 输出 schema 为 fields/missing，不是普通 chat 的 answer 结构
        if (allText.contains("【草稿辅助·采购订单】")) {
            return jsonResult(draftPurchaseOrderJson(userText));
        }

        // ---- 步骤 3：路径 A 第二拍——消息里已有 role=tool，产出最终 JSON ----
        if (hasToolRole(messages)) {
            return finalFromToolMessages(messages, allText);
        }

        // ---- 步骤 4：路径 A 第一拍——暴露 tools 且是库存问句 → 先发 queryInventory ----
        if (withTools && needsInventory(userLower) && !isWriteIntent(userLower)) {
            String item = guessItem(userText).orElse("A001");
            String warehouse = guessWarehouse(userText).orElse(null);
            ObjectNode args = objectMapper.createObjectNode();
            args.put("itemCode", item);
            // 仓库未知时故意不放字段，让工具返回缺参，演示「不编造」
            if (warehouse != null) {
                args.put("warehouse", warehouse);
            }
            LlmToolCall call = new LlmToolCall(
                    "call_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                    "queryInventory",
                    args.toString()
            );
            // content=null + 非空 toolCalls：编排器必须先执行工具
            return new LlmResult(null, "mock-erp-assistant", 32, 16, List.of(call));
        }

        // ---- 步骤 5：路径 A 第一拍——期间状态问句 → queryPeriodStatus ----
        if (withTools && needsPeriod(userLower) && !isWriteIntent(userLower)) {
            ObjectNode args = objectMapper.createObjectNode();
            args.put("company", "主公司");
            args.put("period", guessPeriod(userText).orElse("2026-08"));
            LlmToolCall call = new LlmToolCall(
                    "call_mock_period",
                    "queryPeriodStatus",
                    args.toString()
            );
            return new LlmResult(null, "mock-erp-assistant", 32, 16, List.of(call));
        }

        // ---- 步骤 6：写意图 / 路径 B 拦截标记 → 拒写 JSON ----
        if (allText.contains(ChatToolOrchestrator.WRITE_BLOCK_MARKER) || isWriteIntent(userLower)) {
            return jsonResult(writeBlockedJson());
        }

        // ---- 步骤 7：路径 B——编排器已把工具结果注入 user/system 文本 ----
        if (allText.contains(ChatToolOrchestrator.TOOL_RESULT_MARKER)) {
            return jsonResult(fillFromToolResultText(allText));
        }

        // ---- 步骤 8：无工具链路时的关键词规则兜底 ----
        return jsonResult(keywordJson(userLower));
    }

    /**
     * 根据 tool 角色消息生成终答。
     * <p>
     * 若任一条工具结果含 {@code ok:false} 或缺参提示，则明确拒答并 {@code need_human=true}，
     * <strong>绝不编造数量</strong>。
     *
     * @param messages 含 role=tool 的消息列表
     * @param allText  全量拼接文本（工具 blob 为空时的回退）
     * @return 包装后的 LlmResult（JSON content）
     */
    private LlmResult finalFromToolMessages(List<ChatMessage> messages, String allText) {
        // 合并所有 tool 内容，便于统一扫描 ok/qty/status
        String toolBlob = messages.stream()
                .filter(m -> "tool".equals(m.getRole()))
                .map(ChatMessage::getContent)
                .reduce("", (a, b) -> a + "\n" + b);
        boolean anyFail = toolBlob.contains("\"ok\":false") || toolBlob.contains("\"ok\": false");
        ObjectNode root;
        if (anyFail || toolBlob.contains("missing required")) {
            root = objectMapper.createObjectNode();
            root.put("answer", "只读工具未返回可用数据（可能缺仓库等参数）。我不会编造库存数量；请补全参数后重试。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.4);
        } else {
            root = fillFromToolResultText(toolBlob.isBlank() ? allText : toolBlob);
        }
        return jsonResult(root);
    }

    /**
     * 构造「已拦截写意图」的标准 JSON。
     *
     * @return 非 {@code null} 的 ObjectNode，含 need_human=true
     */
    private ObjectNode writeBlockedJson() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("answer", "已拦截写意图：本助手没有改库存/过账工具，不会把数量改成你说的值。请有权限的同事在 ERP 中人工操作。");
        root.put("need_human", true);
        root.putNull("suggested_doc_type");
        root.putArray("required_fields");
        root.put("confidence", 0.9);
        return root;
    }

    /**
     * 从工具结果文本（JSON 或注入标记后的拼接串）填充聊天答案。
     * <p>
     * 解析优先级：失败标志 → qty → status → 泛化成功句。
     *
     * @param allText 含工具结果的文本；允许非严格 JSON
     * @return 业务 JSON 节点
     */
    private ObjectNode fillFromToolResultText(String allText) {
        ObjectNode root = objectMapper.createObjectNode();
        // 宽松成功判定：ok=true 或出现 qty 字段都视为有可用数据
        boolean ok = allText.contains("ok=true") || allText.contains("\"ok\":true") || allText.contains("\"qty\"");
        if (!ok && (allText.contains("ok=false") || allText.contains("\"ok\":false"))) {
            root.put("answer", "只读工具未查到可用结果（可能缺仓库/存货参数或假数据无此键）。我不会编造库存数量；请补全参数后重试，或人工在库存查询核对。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.4);
            return root;
        }

        // 库存数量：拼一句可读中文，并声明「其它仓库不编造」
        Matcher qty = QTY.matcher(allText);
        if (qty.find()) {
            String item = matchOr(ITEM, allText, "存货");
            String wh = matchOr(WAREHOUSE, allText, "仓库");
            root.put("answer", "根据只读工具查询：" + item + " 在 " + wh + " 的现存量是 " + qty.group(1)
                    + "（学习假数据）。其它仓库数量未经查询，我不编造。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.85);
            return root;
        }

        // 期间状态
        Matcher status = STATUS.matcher(allText);
        if (status.find()) {
            root.put("answer", "根据只读工具查询：期间状态为 " + status.group(1) + "（学习假数据）。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.85);
            return root;
        }

        // 有成功迹象但未匹配到具体字段：给保守成功话术
        root.put("answer", "根据只读工具结果回答（mock）。");
        root.put("need_human", false);
        root.putNull("suggested_doc_type");
        root.putArray("required_fields");
        root.put("confidence", 0.8);
        return root;
    }

    /**
     * Day20 mock：从口语抽取采购订单字段。
     * <p>
     * <b>硬约束：</b>未知字段进 {@code missing}；<strong>禁止编造单价/仓库</strong>；
     * 「下周一」用规则换算为具体 ISO 日期并写入 warnings。
     *
     * @param userText 用户原始话术；可为 {@code null}
     * @return fields/missing/warnings/need_human/confidence 结构
     */
    private ObjectNode draftPurchaseOrderJson(String userText) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("suggested_doc_type", "采购订单");
        ObjectNode fields = root.putObject("fields");
        ArrayNode missing = root.putArray("missing");
        ArrayNode warnings = root.putArray("warnings");

        // 供应商：正则「向…买」或关键词「华东供应」
        String supplier = guessSupplier(userText);
        if (supplier != null) {
            fields.put("供应商", supplier);
        } else {
            fields.putNull("供应商");
            missing.add("供应商");
        }

        // 存货编码
        String item = guessItem(userText).orElse(null);
        if (item != null) {
            fields.put("存货编码", item);
        } else {
            fields.putNull("存货编码");
            missing.add("存货编码");
        }

        // 数量：仅识别明确数字模式，不猜测
        Long qty = guessQty(userText);
        if (qty != null) {
            fields.put("数量", qty);
        } else {
            fields.putNull("数量");
            missing.add("数量");
        }

        // 仓库：仅原料仓/成品仓关键词
        String warehouse = guessWarehouse(userText).orElse(null);
        if (warehouse != null) {
            fields.put("仓库", warehouse);
        } else {
            fields.putNull("仓库");
            missing.add("仓库");
        }

        // 禁止编造单价：永远 missing
        fields.putNull("含税单价");
        missing.add("含税单价");

        // 交货日期：口语「下周一」或 ISO 日期；否则 missing
        if (userText != null && userText.contains("下周一")) {
            String date = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            fields.put("交货日期", date);
            warnings.add("交货日期由 mock 规则换算「下周一」");
        } else {
            Matcher date = Pattern.compile("(20\\d{2}-\\d{2}-\\d{2})").matcher(userText == null ? "" : userText);
            if (date.find()) {
                fields.put("交货日期", date.group(1));
            } else {
                fields.putNull("交货日期");
                missing.add("交货日期");
            }
        }

        // 草稿永远建议人工确认；缺项越少 confidence 略高
        root.put("need_human", true);
        root.put("confidence", missing.size() <= 2 ? 0.72 : 0.55);
        return root;
    }

    /**
     * 从口语猜测供应商名。
     *
     * @param text 用户文本
     * @return 供应商名；无法识别时 {@code null}
     */
    private static String guessSupplier(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher m = Pattern.compile("向([^买，,。\\s]{2,20})买").matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        if (text.contains("华东供应")) {
            return "华东供应";
        }
        return null;
    }

    /**
     * 从口语猜测采购数量（个 / 买 N / 数量=N）。
     *
     * @param text 用户文本
     * @return 数量；无法识别时 {@code null}
     */
    private static Long guessQty(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = Pattern.compile("(\\d+)\\s*个").matcher(text);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        m = Pattern.compile("买\\s*(\\d+)").matcher(text);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        m = Pattern.compile("数量\\s*[=:：]?\\s*(\\d+)").matcher(text);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return null;
    }

    /**
     * 无工具时的关键词规则引擎：按意图返回不同结构化 JSON。
     *
     * @param userText 已小写化的用户文本（部分中文关键词仍用原文 contains）
     * @return 聊天 Schema 的 ObjectNode
     */
    private ObjectNode keywordJson(String userText) {
        ObjectNode root = objectMapper.createObjectNode();
        if (containsAny(userText, "【教材资料】", "教材资料")) {
            root.put("answer", "结论：根据提供的教材资料回答（mock）。若资料含采购主链路，则常见顺序为物资请购→采购订单→到货单→采购入库→采购发票。请以 sources 中的文件章节为准。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.7);
        } else if (containsAny(userText, "库存", "现存量", "有多少")) {
            // 无 tools 时不能查库：引导人工或接入工具
            root.put("answer", "我无法直接查询实时库存。请在【库存查询】按物料编码+仓库核对，或接入只读库存工具后再问我。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.55);
        } else if (containsAny(userText, "采购", "请购", "供应商")) {
            root.put("answer", "若要发起采购，建议先建采购申请/采购订单草稿。请确认供应商、物料、数量、交期、税率后由人工保存。");
            root.put("need_human", true);
            root.put("suggested_doc_type", "采购订单");
            ArrayNode fields = root.putArray("required_fields");
            fields.add("供应商");
            fields.add("物料编码");
            fields.add("数量");
            fields.add("含税单价");
            fields.add("交货日期");
            root.put("confidence", 0.72);
        } else if (containsAny(userText, "过账", "删除", "改库存", "付款")) {
            root.put("answer", "这是高风险操作。请先核对单据状态与期间是否打开，并由有权限的同事在系统中确认执行；我不会直接改账。");
            root.put("need_human", true);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.8);
        } else if (containsAny(userText, "期间", "关账", "凭证不平衡")) {
            root.put("answer", "可按此排查：1) 确认会计期间是否打开；2) 检查借贷是否平衡；3) 查看是否有未审核凭证；4) 仍失败则截图报错给财务顾问。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.7);
        } else {
            root.put("answer", "我是 ERP 助手（mock 模式）。你可以问：采购录单需要哪些字段、期间关闭怎么排查、某类报错如何处理。配置真实 API 后回答会更灵活。");
            root.put("need_human", false);
            root.putNull("suggested_doc_type");
            root.putArray("required_fields");
            root.put("confidence", 0.6);
        }
        return root;
    }

    /**
     * 将 ObjectNode 序列化为字符串并估算 completion tokens。
     *
     * @param root JSON 根节点
     * @return LlmResult（无 toolCalls）
     */
    private LlmResult jsonResult(ObjectNode root) {
        String content = root.toString();
        // 粗估：约 4 字符 ≈ 1 token，下限 16
        int completionTokens = Math.max(16, content.length() / 4);
        return new LlmResult(content, "mock-erp-assistant", 48, completionTokens);
    }

    /**
     * @param messages 消息列表
     * @return 是否已存在 role=tool（路径 A 第二拍）
     */
    private static boolean hasToolRole(List<ChatMessage> messages) {
        return messages.stream().anyMatch(m -> "tool".equals(m.getRole()));
    }

    /**
     * 是否像库存现存量询问。
     *
     * @param text 小写用户文本
     * @return {@code true} 时应优先走 queryInventory
     */
    private static boolean needsInventory(String text) {
        return containsAny(text, "库存", "现存量", "有多少货", "还剩多少")
                || (containsAny(text, "多少") && containsAny(text, "仓"));
    }

    /**
     * 是否像会计期间 / 关账状态询问。
     *
     * @param text 小写用户文本
     * @return {@code true} 时应优先走 queryPeriodStatus
     */
    private static boolean needsPeriod(String text) {
        return containsAny(text, "期间", "关账", "是否打开", "期间状态");
    }

    /**
     * 是否像写库存 / 过账 / 删单等危险意图。
     *
     * @param text 小写用户文本
     * @return {@code true} 时必须拒写，不得发起写工具（本助手本就没有写工具）
     */
    private static boolean isWriteIntent(String text) {
        return containsAny(text,
                "改成", "改库存", "调库存", "写成", "调成",
                "过账", "删单", "删除单据", "付款", "关闭期间", "重开期间",
                "writeinventory", "帮我改");
    }

    /**
     * 从文本猜测存货编码。
     * <p>
     * 先严格匹配 ITEM-xxx / A001；再宽松匹配字母+数字（如 Z999）便于演示主数据不存在。
     *
     * @param text 用户文本
     * @return 大写编码；找不到则为 empty
     */
    private static java.util.Optional<String> guessItem(String text) {
        Matcher m = ITEM_CODE.matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1).toUpperCase(Locale.ROOT));
        }
        // 草稿场景：宽松匹配字母+数字编码（如 Z999），便于演示「主数据不存在」
        m = Pattern.compile("(?i)\\b([A-Z]\\d{3,})\\b").matcher(text == null ? "" : text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1).toUpperCase(Locale.ROOT));
        }
        return java.util.Optional.empty();
    }

    /**
     * 从文本猜测仓库名（仅演示用的两个仓）。
     *
     * @param text 用户文本；调用方保证非 null 时再 contains（见调用处）
     * @return 仓库名或 empty
     */
    private static java.util.Optional<String> guessWarehouse(String text) {
        if (text.contains("原料仓")) {
            return java.util.Optional.of("原料仓");
        }
        if (text.contains("成品仓")) {
            return java.util.Optional.of("成品仓");
        }
        return java.util.Optional.empty();
    }

    /**
     * 从文本猜测会计期间 {@code yyyy-MM}。
     *
     * @param text 用户文本
     * @return 期间字符串或 empty
     */
    private static java.util.Optional<String> guessPeriod(String text) {
        Matcher m = Pattern.compile("(20\\d{2}-\\d{2})").matcher(text);
        if (m.find()) {
            return java.util.Optional.of(m.group(1));
        }
        return java.util.Optional.empty();
    }

    /**
     * 用正则取第一捕获组，找不到则返回 fallback。
     *
     * @param p        已编译正则
     * @param text     待匹配文本
     * @param fallback 未匹配时的占位中文
     * @return 捕获组或 fallback
     */
    private static String matchOr(Pattern p, String text, String fallback) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : fallback;
    }

    /**
     * 拼接所有消息 content，用于标记检测。
     *
     * @param messages 消息列表
     * @return 以换行连接的全文；跳过 null content
     */
    private static String joinAllText(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : messages) {
            if (message != null && message.getContent() != null) {
                sb.append(message.getContent()).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * 从后往前找最近一条「原始」user 消息（排除工具结果/拒写注入）。
     *
     * @param messages 消息列表
     * @return 用户原文；找不到时返回空串（不会 {@code null}）
     */
    private static String latestOriginalUserText(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (!"user".equals(message.getRole()) || message.getContent() == null) {
                continue;
            }
            String c = message.getContent();
            if (c.contains(ChatToolOrchestrator.TOOL_RESULT_MARKER)
                    || c.contains(ChatToolOrchestrator.WRITE_BLOCK_MARKER)) {
                continue;
            }
            return c;
        }
        return "";
    }

    /**
     * 文本是否包含任一关键词（同时尝试原文与 lower 形态，兼容中英混写）。
     *
     * @param text     待检文本
     * @param keywords 关键词列表
     * @return 命中任一即 {@code true}
     */
    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT)) || text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
