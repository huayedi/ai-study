package com.erp.ai.rag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 教材语料加载器：从 classpath 读取 Markdown 文档全文。
 * <p>
 * <b>职责</b>：按目录模式扫描 {@code *.md}，过滤 README，读入 UTF-8 正文，
 * 封装为 {@link LoadedDocument} 列表并按文件名排序，供索引阶段消费。
 * <p>
 * <b>RAG 流水线位置</b>：{@code load}（流水线最前端）。
 * 输出交给 {@link HeadingChunker}（chunk）再进入 {@link RagCorpusIndex}。
 * <p>
 * <b>对应 Day</b>：Day10/11（语料装载与内存索引）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>被 {@link RagCorpusIndex#rebuild()} 调用；</li>
 *   <li>不直接参与 retrieve / gate / prompt / generate。</li>
 * </ul>
 * <p>
 * <b>空语料边界</b>：目录无可读 md、或仅有 README 时返回空列表（不抛业务异常）；
 * I/O 失败则包装为 {@link IllegalStateException}。
 */
@Component
public class DocumentCorpusLoader {

    /**
     * 从指定 classpath 目录加载全部 Markdown 教材。
     * <p>
     * 扫描模式：{@code classpath*:&lt;dir&gt;/*.md}。
     * 跳过不可读资源、文件名为 {@code null}、以及以 {@code README} 开头的说明文件。
     *
     * @param classpathDirectory classpath 相对目录（可带首尾斜杠，会被剥除），例如 {@code rag-docs}
     * @return 按文件名升序排列的已加载文档列表；无匹配时为空列表（空语料）
     * @throws IllegalStateException 当资源解析或读取发生 {@link IOException} 时
     */
    public List<LoadedDocument> loadMarkdownDocs(String classpathDirectory) {
        // 构造 Ant 风格 classpath 扫描模式
        String pattern = "classpath*:" + trimSlashes(classpathDirectory) + "/*.md";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(pattern);
            List<LoadedDocument> docs = new ArrayList<>();
            for (Resource resource : resources) {
                // 不可读资源直接跳过（空语料的一种局部情况）
                if (!resource.isReadable()) {
                    continue;
                }
                String filename = resource.getFilename();
                // README 类说明文件不作为教材进入索引
                if (filename == null || filename.startsWith("README")) {
                    continue;
                }
                // 全文按 UTF-8 读入内存（学习版：不做流式切分）
                String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                docs.add(new LoadedDocument(filename, body));
            }
            // 稳定排序，保证多次启动 chunk id / 索引顺序一致
            docs.sort(Comparator.comparing(LoadedDocument::filename));
            return docs;
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载 RAG 教材目录: " + pattern, ex);
        }
    }

    /**
     * 去掉路径首尾斜杠，避免 {@code classpath*:/foo/*.md} 或尾斜杠导致模式异常。
     *
     * @param path 原始目录字符串，可为 {@code null}
     * @return 规范化后的相对路径；{@code null} 视为空串
     */
    private static String trimSlashes(String path) {
        String p = path == null ? "" : path.trim();
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        while (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /**
     * 单份已加载 Markdown 文档。
     * <p>
     * 作为 load → chunk 的数据契约：{@code filename} 通常用作 {@link TextChunk} 的 {@code docId}。
     *
     * @param filename 资源文件名（如 {@code inventory.md}），非完整 classpath
     * @param content  文档 UTF-8 全文（含 Markdown 标题与正文）
     */
    public record LoadedDocument(String filename, String content) {
    }
}
