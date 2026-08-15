package com.erp.ai.rag;

/**
 * 切分后的教材文本块。
 */
public class TextChunk {

    private final String id;
    private final String docId;
    private final String section;
    private final String content;

    public TextChunk(String id, String docId, String section, String content) {
        this.id = id;
        this.docId = docId;
        this.section = section;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getDocId() {
        return docId;
    }

    public String getSection() {
        return section;
    }

    public String getContent() {
        return content;
    }

    /** 供检索与提示词使用的合并文本 */
    public String searchableText() {
        return section + "\n" + content;
    }
}
