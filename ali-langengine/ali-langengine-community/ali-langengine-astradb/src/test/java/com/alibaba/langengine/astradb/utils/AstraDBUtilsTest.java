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
package com.alibaba.langengine.astradb.utils;

import com.alibaba.langengine.astradb.exception.AstraDBException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class AstraDBUtilsTest {

    @Test
    public void testValidateCollectionName_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateCollectionName("valid_collection"));
        assertDoesNotThrow(() -> AstraDBUtils.validateCollectionName("collection123"));
        assertDoesNotThrow(() -> AstraDBUtils.validateCollectionName("a"));
    }

    @Test
    public void testValidateCollectionName_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateCollectionName(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateCollectionName(""));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateCollectionName("123invalid"));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateCollectionName("invalid-name"));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateCollectionName("invalid.name"));
    }

    @Test
    public void testValidateKeyspaceName_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateKeyspaceName("valid_keyspace"));
        assertDoesNotThrow(() -> AstraDBUtils.validateKeyspaceName("keyspace123"));
    }

    @Test
    public void testValidateKeyspaceName_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateKeyspaceName(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateKeyspaceName(""));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateKeyspaceName("123invalid"));
    }

    @Test
    public void testValidateApiEndpoint_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateApiEndpoint("https://database-id.apps.astra.datastax.com"));
        assertDoesNotThrow(() -> AstraDBUtils.validateApiEndpoint("https://test-123.apps.astra.datastax.com"));
    }

    @Test
    public void testValidateApiEndpoint_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApiEndpoint(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApiEndpoint(""));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApiEndpoint("http://invalid.com"));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApiEndpoint("https://invalid.com"));
    }

    @Test
    public void testValidateApplicationToken_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateApplicationToken("valid_token_123"));
        assertDoesNotThrow(() -> AstraDBUtils.validateApplicationToken("AstraCS:token:value"));
    }

    @Test
    public void testValidateApplicationToken_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApplicationToken(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApplicationToken(""));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateApplicationToken("short"));
    }

    @Test
    public void testValidateVectorDimensions_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateVectorDimensions(1));
        assertDoesNotThrow(() -> AstraDBUtils.validateVectorDimensions(1536));
        assertDoesNotThrow(() -> AstraDBUtils.validateVectorDimensions(4096));
    }

    @Test
    public void testValidateVectorDimensions_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateVectorDimensions(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateVectorDimensions(0));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateVectorDimensions(-1));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateVectorDimensions(5000));
    }

    @Test
    public void testValidateEmbedding_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateEmbedding(Arrays.asList(0.1, 0.2, 0.3), 3));
        assertDoesNotThrow(() -> AstraDBUtils.validateEmbedding(Arrays.asList(1.0f, 2.0f), null));
    }

    @Test
    public void testValidateEmbedding_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateEmbedding(null, 3));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateEmbedding(Collections.emptyList(), 3));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateEmbedding(Arrays.asList(0.1, 0.2), 3));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateEmbedding(Arrays.asList(0.1, null, 0.3), 3));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateEmbedding(Arrays.asList(Double.NaN, 0.2, 0.3), 3));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateEmbedding(Arrays.asList(Double.POSITIVE_INFINITY, 0.2, 0.3), 3));
    }

    @Test
    public void testValidateDocumentId_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateDocumentId("valid_id"));
        assertDoesNotThrow(() -> AstraDBUtils.validateDocumentId("123"));
    }

    @Test
    public void testValidateDocumentId_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateDocumentId(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateDocumentId(""));
        
        String longId = "a".repeat(1001);
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateDocumentId(longId));
    }

    @Test
    public void testValidateBatchSize_Valid() {
        assertDoesNotThrow(() -> AstraDBUtils.validateBatchSize(1));
        assertDoesNotThrow(() -> AstraDBUtils.validateBatchSize(20));
    }

    @Test
    public void testValidateBatchSize_Invalid() {
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateBatchSize(null));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateBatchSize(0));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateBatchSize(-1));
        assertThrows(AstraDBException.class, () -> AstraDBUtils.validateBatchSize(100));
    }

    @Test
    public void testSanitizeCollectionName() {
        assertEquals("valid_collection", AstraDBUtils.sanitizeCollectionName("valid_collection"));
        assertEquals("collection_123invalid", AstraDBUtils.sanitizeCollectionName("123invalid"));
        assertEquals("invalid_name", AstraDBUtils.sanitizeCollectionName("invalid-name"));
        assertEquals("invalid_name", AstraDBUtils.sanitizeCollectionName("invalid.name"));
        assertEquals(Constants.DEFAULT_COLLECTION_NAME, AstraDBUtils.sanitizeCollectionName(null));
        assertEquals(Constants.DEFAULT_COLLECTION_NAME, AstraDBUtils.sanitizeCollectionName(""));
        
        String longName = "a".repeat(60);
        String sanitized = AstraDBUtils.sanitizeCollectionName(longName);
        assertTrue(sanitized.length() <= 48);
    }

    @Test
    public void testExtractDatabaseId() {
        assertEquals("database-id", AstraDBUtils.extractDatabaseId("https://database-id.apps.astra.datastax.com"));
        assertEquals("test-123", AstraDBUtils.extractDatabaseId("https://test-123.apps.astra.datastax.com"));
        assertNull(AstraDBUtils.extractDatabaseId(null));
        assertNull(AstraDBUtils.extractDatabaseId(""));
        assertNull(AstraDBUtils.extractDatabaseId("invalid-url"));
    }

    @Test
    public void testIsSupportedSimilarityFunction() {
        assertTrue(AstraDBUtils.isSupportedSimilarityFunction(Constants.SIMILARITY_FUNCTION_COSINE));
        assertTrue(AstraDBUtils.isSupportedSimilarityFunction(Constants.SIMILARITY_FUNCTION_DOT_PRODUCT));
        assertTrue(AstraDBUtils.isSupportedSimilarityFunction(Constants.SIMILARITY_FUNCTION_EUCLIDEAN));
        
        assertFalse(AstraDBUtils.isSupportedSimilarityFunction(null));
        assertFalse(AstraDBUtils.isSupportedSimilarityFunction(""));
        assertFalse(AstraDBUtils.isSupportedSimilarityFunction("unsupported"));
    }
}