package com.erp.ai.rag;

/**
 * 检索命中结果：chunk + 分数。
 */
public class RetrievedChunk {

    private final TextChunk chunk;
    private final double score;

    public RetrievedChunk(TextChunk chunk, double score) {
        this.chunk = chunk;
        this.score = score;
    }

    public TextChunk getChunk() {
        return chunk;
    }

    public double getScore() {
        return score;
    }
}
