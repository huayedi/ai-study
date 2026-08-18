package com.erp.ai.security.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Day23：Prompt 注入 / 角色劫持 / 泄密 / 绕过审批 的规则防线（学习版）。
 * <p>
 * 硬防线仍是「无写工具」；本类负责话术侧早拦与标注。
 */
@Component
public class InjectionGuard {

    public enum Category {
        IGNORE_RULES,
        LEAK_SYSTEM_PROMPT,
        ROLE_HIJACK,
        BYPASS_APPROVAL,
        WRITE_INVENTORY,
        NONE
    }

    public record Verdict(boolean blocked, Category category, String reason, List<String> matched) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule(Category.IGNORE_RULES, Pattern.compile("忽略(之前|以上|先前)?(所有)?(规则|指令|提示)"), "忽略规则/指令"),
            new Rule(Category.IGNORE_RULES, Pattern.compile("(?i)ignore (all |previous |above )?(rules|instructions)"), "ignore rules"),
            new Rule(Category.LEAK_SYSTEM_PROMPT, Pattern.compile("系统提示(词)?(原文)?|把.*提示.*打出来|打印.*system"), "索要系统提示"),
            new Rule(Category.LEAK_SYSTEM_PROMPT, Pattern.compile("(?i)(print|reveal|dump).*(system prompt|system message)"), "leak system prompt"),
            new Rule(Category.ROLE_HIJACK, Pattern.compile("你现在是|扮演.*(DBA|管理员|黑客)|jailbreak"), "角色劫持"),
            new Rule(Category.BYPASS_APPROVAL, Pattern.compile("绕过审批|跳过审批|直接过账|不要审批"), "绕过审批/直接过账"),
            new Rule(Category.WRITE_INVENTORY, Pattern.compile("直接改库存|帮我改库存|改成\\s*\\d+|writeinventory"), "写库存意图")
    );

    public Verdict inspect(String text) {
        if (text == null || text.isBlank()) {
            return new Verdict(false, Category.NONE, null, List.of());
        }
        String raw = text.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        Category hit = Category.NONE;
        for (Rule rule : RULES) {
            if (rule.pattern.matcher(raw).find() || rule.pattern.matcher(lower).find()) {
                matched.add(rule.label);
                if (hit == Category.NONE) {
                    hit = rule.category;
                }
            }
        }
        if (hit == Category.NONE) {
            return new Verdict(false, Category.NONE, null, List.of());
        }
        return new Verdict(true, hit, refusalMessage(hit), matched);
    }

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

    private record Rule(Category category, Pattern pattern, String label) {
    }
}
