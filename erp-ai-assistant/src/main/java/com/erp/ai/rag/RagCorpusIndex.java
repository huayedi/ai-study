package com.erp.ai.rag;

import com.erp.ai.common.config.AiProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 内存教材语料索引：启动时加载并切分全部 Markdown，供检索器只读访问。
 * <p>
 * <b>职责</b>：编排 {@code load → chunk}，缓存不可变 {@link TextChunk} 列表。
 * <p>
 * <b>RAG 流水线位置</b>：{@code load} + {@code chunk} 的落地索引（进程内学习版，非外部向量库）。
 * <p>
 * <b>对应 Day</b>：Day10/11。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>依赖 {@link DocumentCorpusLoader}、{@link HeadingChunker}、{@link AiProperties}；</li>
 *   <li>被 {@link KeywordRetriever}/{@link HybridRetriever} 与 {@link VectorCorpusIndex} 消费；</li>
 *   <li>{@link com.erp.ai.rag.service.RagService} 通过 {@link #allChunks()} 传入检索。</li>
 * </ul>
 * <p>
 * <b>空语料</b>：目录无 md 时 {@code chunks} 为空列表，后续检索自然 EMPTY。
 */
@Component
public class RagCorpusIndex {

    /** 启动与重建日志 */
    private static final Logger log = LoggerFactory.getLogger(RagCorpusIndex.class);

    /** RAG 配置（含 classpath 教材目录） */
    private final AiProperties properties;

    /** Markdown 加载器 */
    private final DocumentCorpusLoader loader;

    /** 标题切分器 */
    private final HeadingChunker chunker;

    /** 全量文本块（不可变）；默认空列表，{@link #rebuild()} 后替换 */
    private List<TextChunk> chunks = List.of();

    /**
     * @param properties AI/RAG 配置
     * @param loader     语料加载器
     * @param chunker    切分器
     */
    public RagCorpusIndex(AiProperties properties, DocumentCorpusLoader loader, HeadingChunker chunker) {
        this.properties = properties;
        this.loader = loader;
        this.chunker = chunker;
    }

    /**
     * Spring 启动后重建索引：加载目录下全部 md → 逐篇切分 → 固化为不可变列表。
     * <p>
     * 空语料时不抛错，仅记录 docs=0/chunks=0，由检索阶段表现为空命中。
     *
     * @throws IllegalStateException 当加载器 I/O 失败时向上抛出（来自 loader）
     */
    @PostConstruct
    public void rebuild() {
        String dir = properties.getRag().getClasspathDocs();
        // load：classpath Markdown
        List<DocumentCorpusLoader.LoadedDocument> docs = loader.loadMarkdownDocs(dir);
        List<TextChunk> built = new ArrayList<>();
        // chunk：按标题切分并汇总
        for (DocumentCorpusLoader.LoadedDocument doc : docs) {
            built.addAll(chunker.chunk(doc.filename(), doc.content()));
        }
        this.chunks = Collections.unmodifiableList(built);
        log.info("RAG corpus ready: docs={}, chunks={}, dir=classpath:{}", docs.size(), chunks.size(), dir);
    }

    /**
     * 返回全量文本块只读视图。
     *
     * @return 不可变列表；空语料时为空列表
     */
    public List<TextChunk> allChunks() {
        return chunks;
    }
}
