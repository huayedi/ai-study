package com.erp.ai.rag;

import com.erp.ai.config.AiProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 启动时构建内存教材索引（学习版，进程内）。
 */
@Component
public class RagCorpusIndex {

    private static final Logger log = LoggerFactory.getLogger(RagCorpusIndex.class);

    private final AiProperties properties;
    private final DocumentCorpusLoader loader;
    private final HeadingChunker chunker;

    private List<TextChunk> chunks = List.of();

    public RagCorpusIndex(AiProperties properties, DocumentCorpusLoader loader, HeadingChunker chunker) {
        this.properties = properties;
        this.loader = loader;
        this.chunker = chunker;
    }

    @PostConstruct
    public void rebuild() {
        String dir = properties.getRag().getClasspathDocs();
        List<DocumentCorpusLoader.LoadedDocument> docs = loader.loadMarkdownDocs(dir);
        List<TextChunk> built = new ArrayList<>();
        for (DocumentCorpusLoader.LoadedDocument doc : docs) {
            built.addAll(chunker.chunk(doc.filename(), doc.content()));
        }
        this.chunks = Collections.unmodifiableList(built);
        log.info("RAG corpus ready: docs={}, chunks={}, dir=classpath:{}", docs.size(), chunks.size(), dir);
    }

    public List<TextChunk> allChunks() {
        return chunks;
    }
}
