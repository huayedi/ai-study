package com.erp.ai.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrievalGateTest {

    private final RetrievalGate gate = new RetrievalGate();

    @Test
    void emptyWhenNoHits() {
        GateDecision d = gate.decide(List.of(), 0.02);
        assertEquals(GateStrength.EMPTY, d.getStrength());
        assertTrue(d.getHits().isEmpty());
    }

    @Test
    void weakWhenBelowMinScore() {
        TextChunk chunk = new TextChunk("1", "a.md", "s", "content");
        GateDecision d = gate.decide(List.of(new RetrievedChunk(chunk, 0.01)), 0.02);
        assertEquals(GateStrength.WEAK, d.getStrength());
        assertEquals(1, d.getHits().size());
    }

    @Test
    void strongWhenAboveMinScore() {
        TextChunk chunk = new TextChunk("1", "a.md", "s", "content");
        GateDecision d = gate.decide(List.of(new RetrievedChunk(chunk, 0.5)), 0.02);
        assertEquals(GateStrength.STRONG, d.getStrength());
    }
}
