package com.erp.ai.rag;

/**
 * 切分后的教材文本块（RAG 语料原子单位）。
 * <p>
 * <b>职责</b>：承载单块文本的稳定标识、来源文档、章节标签与正文，
 * 并提供检索用合并文本 {@link #searchableText()}。
 * <p>
 * <b>RAG 流水线位置</b>：{@code chunk} 产物；贯穿 retrieve / gate / prompt（作为引用资料）。
 * <p>
 * <b>对应 Day</b>：Day10/11。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>由 {@link HeadingChunker} 创建；</li>
 *   <li>存于 {@link RagCorpusIndex}；</li>
 *   <li>被 {@link RetrievedChunk} / {@link IndexedChunk} 包装；</li>
 *   <li>提示词与 sources 摘录均读取本节字段。</li>
 * </ul>
 */
public class TextChunk {

    /** 块唯一 id，形如 {@code inventory.md#3}，供 RRF 去重与稳定排序 */
    private final String id;

    /** 来源文档 id（通常为 Markdown 文件名） */
    private final String docId;

    /** 章节标签（标题；超长切分时可能带 {@code (i/n)}） */
    private final String section;

    /** 块正文内容（已 trim） */
    private final String content;

    /**
     * 构造不可变文本块。
     *
     * @param id      块唯一标识
     * @param docId   文档标识
     * @param section 章节标签
     * @param content 正文
     */
    public TextChunk(String id, String docId, String section, String content) {
        this.id = id;
        this.docId = docId;
        this.section = section;
        this.content = content;
    }

    /**
     * @return 块唯一 id
     */
    public String getId() {
        return id;
    }

    /**
     * @return 来源文档 id
     */
    public String getDocId() {
        return docId;
    }

    /**
     * @return 章节标签
     */
    public String getSection() {
        return section;
    }

    /**
     * @return 块正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 供检索打分与 Embedding 使用的合并文本：章节名 + 换行 + 正文。
     * <p>
     * 使标题中的关键词也能参与 keyword 重叠与向量语义。
     *
     * @return {@code section + "\n" + content}
     */
    public String searchableText() {
        return section + "\n" + content;
    }
}
