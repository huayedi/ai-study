package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RAG 提示词构建器：把 Gate 状态与检索资料格式化为 system/user 消息文本。
 * <p>
 * <b>职责</b>：在 {@code prompt} 阶段生成约束模型行为的系统提示，以及含【教材资料】的用户提示；
 * 针对空/弱命中提供差异化文案，降低幻觉与伪引用风险。
 * <p>
 * <b>RAG 流水线位置</b>：{@code prompt}（gate 之后、generate 之前）。
 * <p>
 * <b>对应 Day</b>：Day13（空/弱命中分支文案）。
 * <p>
 * <b>与其他类关系</b>：
 * <ul>
 *   <li>消费 {@link RetrievedChunk} 与 {@link GateStrength}；</li>
 *   <li>被 {@link com.erp.ai.rag.service.RagService} 调用以组装 {@code ChatMessage}。</li>
 * </ul>
 */
@Component
public class RagPromptBuilder {

    /**
     * 标准（强命中路径）系统提示：要求仅依据资料、结构化 JSON 输出。
     *
     * @return 系统提示全文（已 trim）
     */
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

    /**
     * 弱命中系统提示：在标准提示上追加「依据不足 / 强制人工 / 置信度上限」约束。
     * <p>
     * 对应 Gate {@link GateStrength#WEAK}；空命中若仍调模型也可复用谨慎策略（由 Service 决定）。
     *
     * @return 弱命中增强后的系统提示
     */
    public String systemPromptWeak() {
        return systemPrompt() + """

                额外：当前检索为弱命中（相关分偏低）。你必须：
                - 明确说明依据不足 / 可能答非所问；
                - need_human 必须为 true；
                - confidence 不超过 0.45；
                - 不要假装资料充分。
                """.trim();
    }

    /**
     * 构造用户提示的便捷重载：默认按强命中状态排版资料（无弱/空状态头）。
     *
     * @param question  用户原问题
     * @param retrieved 门控后的命中列表；可空
     * @return 用户提示全文
     */
    public String userPrompt(String question, List<RetrievedChunk> retrieved) {
        return userPrompt(question, retrieved, GateStrength.STRONG);
    }

    /**
     * 按门控强度构造用户提示：状态头 + 教材资料块 + 用户问题。
     * <p>
     * <b>边界</b>：
     * <ul>
     *   <li>空命中：写入「空命中」状态，资料区显示「未检索到相关片段」；</li>
     *   <li>弱命中：写入谨慎作答状态头，仍可附带低分片段；</li>
     *   <li>{@code retrieved} 为 {@code null}/空时不伪造资料正文。</li>
     * </ul>
     *
     * @param question  用户原问题（原样追加，不做改写）
     * @param retrieved 命中列表
     * @param strength  门控强度，决定是否追加检索状态头
     * @return 完整用户侧提示字符串
     */
    public String userPrompt(String question, List<RetrievedChunk> retrieved, GateStrength strength) {
        StringBuilder sb = new StringBuilder();
        // 弱/空命中显式告知模型，避免把噪声片段当强证据
        if (strength == GateStrength.WEAK) {
            sb.append("【检索状态】弱命中：最高相关分偏低，请谨慎作答。\n");
        } else if (strength == GateStrength.EMPTY) {
            sb.append("【检索状态】空命中：没有任何教材片段。\n");
        }
        sb.append("【教材资料】\n");
        if (retrieved == null || retrieved.isEmpty()) {
            // 空命中或无片段：明确占位，禁止模型臆造引用
            sb.append("（未检索到相关片段）\n");
        } else {
            int i = 1;
            for (RetrievedChunk item : retrieved) {
                TextChunk chunk = item.getChunk();
                // 每条资料带文件/章节/分数元数据，便于模型与人工核对
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
