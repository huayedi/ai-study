package com.erp.ai.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashEmbeddingClientTest {

    @Test
    void sameTextProducesSameVector() {
        HashEmbeddingClient client = new HashEmbeddingClient(64);
        float[] a = client.embed("采购入库");
        float[] b = client.embed("采购入库");
        assertEquals(64, a.length);
        double cos = 0;
        for (int i = 0; i < a.length; i++) {
            cos += a[i] * b[i];
        }
        assertEquals(1.0, cos, 1e-5);
    }

    @Test
    void blankTextReturnsZeroVector() {
        HashEmbeddingClient client = new HashEmbeddingClient(32);
        float[] v = client.embed("   ");
        double sum = 0;
        for (float x : v) {
            sum += Math.abs(x);
        }
        assertEquals(0.0, sum, 1e-9);
    }

    @Test
    void differentTextsUsuallyDiffer() {
        HashEmbeddingClient client = new HashEmbeddingClient(128);
        float[] a = client.embed("采购入库");
        float[] b = client.embed("会计期间关闭");
        assertTrue(dot(a, b) < 0.999);
    }

    private static double dot(float[] a, float[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += a[i] * b[i];
        }
        return s;
    }
}
