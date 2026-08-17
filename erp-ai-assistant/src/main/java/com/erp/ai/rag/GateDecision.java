package com.erp.ai.rag;

import java.util.List;

/**
 * Gate 决策结果（Day13）。
 */
public class GateDecision {

    private final GateStrength strength;
    private final List<RetrievedChunk> hits;
    private final String reason;
    private final double topScore;

    private GateDecision(GateStrength strength, List<RetrievedChunk> hits, String reason, double topScore) {
        this.strength = strength;
        this.hits = hits == null ? List.of() : List.copyOf(hits);
        this.reason = reason;
        this.topScore = topScore;
    }

    public static GateDecision empty() {
        return new GateDecision(GateStrength.EMPTY, List.of(), "no retrieved chunks", 0);
    }

    public static GateDecision weak(List<RetrievedChunk> hits, String reason) {
        double top = hits.isEmpty() ? 0 : hits.getFirst().getScore();
        return new GateDecision(GateStrength.WEAK, hits, reason, top);
    }

    public static GateDecision strong(List<RetrievedChunk> hits) {
        double top = hits.isEmpty() ? 0 : hits.getFirst().getScore();
        return new GateDecision(GateStrength.STRONG, hits, "top score above threshold", top);
    }

    public GateStrength getStrength() {
        return strength;
    }

    public List<RetrievedChunk> getHits() {
        return hits;
    }

    public String getReason() {
        return reason;
    }

    public double getTopScore() {
        return topScore;
    }

    public boolean isEmpty() {
        return strength == GateStrength.EMPTY;
    }

    public boolean isWeak() {
        return strength == GateStrength.WEAK;
    }
}
