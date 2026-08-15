package com.erp.ai.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadingChunkerTest {

    private final HeadingChunker chunker = new HeadingChunker();

    @Test
    void splitsByMarkdownHeadings() {
        String md = """
                来源：教材
                ## 1. 主链路
                物资请购到采购发票。
                ## 2. 字段
                供应商与存货编码。
                """;
        List<TextChunk> chunks = chunker.chunk("01.md", md);
        assertTrue(chunks.size() >= 2);
        assertTrue(chunks.stream().anyMatch(c -> c.getSection().contains("主链路")));
        assertTrue(chunks.stream().anyMatch(c -> c.getContent().contains("存货编码")));
        assertFalse(chunks.getFirst().getDocId().isBlank());
    }
}
