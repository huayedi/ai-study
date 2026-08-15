package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 按 Markdown 二级/三级标题切分；单块过长再按字符窗口切开。
 */
@Component
public class HeadingChunker {

    private static final int MAX_CHARS = 800;

    public List<TextChunk> chunk(String docId, String markdown) {
        List<Section> sections = splitByHeadings(markdown);
        List<TextChunk> chunks = new ArrayList<>();
        int index = 0;
        for (Section section : sections) {
            List<String> parts = splitLong(section.body());
            for (int i = 0; i < parts.size(); i++) {
                String sectionLabel = parts.size() == 1
                        ? section.title()
                        : section.title() + " (" + (i + 1) + "/" + parts.size() + ")";
                String id = docId + "#" + (index++);
                chunks.add(new TextChunk(id, docId, sectionLabel, parts.get(i).trim()));
            }
        }
        return chunks.stream().filter(c -> !c.getContent().isBlank()).toList();
    }

    private static List<Section> splitByHeadings(String markdown) {
        String[] lines = markdown.replace("\r\n", "\n").split("\n");
        List<Section> sections = new ArrayList<>();
        String currentTitle = "前言";
        StringBuilder body = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("## ") || line.startsWith("### ")) {
                sections.add(new Section(currentTitle, body.toString().trim()));
                currentTitle = line.replaceFirst("^#+\\s*", "").trim();
                body = new StringBuilder();
            } else {
                body.append(line).append('\n');
            }
        }
        sections.add(new Section(currentTitle, body.toString().trim()));
        return sections;
    }

    private static List<String> splitLong(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        if (text.length() <= MAX_CHARS) {
            return List.of(text);
        }
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + MAX_CHARS);
            parts.add(text.substring(start, end));
            start = end;
        }
        return parts;
    }

    private record Section(String title, String body) {
    }
}
