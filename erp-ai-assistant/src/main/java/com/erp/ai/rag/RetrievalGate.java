package com.erp.ai.rag;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 检索门控（Day13）：空命中 / 弱命中 / 强命中。
 * <p>
 * 注意：keyword 分、向量余弦、RRF 融合分量纲不同；{@code minScore} 需按当前 retriever 理解与标定。
 */
@Component
public class RetrievalGate {

    public GateDecision decide(List<RetrievedChunk> hits, double minScore) {
        if (hits == null || hits.isEmpty()) {
            return GateDecision.empty();
        }
        double top = hits.getFirst().getScore();
        if (top < minScore) {
            return GateDecision.weak(hits, "top score " + format(top) + " below minScore " + format(minScore));
        }
        return GateDecision.strong(hits);
    }

    private static String format(double v) {
        return String.format("%.4f", v);
    }
}
