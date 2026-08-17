package com.erp.ai.rag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CosineSimilarityTest {

    @Test
    void identicalVectorsScoreOne() {
        float[] a = {1f, 0f, 0f};
        assertEquals(1.0, CosineSimilarity.cosine(a, a), 1e-6);
    }

    @Test
    void orthogonalVectorsScoreZero() {
        float[] a = {1f, 0f};
        float[] b = {0f, 1f};
        assertEquals(0.0, CosineSimilarity.cosine(a, b), 1e-6);
    }

    @Test
    void lengthMismatchThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CosineSimilarity.cosine(new float[]{1f}, new float[]{1f, 2f}));
    }

    @Test
    void zeroVectorReturnsZero() {
        assertEquals(0.0, CosineSimilarity.cosine(new float[]{0f, 0f}, new float[]{1f, 2f}), 1e-6);
    }

    @Test
    void similarDirectionHigherThanOpposite() {
        float[] q = {1f, 1f, 0f};
        float[] near = {0.9f, 1.1f, 0.05f};
        float[] far = {-1f, -1f, 0f};
        assertTrue(CosineSimilarity.cosine(q, near) > CosineSimilarity.cosine(q, far));
    }
}
