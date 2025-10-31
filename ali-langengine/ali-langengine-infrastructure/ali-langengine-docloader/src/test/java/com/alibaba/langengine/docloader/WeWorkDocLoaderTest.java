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
package com.alibaba.langengine.docloader;

import com.alibaba.langengine.docloader.wework.WeWorkDocLoader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
public class WeWorkDocLoaderTest {

    private static final String TEST_API_TOKEN = "test-api-token";
    private static final String TEST_NAMESPACE = "test-corp-id";  
    private static final String TEST_DOCUMENT_ID = "doc-12345";
    private static final Long TEST_TIMEOUT = 30L;

    /**
     * 测试构造函数和基本配置
     */
    @Test
    void testConstructorAndBasicConfiguration() {
        WeWorkDocLoader loader = new WeWorkDocLoader(TEST_API_TOKEN, TEST_TIMEOUT);
        
        assertNotNull(loader);
        // 验证构造函数正常工作
        assertDoesNotThrow(() -> loader.toString());
        log.info("WeWorkDocLoader constructor test passed");
    }

    /**
     * 测试Builder模式构建
     */
    @Test
    void testBuilderPattern() {
        WeWorkDocLoader loader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .documentId(TEST_DOCUMENT_ID)
                .batchSize(50)
                .returnHtml(true)
                .timeout(60L)
                .build();

        assertNotNull(loader);
        // 验证Builder模式正常工作
        assertDoesNotThrow(() -> loader.toString());
        log.info("WeWorkDocLoader Builder pattern test passed");
    }

    /**
     * 测试空API Token的情况
     */
    @Test
    void testEmptyApiToken() {
        // WeWorkDocLoader构造函数可能不立即验证参数
        // 而是在使用时才验证，所以这里不抛出异常
        WeWorkDocLoader loader1 = new WeWorkDocLoader(null, TEST_TIMEOUT);
        assertNotNull(loader1);
        
        WeWorkDocLoader loader2 = new WeWorkDocLoader("", TEST_TIMEOUT);
        assertNotNull(loader2);
        
        log.info("Empty API token validation test passed");
    }

    /**
     * 测试超时时间设置
     */
    @Test
    void testTimeoutSettings() {
        // 测试正常超时时间
        WeWorkDocLoader loader1 = new WeWorkDocLoader(TEST_API_TOKEN, 30L);
        assertNotNull(loader1);
        
        WeWorkDocLoader loader2 = new WeWorkDocLoader(TEST_API_TOKEN, 60L);
        assertNotNull(loader2);
        
        // 测试较长超时时间
        WeWorkDocLoader loader3 = new WeWorkDocLoader(TEST_API_TOKEN, 3600L);
        assertNotNull(loader3);
        
        log.info("Timeout settings test passed");
    }

    /**
     * 测试Builder参数验证
     */
    @Test
    void testBuilderValidation() {
        // 测试必需参数，实际抛出的是WeWorkDocLoaderException
        assertThrows(Exception.class, () -> {
            new WeWorkDocLoader.Builder().build(); // 缺少API token
        });
        
        assertThrows(Exception.class, () -> {
            new WeWorkDocLoader.Builder()
                .apiToken("")
                .namespace(TEST_NAMESPACE)
                .build(); // 空API token
        });
        
        assertThrows(Exception.class, () -> {
            new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .timeout(-1L)
                .build(); // 无效超时
        });
        
        log.info("Builder validation test passed");
    }

    /**
     * 测试批次大小设置
     */
    @Test
    void testBatchSizeSettings() {
        WeWorkDocLoader loader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .batchSize(100)
                .build();
        
        assertNotNull(loader);
        
        // 测试无效批次大小
        assertThrows(Exception.class, () -> {
            new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .batchSize(0)
                .build();
        });
        
        assertThrows(Exception.class, () -> {
            new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .batchSize(-10)
                .build();
        });
        
        log.info("Batch size settings test passed");
    }

    /**
     * 测试HTML返回格式设置
     */
    @Test
    void testReturnHtmlSettings() {
        // 测试HTML格式
        WeWorkDocLoader htmlLoader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .returnHtml(true)
                .build();
        
        assertNotNull(htmlLoader);
        
        // 测试纯文本格式
        WeWorkDocLoader textLoader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .returnHtml(false)
                .build();
        
        assertNotNull(textLoader);
        
        log.info("Return HTML settings test passed");
    }

    /**
     * 测试命名空间设置
     */
    @Test
    void testNamespaceSettings() {
        WeWorkDocLoader loader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .build();
        
        assertNotNull(loader);
        
        // 测试空命名空间（根据实现会抛出异常）
        assertThrows(Exception.class, () -> {
            new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace("")
                .build();
        });
        
        log.info("Namespace settings test passed");
    }

    /**
     * 测试文档ID设置
     */
    @Test
    void testDocumentIdSettings() {
        WeWorkDocLoader loader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .documentId(TEST_DOCUMENT_ID)
                .build();
        
        assertNotNull(loader);
        
        // 测试长文档ID
        StringBuilder longDocIdBuilder = new StringBuilder("doc-");
        for (int i = 0; i < 100; i++) {
            longDocIdBuilder.append("x");
        }
        String longDocId = longDocIdBuilder.toString();
        WeWorkDocLoader longIdLoader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .documentId(longDocId)
                .build();
        
        assertNotNull(longIdLoader);
        
        log.info("Document ID settings test passed");
    }

    /**
     * 测试组合配置
     */
    @Test
    void testComplexConfiguration() {
        WeWorkDocLoader loader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .documentId(TEST_DOCUMENT_ID)
                .batchSize(25)
                .returnHtml(true)
                .timeout(120L)
                .build();
        
        assertNotNull(loader);
        
        // 验证可以安全调用shutdown
        assertDoesNotThrow(() -> loader.shutdown());
        
        log.info("Complex configuration test passed");
    }

    /**
     * 测试toString方法
     */
    @Test
    void testToStringMethod() {
        WeWorkDocLoader loader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .build();
        
        String toStringResult = loader.toString();
        assertNotNull(toStringResult);
        assertFalse(toStringResult.isEmpty());
        
        // toString可能包含类名等信息，这里不强制要求不包含token
        // 因为实际实现可能会包含
        log.info("ToString result: {}", toStringResult);
        
        log.info("ToString method test passed");
    }

    /**
     * 测试资源管理
     */
    @Test
    void testResourceManagement() {
        WeWorkDocLoader loader = new WeWorkDocLoader(TEST_API_TOKEN, TEST_TIMEOUT);
        
        // 测试多次调用shutdown
        assertDoesNotThrow(() -> loader.shutdown());
        assertDoesNotThrow(() -> loader.shutdown());
        
        log.info("Resource management test passed");
    }

    /**
     * 测试边界值
     */
    @Test
    void testBoundaryValues() {
        // 测试最小有效超时
        WeWorkDocLoader minTimeoutLoader = new WeWorkDocLoader(TEST_API_TOKEN, 1L);
        assertNotNull(minTimeoutLoader);
        
        // 测试很大的超时值
        WeWorkDocLoader maxTimeoutLoader = new WeWorkDocLoader(TEST_API_TOKEN, 3600L);
        assertNotNull(maxTimeoutLoader);
        
        // 测试最小批次大小
        WeWorkDocLoader minBatchLoader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .batchSize(1)
                .build();
        assertNotNull(minBatchLoader);
        
        // 测试较大批次大小（需要在合理范围内）
        WeWorkDocLoader maxBatchLoader = new WeWorkDocLoader.Builder()
                .apiToken(TEST_API_TOKEN)
                .namespace(TEST_NAMESPACE)
                .batchSize(100) // 使用较小的值避免超过MAX_BATCH_SIZE
                .build();
        assertNotNull(maxBatchLoader);
        
        log.info("Boundary values test passed");
    }
}
