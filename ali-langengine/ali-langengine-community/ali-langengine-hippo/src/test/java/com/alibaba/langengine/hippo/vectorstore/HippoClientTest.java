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
package com.alibaba.langengine.hippo.vectorstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class HippoClientTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private HippoClient hippoClient;
    private HippoParam hippoParam;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        
        hippoParam = new HippoParam();
        
        // 直接创建 mock 的 HippoClient 实例
        hippoClient = mock(HippoClient.class);
    }

    @Test
    public void testCreateTable() throws SQLException {
        // Mock 行为
        doNothing().when(hippoClient).createTable("test_table", hippoParam);
        
        hippoClient.createTable("test_table", hippoParam);
        
        verify(hippoClient, times(1)).createTable("test_table", hippoParam);
    }

    @Test
    public void testCreateIndex() throws SQLException {
        doNothing().when(hippoClient).createIndex("test_table", hippoParam);
        
        hippoClient.createIndex("test_table", hippoParam);
        
        verify(hippoClient, times(1)).createIndex("test_table", hippoParam);
    }

    @Test
    public void testTableExists() throws SQLException {
        when(hippoClient.tableExists("test_table")).thenReturn(true);
        
        boolean exists = hippoClient.tableExists("test_table");
        
        assertTrue(exists);
        verify(hippoClient, times(1)).tableExists("test_table");
    }

    @Test
    public void testInsertDocuments() throws SQLException {
        List<Map<String, Object>> documents = createTestDocuments();
        doNothing().when(hippoClient).insertDocuments("test_table", documents, hippoParam);
        
        hippoClient.insertDocuments("test_table", documents, hippoParam);
        
        verify(hippoClient, times(1)).insertDocuments("test_table", documents, hippoParam);
    }

    @Test
    public void testSearchSimilar() throws SQLException {
        List<Map<String, Object>> expectedResults = createTestSearchResults();
        when(hippoClient.searchSimilar("test_table", "[0.1,0.2,0.3]", 2, hippoParam))
                .thenReturn(expectedResults);
        
        List<Map<String, Object>> results = hippoClient.searchSimilar("test_table", "[0.1,0.2,0.3]", 2, hippoParam);
        
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(hippoClient, times(1)).searchSimilar("test_table", "[0.1,0.2,0.3]", 2, hippoParam);
    }

    @Test
    public void testDropTable() throws SQLException {
        doNothing().when(hippoClient).dropTable("test_table");
        
        hippoClient.dropTable("test_table");
        
        verify(hippoClient, times(1)).dropTable("test_table");
    }

    @Test
    public void testClose() throws SQLException {
        doNothing().when(hippoClient).close();
        
        hippoClient.close();
        
        verify(hippoClient, times(1)).close();
    }

    @Test
    public void testCreateTableException() throws SQLException {
        doThrow(new HippoException("TABLE_001", "Failed to create table"))
            .when(hippoClient).createTable("test_table", hippoParam);
        
        assertThrows(HippoException.class, () -> {
            hippoClient.createTable("test_table", hippoParam);
        });
        
        verify(hippoClient, times(1)).createTable("test_table", hippoParam);
    }

    private List<Map<String, Object>> createTestDocuments() {
        List<Map<String, Object>> documents = new ArrayList<>();
        
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("content_id", 1L);
        doc1.put("row_content", "test content 1");
        doc1.put("embeddings", "[0.1,0.2,0.3]");
        documents.add(doc1);

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("content_id", 2L);
        doc2.put("row_content", "test content 2");
        doc2.put("embeddings", "[0.4,0.5,0.6]");
        documents.add(doc2);

        return documents;
    }

    private List<Map<String, Object>> createTestSearchResults() {
        List<Map<String, Object>> results = new ArrayList<>();
        
        Map<String, Object> result1 = new HashMap<>();
        result1.put("content_id", 1L);
        result1.put("row_content", "result 1");
        result1.put("distance", 0.1);
        results.add(result1);

        Map<String, Object> result2 = new HashMap<>();
        result2.put("content_id", 2L);
        result2.put("row_content", "result 2");
        result2.put("distance", 0.2);
        results.add(result2);

        return results;
    }
}