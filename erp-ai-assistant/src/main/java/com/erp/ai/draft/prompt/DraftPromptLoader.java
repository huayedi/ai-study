package com.erp.ai.draft.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Day20：采购草稿专用 system 提示词加载器（draft 域）。
 * <p>
 * <b>职责</b>：启动时从 classpath 读取 {@code prompts/draft-purchase-order-prompt.txt}，
 * 缓存为字符串，供草稿单轮对话作为 role=system 内容。
 * <p>
 * <b>在系统中的位置</b>：draft 域 prompt 组件；由 {@link com.erp.ai.draft.service.DraftService} 调用。
 * 与 chat 域 {@link com.erp.ai.chat.prompt.SystemPromptLoader} <b>隔离</b>，
 * 避免多轮聊天规则与草稿 JSON schema 互相污染。
 * <p>
 * <b>对应学习 Day</b>：Day20；Day22 通过 {@code PromptVersions.of("draft-po", load())} 做版本 hash。
 * <p>
 * <b>调用链 / 上下游</b>：Bean 构造加载 → {@link #load()} 返回缓存 → DraftService 拼
 * {@code [system, user(utterance)]} 调 LLM。
 * <p>
 * <b>重要设计约束</b>：
 * <ul>
 *   <li>提示词文件必存在；缺失则 {@link IllegalStateException}，阻止错误配置启动</li>
 *   <li>无热刷新：改文件需重启</li>
 *   <li>{@link #MARKER} 供测试/排障识别「确为草稿提示」的常量标记文案</li>
 * </ul>
 *
 * @see com.erp.ai.draft.service.DraftService
 */
@Component
public class DraftPromptLoader {

    /**
     * 草稿提示词中的可识别标记文案（与文件内容约定一致），
     * 便于测试断言「加载的是采购草稿提示」而非 chat system。
     */
    public static final String MARKER = "【草稿辅助·采购订单】";

    /** classpath 上草稿专用提示词路径 */
    private static final String PATH = "prompts/draft-purchase-order-prompt.txt";

    /** 构造期读入并 trim 后的完整提示词缓存 */
    private final String prompt;

    /**
     * 构造时同步加载提示词；IO 失败则 Bean 创建失败。
     *
     * @throws IllegalStateException 无法读取 {@link #PATH} 时（包装 IOException）
     */
    public DraftPromptLoader() {
        try {
            ClassPathResource resource = new ClassPathResource(PATH);
            this.prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("无法加载草稿提示词: " + PATH, e);
        }
    }

    /**
     * 返回缓存的草稿 system 提示词。
     * <p>
     * 边界：每次返回同一字符串引用；内容不可变。调用方不应假设可修改。
     *
     * @return 完整草稿 system 提示文本
     */
    public String load() {
        return prompt;
    }
}
