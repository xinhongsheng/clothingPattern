package com.xhs.clothingpatternbackend.utils;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-12
 * @Description: 余弦相似度工具类
 * @Version: 1.0
 */
public class VectorUtils {
    /**
     * 计算两个向量的余弦相似度
     * @param v1 向量A
     * @param v2 向量B
     * @return 相似度 (0.0 ~ 1.0，越接近1越相似)
     */
    public static double cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must not be null and strictly of the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
