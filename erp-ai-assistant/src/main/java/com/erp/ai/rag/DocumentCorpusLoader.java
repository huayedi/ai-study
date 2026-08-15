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
 * 从 classpath 加载 Markdown 教材全文。
 */
@Component
public class DocumentCorpusLoader {

    public List<LoadedDocument> loadMarkdownDocs(String classpathDirectory) {
        String pattern = "classpath*:" + trimSlashes(classpathDirectory) + "/*.md";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(pattern);
            List<LoadedDocument> docs = new ArrayList<>();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }
                String filename = resource.getFilename();
                if (filename == null || filename.startsWith("README")) {
                    continue;
                }
                String body = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                docs.add(new LoadedDocument(filename, body));
            }
            docs.sort(Comparator.comparing(LoadedDocument::filename));
            return docs;
        } catch (IOException ex) {
            throw new IllegalStateException("无法加载 RAG 教材目录: " + pattern, ex);
        }
    }

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

    public record LoadedDocument(String filename, String content) {
    }
}
