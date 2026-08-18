package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 按 Markdown 标题结构切分教材文本块。
 * <p>
 * <b>职责</b>：把整篇 Markdown 按 {@code ##}/{@code ###} 切成章节，
 * 超长章节再按固定字符窗口切开，产出带稳定 id 的 {@link TextChunk} 列表。
 * <p>
 * <b>RAG 流水线位置</b>：{@code chunk}（load 之后、索引/检索之前）。
 * <p>
 * <b>对应 Day</b>：Day10/11（切分与内存语料索引）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>输入来自 {@link DocumentCorpusLoader.LoadedDocument}；</li>
 *   <li>输出被 {@link RagCorpusIndex} 聚合为全量语料；</li>
 *   <li>后续 keyword/vector/hybrid 检索均消费这些 chunk。</li>
 * </ul>
 * <p>
 * <b>边界</b>：正文为空或仅空白的块在最终结果中被过滤；无标题文档会落入默认「前言」节。
 */
@Component
public class HeadingChunker {

    /** 单块最大字符数；超过则硬切窗口（学习版不做语义重叠） */
    private static final int MAX_CHARS = 800;

    /**
     * 将一篇 Markdown 切分为若干 {@link TextChunk}。
     * <p>
     * <b>算法步骤</b>：
     * <ol>
     *   <li>{@link #splitByHeadings}：按二级/三级标题切成 {@link Section}；</li>
     *   <li>对每个 section 的 body 调用 {@link #splitLong}，必要时再切窗口；</li>
     *   <li>多窗口时在 section 标签上追加 {@code (i/n)}；</li>
     *   <li>生成 id：{@code docId#序号}（序号全局递增）；</li>
     *   <li>过滤 content 空白的块。</li>
     * </ol>
     *
     * @param docId    文档标识（通常为文件名），写入每个 chunk 的 docId，并参与 id 前缀
     * @param markdown 原文 Markdown；空串会得到空列表或仅空白被滤掉
     * @return 非空白文本块列表；无有效内容时为空列表
     */
    public List<TextChunk> chunk(String docId, String markdown) {
        // 步骤 1：按标题切成逻辑章节
        List<Section> sections = splitByHeadings(markdown);
        List<TextChunk> chunks = new ArrayList<>();
        int index = 0;
        for (Section section : sections) {
            // 步骤 2：超长章节按 MAX_CHARS 硬切
            List<String> parts = splitLong(section.body());
            for (int i = 0; i < parts.size(); i++) {
                // 单窗口保留原标题；多窗口标注分片序号便于溯源
                String sectionLabel = parts.size() == 1
                        ? section.title()
                        : section.title() + " (" + (i + 1) + "/" + parts.size() + ")";
                // 稳定 id：文档内递增，便于检索去重与 RRF 按 id 合并
                String id = docId + "#" + (index++);
                chunks.add(new TextChunk(id, docId, sectionLabel, parts.get(i).trim()));
            }
        }
        // 步骤 5：丢掉纯空白块（空标题节、空前言等）
        return chunks.stream().filter(c -> !c.getContent().isBlank()).toList();
    }

    /**
     * 按行扫描，遇到 {@code ## } 或 {@code ### } 开启新节；文首无标题内容归入「前言」。
     * <p>
     * 注意：一级标题 {@code # } 不作为切分点（学习版约定仅二/三级）。
     *
     * @param markdown 原文；{@code \r\n} 会先规范为 {@code \n}
     * @return 至少包含一个 Section（可能 body 为空）
     */
    private static List<Section> splitByHeadings(String markdown) {
        // 统一换行，避免 Windows 换行干扰行首标题检测
        String[] lines = markdown.replace("\r\n", "\n").split("\n");
        List<Section> sections = new ArrayList<>();
        // 文首默认节名：无 ##/### 时全部落入「前言」
        String currentTitle = "前言";
        StringBuilder body = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("## ") || line.startsWith("### ")) {
                // 收束上一节，再开启新节
                sections.add(new Section(currentTitle, body.toString().trim()));
                // 去掉行首井号与空白，得到可读章节名
                currentTitle = line.replaceFirst("^#+\\s*", "").trim();
                body = new StringBuilder();
            } else {
                body.append(line).append('\n');
            }
        }
        // 收束最后一节
        sections.add(new Section(currentTitle, body.toString().trim()));
        return sections;
    }

    /**
     * 将过长正文按固定窗口切开；不重叠、不回溯句子边界（学习版简化）。
     * <p>
     * <b>边界</b>：{@code null}/空白 → 空列表；长度 ≤ {@link #MAX_CHARS} → 单元素列表。
     *
     * @param text 章节正文
     * @return 切分后的字符串片段列表
     */
    private static List<String> splitLong(String text) {
        // 空输入：不产生 chunk 原料
        if (text == null || text.isBlank()) {
            return List.of();
        }
        // 未超限：整节一块
        if (text.length() <= MAX_CHARS) {
            return List.of(text);
        }
        // 硬切：每次推进 MAX_CHARS 字符
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + MAX_CHARS);
            parts.add(text.substring(start, end));
            start = end;
        }
        return parts;
    }

    /**
     * 内部标题节：切分过程中的临时结构，不对外暴露。
     *
     * @param title 章节标题（或「前言」）
     * @param body  该标题下的正文（已 trim）
     */
    private record Section(String title, String body) {
    }
}
