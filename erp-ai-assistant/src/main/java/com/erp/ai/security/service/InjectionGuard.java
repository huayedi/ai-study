package com.erp.ai.security.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Prompt 注入 / 角色劫持 / 泄密 / 绕过审批 / 写库存意图 的规则防线（学习版）。
 *
 * <h2>职责</h2>
 * <ul>
 *   <li>对用户输入文本做正则匹配，识别常见攻击/越权话术</li>
 *   <li>返回 {@link Verdict}：是否拦截、首个命中类别、拒答文案、全部命中标签列表</li>
 *   <li>按类别提供固定中文拒答文案 {@link #refusalMessage(Category)}</li>
 * </ul>
 *
 * <h2>在系统中的位置</h2>
 * <p>
 * 位于 {@code security.service}，被以下路径复用：
 * </p>
 * <ul>
 *   <li>{@code SecurityController} — 独立安全抽测 API</li>
 *   <li>{@code ChatService} — 对话入口早拦</li>
 *   <li>{@code ScenarioService} — 场景 assist 走 SECURITY_REFUSE 时组装探针结果</li>
 *   <li>{@code EvalRunnerService} — security 类型用例评测</li>
 * </ul>
 *
 * <h2>对应学习 Day</h2>
 * <p>Day23：话术侧早拦与标注；强调硬防线仍是「无写工具」。</p>
 *
 * <h2>调用链</h2>
 * <pre>
 * 调用方.inspect(text)
 *   → 空/空白 → Verdict(false, NONE, null, [])
 *   → 遍历 RULES（对 raw 与 lower 各匹配一次）
 *   → 收集 matched 标签；首个命中类别记为 hit
 *   → 有命中 → Verdict(true, hit, refusalMessage(hit), matched)
 * </pre>
 *
 * <h2>设计约束</h2>
 * <ul>
 *   <li>规则为学习演示用关键词/正则，非完整安全产品</li>
 *   <li>多规则可同时命中；{@code category} 只取<strong>第一条</strong>命中规则的类别</li>
 *   <li>{@code matched} 可含多条标签，按 RULES 声明顺序追加</li>
 *   <li>不修改输入、不调用 LLM、不访问网络</li>
 * </ul>
 */
@Component
public class InjectionGuard {

    /**
     * 安全命中类别枚举。
     * <p>与 RULES 中各条规则绑定；{@link #NONE} 表示未命中任何规则。</p>
     */
    public enum Category {
        /** 要求忽略既有规则/指令（中英文常见注入话术）。 */
        IGNORE_RULES,
        /** 索要系统提示词原文或内部配置。 */
        LEAK_SYSTEM_PROMPT,
        /** 要求切换角色（DBA/管理员/黑客/jailbreak 等）。 */
        ROLE_HIJACK,
        /** 要求绕过审批、跳过审批、直接过账。 */
        BYPASS_APPROVAL,
        /** 要求直接改库存等写账意图（系统无写工具，应拦截）。 */
        WRITE_INVENTORY,
        /** 未命中任何规则时的占位类别。 */
        NONE
    }

    /**
     * 单次检查结论（不可变记录）。
     *
     * @param blocked  是否应拦截（命中任一规则则为 true）
     * @param category 首个命中规则的类别；未命中为 {@link Category#NONE}
     * @param reason   面向用户的拒答文案；未命中时为 null
     * @param matched  所有命中规则的可读标签列表；未命中时为空列表
     */
    public record Verdict(boolean blocked, Category category, String reason, List<String> matched) {
    }

    /**
     * 静态规则表：按声明顺序匹配。
     * <p>
     * 每条含类别、编译后的正则、可读标签。标签会进入 {@link Verdict#matched()}，
     * 供探针响应与评测展示「命中了哪些模式」。
     * </p>
     */
    private static final List<Rule> RULES = List.of(
            // 中文：忽略（之前/以上/先前）？（所有）？（规则|指令|提示）
            new Rule(Category.IGNORE_RULES, Pattern.compile("忽略(之前|以上|先前)?(所有)?(规则|指令|提示)"), "忽略规则/指令"),
            // 英文忽略规则（大小写不敏感）
            new Rule(Category.IGNORE_RULES, Pattern.compile("(?i)ignore (all |previous |above )?(rules|instructions)"), "ignore rules"),
            // 索要系统提示词 / 打印 system
            new Rule(Category.LEAK_SYSTEM_PROMPT, Pattern.compile("系统提示(词)?(原文)?|把.*提示.*打出来|打印.*system"), "索要系统提示"),
            new Rule(Category.LEAK_SYSTEM_PROMPT, Pattern.compile("(?i)(print|reveal|dump).*(system prompt|system message)"), "leak system prompt"),
            // 角色劫持：你现在是 / 扮演 DBA|管理员|黑客 / jailbreak
            new Rule(Category.ROLE_HIJACK, Pattern.compile("你现在是|扮演.*(DBA|管理员|黑客)|jailbreak"), "角色劫持"),
            // 绕过审批类
            new Rule(Category.BYPASS_APPROVAL, Pattern.compile("绕过审批|跳过审批|直接过账|不要审批"), "绕过审批/直接过账"),
            // 写库存意图（含 writeinventory 字面）
            new Rule(Category.WRITE_INVENTORY, Pattern.compile("直接改库存|帮我改库存|改成\\s*\\d+|writeinventory"), "写库存意图")
    );

    /**
     * 检查文本是否命中注入/越权规则。
     * <p>
     * 业务含义：对用户原始输入做早拦；命中则返回拒答文案与标签。
     * 边界：{@code null} 或空白串视为安全，返回未拦截；匹配同时尝试原始串与小写串，
     * 以覆盖部分大小写敏感/不敏感混用的正则。
     * </p>
     *
     * @param text 待检查的用户话术；可为 null
     * @return 判定结论；永不返回 null
     */
    public Verdict inspect(String text) {
        // 空输入直接放行：无内容可匹配，不算攻击
        if (text == null || text.isBlank()) {
            return new Verdict(false, Category.NONE, null, List.of());
        }
        String raw = text.trim();
        // 小写副本：部分规则用 (?i)，部分依赖 lower 再匹配一次以提高命中面
        String lower = raw.toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        Category hit = Category.NONE;
        for (Rule rule : RULES) {
            // 对原始文本与小写文本各找一次；任一 find 成功即记命中
            if (rule.pattern.matcher(raw).find() || rule.pattern.matcher(lower).find()) {
                matched.add(rule.label);
                // 仅第一条命中规则决定 category（后续命中只追加 matched）
                if (hit == Category.NONE) {
                    hit = rule.category;
                }
            }
        }
        if (hit == Category.NONE) {
            return new Verdict(false, Category.NONE, null, List.of());
        }
        // 拦截：blocked=true，reason 为对应类别的固定拒答文案
        return new Verdict(true, hit, refusalMessage(hit), matched);
    }

    /**
     * 按类别返回固定中文拒答文案。
     * <p>
     * 业务含义：统一对外说辞，避免模型自由发挥越权说明。
     * 边界：{@link Category#NONE} 返回空串（正常不应作为拒答展示）。
     * </p>
     *
     * @param category 命中类别；不应为 null
     * @return 对应该类别的拒答字符串；NONE 时为 ""
     */
    public String refusalMessage(Category category) {
        return switch (category) {
            case IGNORE_RULES -> "已拦截：不能按「忽略规则」改角色或推翻安全策略。请用正常 ERP 业务问题提问。";
            case LEAK_SYSTEM_PROMPT -> "已拦截：不会输出系统提示词原文或内部配置。";
            case ROLE_HIJACK -> "已拦截：不能切换为 DBA/黑客等越权角色。本助手只做 ERP 学习辅助。";
            case BYPASS_APPROVAL -> "已拦截：不能提供绕过审批或直接过账的步骤。请走合规审批，并由有权限同事操作。";
            case WRITE_INVENTORY -> "已拦截：本系统没有写库存工具，不会改账。请人工在 ERP 中操作。";
            case NONE -> "";
        };
    }

    /**
     * 内部规则三元组：类别 + 正则 + 可读标签。
     *
     * @param category 命中后归属的 {@link Category}
     * @param pattern  已编译的匹配模式
     * @param label    写入 Verdict.matched 的中文/英文短标签
     */
    private record Rule(Category category, Pattern pattern, String label) {
    }
}
