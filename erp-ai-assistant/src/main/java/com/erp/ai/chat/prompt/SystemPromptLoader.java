package com.erp.ai.chat.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 系统提示词加载器（chat 域）。
 * <p>
 * <b>职责</b>：在 Spring Bean 构造阶段从 classpath 读取 ERP 对话用 system 提示词，
 * 可选追加 few-shot 示范，并缓存为不可变字符串供每轮对话复用。
 * <p>
 * <b>在系统中的位置</b>：chat 域 prompt 组件；由 {@link com.erp.ai.chat.service.ChatService}
 * 在拼装 messages 时取 {@link #getSystemPrompt()} 作为 role=system 的首条消息。
 * 与草稿域 {@link com.erp.ai.draft.prompt.DraftPromptLoader} 隔离，避免草稿规则污染多轮聊天。
 * <p>
 * <b>对应学习 Day</b>：Week1 提示词外置；Day22 通过 {@code PromptVersions.of("erp-system", ...)}
 * 对返回内容做版本 hash，改文件后需重启才生效并改变版本。
 * <p>
 * <b>调用链 / 上下游</b>：
 * <ol>
 *   <li>启动：构造方法 → {@link #load()} → 读 {@code prompts/erp-system-prompt.txt}</li>
 *   <li>可选：若存在 {@code prompts/erp-few-shot.txt} 则 trim 后追加到 base 之后</li>
 *   <li>运行期：{@code ChatService} / 观测层读取缓存字符串，不再重复读盘</li>
 * </ol>
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>主文件必存在，缺失则抛 {@link IllegalStateException} 阻止应用以错误配置启动</li>
 *   <li>few-shot 文件可选：不存在或空白则仅返回 base</li>
 *   <li>学习阶段改提示词后需要重启应用才会生效（无热刷新）</li>
 * </ul>
 *
 * @see com.erp.ai.chat.service.ChatService
 */
@Component
public class SystemPromptLoader {

    /** classpath 主系统提示词路径：角色 / 规则 / 术语 / 输出 JSON 格式 */
    private static final String SYSTEM_PROMPT_PATH = "prompts/erp-system-prompt.txt";

    /** classpath 少样本示范路径；文件可不存在 */
    private static final String FEW_SHOT_PATH = "prompts/erp-few-shot.txt";

    /** 启动时组装并缓存的完整 system 提示词（可能已含 few-shot） */
    private final String systemPrompt;

    /**
     * 构造时立即加载并缓存提示词；失败则 Bean 创建失败，应用无法启动。
     */
    public SystemPromptLoader() {
        this.systemPrompt = load();
    }

    /**
     * 获取作为 role=system 发送给模型的完整提示词。
     * <p>
     * 边界：返回的是构造期缓存引用；调用方不应修改（字符串本身不可变）。
     * Day18 路径 A 时，{@code ChatService} 会在此基础上再追加工具使用说明，而不改本缓存。
     *
     * @return 完整 system 提示词文本，已 trim；若加载过 few-shot 则二者已用空行拼接
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * 组装主提示词与可选 few-shot。
     * <p>
     * 为何分开读：主规则必须有；示范样本可缺，避免为可选文件拖垮启动。
     *
     * @return 最终 system 文本
     */
    private static String load() {
        // 主文件失败直接中断启动，避免带着空 system 调模型
        String base = readRequired(SYSTEM_PROMPT_PATH);
        String fewShot = readOptional(FEW_SHOT_PATH);
        // 无 few-shot 或全空白：不追加分隔符，避免污染提示词尾部
        if (fewShot == null || fewShot.isBlank()) {
            return base;
        }
        // 用空行分隔，让模型清晰区分「规则」与「示范」
        return base + "\n\n" + fewShot.trim();
    }

    /**
     * 读取必选 classpath 资源；不存在或 IO 失败则包装为 IllegalStateException。
     *
     * @param classpathLocation classpath 相对路径
     * @return 文件全文 trim 后的字符串
     * @throws IllegalStateException 无法打开或读取资源时
     */
    private static String readRequired(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载提示词 " + classpathLocation, ex);
        }
    }

    /**
     * 读取可选 classpath 资源。
     * <p>
     * 为何先 {@code exists()}：避免可选文件缺失时被当成启动失败。
     * 若文件存在但读失败，仍视为配置错误并抛异常（与「故意不放文件」区分）。
     *
     * @param classpathLocation classpath 相对路径
     * @return 全文 trim；文件不存在时返回 null
     * @throws IllegalStateException 文件存在但读取失败时
     */
    private static String readOptional(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载提示词 " + classpathLocation, ex);
        }
    }
}
