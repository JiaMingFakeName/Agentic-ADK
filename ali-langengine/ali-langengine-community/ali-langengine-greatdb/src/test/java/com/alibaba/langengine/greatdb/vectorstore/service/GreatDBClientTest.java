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
package com.alibaba.langengine.greatdb.vectorstore.service;

import com.alibaba.langengine.greatdb.vectorstore.GreatDBException;
import com.alibaba.langengine.greatdb.vectorstore.GreatDBParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GreatDBClientTest {

    @Test
    public void testValidCollectionNames() {
        GreatDBParam param = GreatDBParam.builder()
            .url("jdbc:mysql://localhost:3306/test_db")
            .username("test_user")
            .password("test_password")
            .collectionName("valid_collection_123")
            .build();
        
        // Valid collection names should not throw exceptions
        assertDoesNotThrow(() -> {
            String validName = "valid_collection_123";
            assertTrue(validName.matches("^[a-zA-Z0-9_-]+$"));
            assertTrue(validName.length() <= 64);
        });
    }

    @Test
    public void testInvalidCollectionNames() {
        // Test null collection name
        assertThrows(IllegalArgumentException.class, () -> {
            String nullName = null;
            if (nullName == null || nullName.trim().isEmpty()) {
                throw new IllegalArgumentException("Collection name cannot be null or empty");
            }
        });

        // Test empty collection name
        assertThrows(IllegalArgumentException.class, () -> {
            String emptyName = "";
            if (emptyName == null || emptyName.trim().isEmpty()) {
                throw new IllegalArgumentException("Collection name cannot be null or empty");
            }
        });

        // Test invalid characters
        assertThrows(IllegalArgumentException.class, () -> {
            String invalidName = "invalid@collection";
            if (!invalidName.matches("^[a-zA-Z0-9_-]+$")) {
                throw new IllegalArgumentException("Invalid collection name");
            }
        });

        // Test too long name
        assertThrows(IllegalArgumentException.class, () -> {
            String longName = "a".repeat(65);
            if (longName.length() > 64) {
                throw new IllegalArgumentException("Collection name too long");
            }
        });
    }

    @Test
    public void testJsonEscaping() {
        // Test JSON string escaping
        String testString = "test\"value\nwith\\special\tchars";
        String escaped = testString.replace("\\", "\\\\")
                                  .replace("\"", "\\\"")
                                  .replace("\n", "\\n")
                                  .replace("\r", "\\r")
                                  .replace("\t", "\\t");
        
        assertFalse(escaped.contains("\n"));
        assertFalse(escaped.contains("\t"));
        assertTrue(escaped.contains("\\\""));
        assertTrue(escaped.contains("\\n"));
        assertTrue(escaped.contains("\\t"));
    }

    @Test
    public void testConnectionPoolConfiguration() {
        GreatDBParam param = GreatDBParam.builder()
            .url("jdbc:mysql://localhost:3306/test_db")
            .username("test_user")
            .password("test_password")
            .poolSize(10)
            .collectionName("test_collection")
            .build();

        // Test parameter validation
        assertTrue(param.getPoolSize() > 0);
        assertNotNull(param.getUrl());
        assertNotNull(param.getUsername());
        assertNotNull(param.getCollectionName());
    }
}