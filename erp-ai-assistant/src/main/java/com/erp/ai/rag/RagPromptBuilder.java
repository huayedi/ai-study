package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 把检索资料格式化进 RAG 提示词（Day13：空/弱命中分支文案）。
 */
@Component
public class RagPromptBuilder {

    public String systemPrompt() {
        return """
                你是常规 ERP 学习助手（纯学习项目）。请只根据用户消息中提供的【教材资料】回答。
                硬性规则：
                1. 优先依据资料；资料不足时明确说教材未覆盖，不要编造公司私有制度。
                2. 不编造实时库存/金额；不协助直接改账、删单、绕过审批。
                3. 回答结构：结论 → 要点/步骤 → 如有风险则提示。
                4. 只输出一个 JSON 对象，不要 Markdown 代码块。Schema：
                {"answer":"中文回答","need_human":true/false,"confidence":0.0到1.0}
                """.trim();
    }

    /** 弱命中：允许参考资料，但必须承认依据不足并建议人工。 */
    public String systemPromptWeak() {
        return systemPrompt() + """

                额外：当前检索为弱命中（相关分偏低）。你必须：
                - 明确说明依据不足 / 可能答非所问；
                - need_human 必须为 true；
                - confidence 不超过 0.45；
                - 不要假装资料充分。
                """.trim();
    }

    public String userPrompt(String question, List<RetrievedChunk> retrieved) {
        return userPrompt(question, retrieved, GateStrength.STRONG);
    }

    public String userPrompt(String question, List<RetrievedChunk> retrieved, GateStrength strength) {
        StringBuilder sb = new StringBuilder();
        if (strength == GateStrength.WEAK) {
            sb.append("【检索状态】弱命中：最高相关分偏低，请谨慎作答。\n");
        } else if (strength == GateStrength.EMPTY) {
            sb.append("【检索状态】空命中：没有任何教材片段。\n");
        }
        sb.append("【教材资料】\n");
        if (retrieved == null || retrieved.isEmpty()) {
            sb.append("（未检索到相关片段）\n");
        } else {
            int i = 1;
            for (RetrievedChunk item : retrieved) {
                TextChunk chunk = item.getChunk();
                sb.append("资料").append(i++)
                        .append(" | 文件=").append(chunk.getDocId())
                        .append(" | 章节=").append(chunk.getSection())
                        .append(" | 相关分=").append(String.format("%.4f", item.getScore()))
                        .append('\n')
                        .append(chunk.getContent())
                        .append("\n\n");
            }
        }
        sb.append("【用户问题】\n").append(question);
        return sb.toString();
    }
}
