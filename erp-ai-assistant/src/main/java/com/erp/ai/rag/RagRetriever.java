package com.erp.ai.rag;

import java.util.List;

/**
 * RAG 检索器抽象（Day11/12）：关键词与向量实现可替换，RagService 只依赖本接口。
 */
public interface RagRetriever {

    /**
     * @param question 用户问题
     * @param corpus   切分后的教材块（关键词检索使用；向量检索可忽略，改读自有索引）
     * @param topK     返回条数
     */
    List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK);

    /** 供日志/响应观察：keyword | vector */
    String name();
}
