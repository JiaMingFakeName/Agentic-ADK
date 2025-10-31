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
package com.alibaba.langengine.lucene;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;


public class LuceneUtilsTest {

    @Test
    @DisplayName("测试查询参数验证")
    public void testValidateSearchParams() {
        // 正常情况
        assertDoesNotThrow(() -> LuceneUtils.validateSearchParams("test query", 10));
        
        // 异常情况
        assertThrows(IllegalArgumentException.class, 
            () -> LuceneUtils.validateSearchParams("", 10));
        assertThrows(IllegalArgumentException.class, 
            () -> LuceneUtils.validateSearchParams(null, 10));
        assertThrows(IllegalArgumentException.class, 
            () -> LuceneUtils.validateSearchParams("test", 0));
        assertThrows(IllegalArgumentException.class, 
            () -> LuceneUtils.validateSearchParams("test", -1));
    }

    @Test
    @DisplayName("测试查询字符串清理")
    public void testSanitizeQuery() {
        assertEquals("test query", LuceneUtils.sanitizeQuery("test+query"));
        assertEquals("test query", LuceneUtils.sanitizeQuery("test-query"));
        assertEquals("test query", LuceneUtils.sanitizeQuery("test*query"));
        assertEquals("test query", LuceneUtils.sanitizeQuery("test?query"));
        assertEquals("", LuceneUtils.sanitizeQuery(""));
        assertEquals("", LuceneUtils.sanitizeQuery(null));
        assertEquals("test query", LuceneUtils.sanitizeQuery("  test   query  "));
    }

    @Test
    @DisplayName("测试向量转换")
    public void testConvertToFloatArray() {
        // 正常情况
        java.util.List<Double> input = java.util.Arrays.asList(1.0, 2.5, 3.7);
        float[] result = LuceneUtils.convertToFloatArray(input);
        assertEquals(3, result.length);
        assertEquals(1.0f, result[0], 0.001f);
        assertEquals(2.5f, result[1], 0.001f);
        assertEquals(3.7f, result[2], 0.001f);
        
        // 空列表
        assertEquals(0, LuceneUtils.convertToFloatArray(java.util.Collections.emptyList()).length);
        assertEquals(0, LuceneUtils.convertToFloatArray(null).length);
        
        // 包含null值
        java.util.List<Double> nullInput = java.util.Arrays.asList(1.0, null, 3.0);
        float[] nullResult = LuceneUtils.convertToFloatArray(nullInput);
        assertEquals(3, nullResult.length);
        assertEquals(1.0f, nullResult[0], 0.001f);
        assertEquals(0.0f, nullResult[1], 0.001f);
        assertEquals(3.0f, nullResult[2], 0.001f);
    }

    @Test
    @DisplayName("测试索引路径验证")
    public void testIsValidIndexPath() {
        // 有效路径
        assertTrue(LuceneUtils.isValidIndexPath("/tmp/test"));
        assertTrue(LuceneUtils.isValidIndexPath("./test"));
        assertTrue(LuceneUtils.isValidIndexPath("C:\\temp\\test"));
        
        // 无效路径
        assertFalse(LuceneUtils.isValidIndexPath(""));
        assertFalse(LuceneUtils.isValidIndexPath(null));
    }
}
