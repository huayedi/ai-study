package com.erp.ai.rag;

/**
 * 余弦相似度：dot(a,b) / (||a|| * ||b||)。
 */
public final class CosineSimilarity {

    private CosineSimilarity() {
    }

    public static double cosine(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("vectors must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "vector length mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA <= 1e-12 || normB <= 1e-12) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
