package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 关键词检索器：中文友好的极简词项重叠打分（学习版 BM25 替代方案）。
 * <p>
 * <b>职责</b>：将问题与每个 chunk 的 {@link TextChunk#searchableText()} 分词后，
 * 以「查询词命中比例」作为分数，返回 TopK。
 * <p>
 * <b>RAG 流水线位置</b>：{@code retrieve}。
 * <p>
 * <b>对应 Day</b>：Day11/12；亦作为 Day24 向量/混合失败时的降级路径。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>实现 {@link RagRetriever}，与 {@link VectorRetriever} 并列；</li>
 *   <li>被 {@link HybridRetriever} 作为 keyword 支路召回；</li>
 *   <li>被 {@link com.erp.ai.rag.service.RagService} 在配置或 fallback 时选中。</li>
 * </ul>
 * <p>
 * <b>分数量纲</b>：{@code overlap / |queryTerms|}，范围约 {@code (0, 1]}；无重叠则不入榜。
 */
@Component
public class KeywordRetriever implements RagRetriever {

    /**
     * 分词分隔：连续非「汉字/字母/数字」的字符视为分隔符。
     * 保留汉字与字母数字 token，便于中英混排问题。
     */
    private static final Pattern SPLIT = Pattern.compile("[^\\p{IsHan}A-Za-z0-9]+");

    /**
     * 关键词重叠检索。
     * <p>
     * <b>算法步骤</b>：
     * <ol>
     *   <li>对问题 {@link #tokenize}，得到查询词集合；</li>
     *   <li>边界：查询词空、语料空、{@code topK<=0} → 空列表；</li>
     *   <li>对每个 chunk 分词，统计与查询词的命中个数 {@code overlap}；</li>
     *   <li>{@code overlap==0} 跳过（无弱正分噪声）；</li>
     *   <li>分数 = {@code overlap / queryTerms.size()}；</li>
     *   <li>按分数降序、id 升序取 TopK。</li>
     * </ol>
     *
     * @param question 用户问题
     * @param corpus   全量文本块；{@code null} 或空视为空语料
     * @param topK     返回上限
     * @return 命中列表；无重叠时为空（可能导致 Gate EMPTY）
     */
    @Override
    public List<RetrievedChunk> retrieve(String question, List<TextChunk> corpus, int topK) {
        // 步骤 1：问题分词（含中文 bigram）
        Set<String> queryTerms = tokenize(question);
        // 步骤 2：空查询 / 空语料 / 非法 topK → 空命中原料
        if (queryTerms.isEmpty() || corpus == null || corpus.isEmpty() || topK <= 0) {
            return List.of();
        }

        List<RetrievedChunk> scored = new ArrayList<>();
        for (TextChunk chunk : corpus) {
            // 步骤 3：文档侧分词（section+content）
            Set<String> docTerms = tokenize(chunk.searchableText());
            int overlap = 0;
            for (String term : queryTerms) {
                if (docTerms.contains(term)) {
                    overlap++;
                }
            }
            // 步骤 4：零重叠不入候选（避免全库低分刷屏）
            if (overlap == 0) {
                continue;
            }
            // 步骤 5：命中比例作为分数（弱命中仍可能低于 Gate minScore）
            double score = overlap / (double) queryTerms.size();
            scored.add(new RetrievedChunk(chunk, score));
        }

        // 步骤 6：稳定排序后截断 TopK
        return scored.stream()
                .sorted(Comparator.comparingDouble(RetrievedChunk::getScore).reversed()
                        .thenComparing(r -> r.getChunk().getId()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * @return 固定名 {@code keyword}，供配置选择与降级判断
     */
    @Override
    public String name() {
        return "keyword";
    }

    /**
     * 中英友好分词：分隔符切词 + 连续汉字 bigram。
     * <p>
     * <b>步骤</b>：
     * <ol>
     *   <li>空/空白 → 空集合；</li>
     *   <li>小写化后按 {@link #SPLIT} 切开；</li>
     *   <li>去掉空白与单字符 token（过短噪声）；</li>
     *   <li>对纯汉字串滑动窗口加 bigram，提高短中文问句命中率。</li>
     * </ol>
     *
     * @param text 原始文本
     * @return 去重后的词项集合；无法分词时为空
     */
    static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        // 统一小写，便于英文词匹配
        String normalized = text.toLowerCase(Locale.ROOT);
        Set<String> terms = new HashSet<>(Arrays.asList(SPLIT.split(normalized)));
        // 过滤空串与单字（单汉字/单字母噪声大）
        terms.removeIf(t -> t == null || t.isBlank() || t.length() == 1);
        // 对连续中文再做 bigram，提高短问句命中率
        String hanOnly = normalized.replaceAll("[^\\p{IsHan}]", "");
        for (int i = 0; i + 1 < hanOnly.length(); i++) {
            terms.add(hanOnly.substring(i, i + 2));
        }
        return terms;
    }
}
