/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.usearch.vectorstore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;


class VectorUtilsTest {

    @Test
    void testVectorConversionFromDouble() {
        // 测试从Double列表转换为float数组
        List<Double> doubleVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        float[] floatArray = convertToFloatArray(doubleVector);
        
        assertEquals(5, floatArray.length);
        assertEquals(0.1f, floatArray[0], 0.001f);
        assertEquals(0.2f, floatArray[1], 0.001f);
        assertEquals(0.3f, floatArray[2], 0.001f);
        assertEquals(0.4f, floatArray[3], 0.001f);
        assertEquals(0.5f, floatArray[4], 0.001f);
    }

    @Test
    void testVectorConversionNullInput() {
        // 测试null输入的处理
        float[] result = convertToFloatArray(null);
        assertNull(result);
    }

    @Test
    void testVectorConversionEmptyList() {
        // 测试空列表的处理
        List<Double> emptyList = Arrays.asList();
        float[] result = convertToFloatArray(emptyList);
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testCosineDistanceCalculation() {
        // 测试余弦距离计算
        float[] vector1 = {1.0f, 0.0f, 0.0f};
        float[] vector2 = {0.0f, 1.0f, 0.0f};
        
        double distance = calculateCosineDistance(vector1, vector2);
        assertEquals(1.0, distance, 0.001); // 垂直向量的余弦距离为1
    }

    @Test
    void testCosineDistanceIdenticalVectors() {
        // 测试相同向量的余弦距离
        float[] vector1 = {1.0f, 2.0f, 3.0f};
        float[] vector2 = {1.0f, 2.0f, 3.0f};
        
        double distance = calculateCosineDistance(vector1, vector2);
        assertEquals(0.0, distance, 0.001); // 相同向量的余弦距离为0
    }

    @Test
    void testCosineDistanceOppositeVectors() {
        // 测试相反向量的余弦距离
        float[] vector1 = {1.0f, 0.0f, 0.0f};
        float[] vector2 = {-1.0f, 0.0f, 0.0f};
        
        double distance = calculateCosineDistance(vector1, vector2);
        assertEquals(2.0, distance, 0.001); // 相反向量的余弦距离为2
    }

    @Test
    void testCosineDistanceZeroVector() {
        // 测试零向量的处理
        float[] vector1 = {1.0f, 2.0f, 3.0f};
        float[] zeroVector = {0.0f, 0.0f, 0.0f};
        
        double distance = calculateCosineDistance(vector1, zeroVector);
        assertEquals(1.0, distance, 0.001); // 零向量的余弦距离应该返回1.0（最大距离）
    }

    @Test
    void testEuclideanDistanceCalculation() {
        // 测试欧几里得距离计算
        float[] vector1 = {0.0f, 0.0f, 0.0f};
        float[] vector2 = {3.0f, 4.0f, 0.0f};
        
        double distance = calculateEuclideanDistance(vector1, vector2);
        assertEquals(5.0, distance, 0.001); // 3-4-5直角三角形
    }

    @Test
    void testEuclideanDistanceIdenticalVectors() {
        // 测试相同向量的欧几里得距离
        float[] vector1 = {1.0f, 2.0f, 3.0f};
        float[] vector2 = {1.0f, 2.0f, 3.0f};
        
        double distance = calculateEuclideanDistance(vector1, vector2);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void testInnerProductCalculation() {
        // 测试内积计算
        float[] vector1 = {1.0f, 2.0f, 3.0f};
        float[] vector2 = {4.0f, 5.0f, 6.0f};
        
        double innerProduct = calculateInnerProduct(vector1, vector2);
        assertEquals(32.0, innerProduct, 0.001); // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
    }

    @Test
    void testVectorNormalization() {
        // 测试向量归一化
        float[] vector = {3.0f, 4.0f, 0.0f}; // 长度为5的向量
        float[] normalized = normalizeVector(vector);
        
        // 验证归一化后的向量长度为1
        double length = calculateVectorLength(normalized);
        assertEquals(1.0, length, 0.001);
        
        // 验证方向保持不变
        assertEquals(0.6f, normalized[0], 0.001f); // 3/5
        assertEquals(0.8f, normalized[1], 0.001f); // 4/5
        assertEquals(0.0f, normalized[2], 0.001f);
    }

    @Test
    void testVectorNormalizationZeroVector() {
        // 测试零向量的归一化
        float[] zeroVector = {0.0f, 0.0f, 0.0f};
        float[] result = normalizeVector(zeroVector);
        
        // 零向量无法归一化，应该返回原向量或特殊处理
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    void testVectorLength() {
        // 测试向量长度计算
        float[] vector = {3.0f, 4.0f, 0.0f};
        double length = calculateVectorLength(vector);
        assertEquals(5.0, length, 0.001);
    }

    @Test
    void testVectorLengthUnitVector() {
        // 测试单位向量的长度
        float[] unitVector = {1.0f, 0.0f, 0.0f};
        double length = calculateVectorLength(unitVector);
        assertEquals(1.0, length, 0.001);
    }

    @Test
    void testVectorDimensionMismatch() {
        // 测试不同维度向量的处理
        float[] vector1 = {1.0f, 2.0f, 3.0f};
        float[] vector2 = {4.0f, 5.0f};
        
        assertThrows(IllegalArgumentException.class, () -> {
            calculateCosineDistance(vector1, vector2);
        });
    }

    @Test
    void testLargeVectorCalculation() {
        // 测试大向量的计算
        int dimension = 1000;
        float[] vector1 = new float[dimension];
        float[] vector2 = new float[dimension];
        
        for (int i = 0; i < dimension; i++) {
            vector1[i] = 1.0f;
            vector2[i] = 1.0f;
        }
        
        double distance = calculateCosineDistance(vector1, vector2);
        assertEquals(0.0, distance, 0.001); // 相同向量的距离为0
        
        double euclideanDistance = calculateEuclideanDistance(vector1, vector2);
        assertEquals(0.0, euclideanDistance, 0.001);
    }

    @Test
    void testPrecisionHandling() {
        // 测试精度处理
        float[] vector1 = {0.123456789f, 0.987654321f};
        float[] vector2 = {0.123456789f, 0.987654321f};
        
        double distance = calculateCosineDistance(vector1, vector2);
        assertTrue(Math.abs(distance) < 0.001); // 应该非常接近0
    }

    // 辅助方法实现
    private float[] convertToFloatArray(List<Double> doubleList) {
        if (doubleList == null) return null;
        float[] result = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            result[i] = doubleList.get(i).floatValue();
        }
        return result;
    }

    private double calculateCosineDistance(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 1.0; // 如果有零向量，返回最大距离
        }
        
        double cosine = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return 1.0 - cosine; // 余弦距离 = 1 - 余弦相似度
    }

    private double calculateEuclideanDistance(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            double diff = vector1[i] - vector2[i];
            sum += diff * diff;
        }
        
        return Math.sqrt(sum);
    }

    private double calculateInnerProduct(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        double product = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            product += vector1[i] * vector2[i];
        }
        
        return product;
    }

    private float[] normalizeVector(float[] vector) {
        double norm = calculateVectorLength(vector);
        
        if (norm == 0.0) {
            return vector.clone(); // 零向量无法归一化
        }
        
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / norm);
        }
        return normalized;
    }

    private double calculateVectorLength(float[] vector) {
        double sum = 0.0;
        for (float v : vector) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }
}
