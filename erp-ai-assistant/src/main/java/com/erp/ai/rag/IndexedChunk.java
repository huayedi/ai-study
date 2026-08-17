package com.erp.ai.rag;

/**
 * 带向量的索引条目（Day11 索引阶段产物）。
 */
public class IndexedChunk {

    private final TextChunk chunk;
    private final float[] vector;

    public IndexedChunk(TextChunk chunk, float[] vector) {
        this.chunk = chunk;
        this.vector = vector;
    }

    public TextChunk getChunk() {
        return chunk;
    }

    public float[] getVector() {
        return vector;
    }
}
