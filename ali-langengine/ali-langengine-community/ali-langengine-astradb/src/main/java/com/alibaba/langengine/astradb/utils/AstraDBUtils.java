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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;


public final class AstraDBUtils {

    private static final Pattern COLLECTION_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,47}$");
    private static final Pattern KEYSPACE_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{0,47}$");
    private static final Pattern ENDPOINT_PATTERN = Pattern.compile("^https://[a-zA-Z0-9-]+\\.apps\\.astra\\.datastax\\.com$");

    private AstraDBUtils() {
        // Utility class
    }

    /**
     * Validates collection name according to AstraDB naming rules
     *
     * @param collectionName the collection name to validate
     * @throws AstraDBException if the collection name is invalid
     */
    public static void validateCollectionName(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            throw AstraDBException.validationError("Collection name cannot be null or empty");
        }
        
        if (!COLLECTION_NAME_PATTERN.matcher(collectionName).matches()) {
            throw AstraDBException.validationError(
                "Collection name must start with a letter and contain only letters, numbers, and underscores (max 48 characters): " + collectionName
            );
        }
    }

    /**
     * Validates keyspace name according to AstraDB naming rules
     *
     * @param keyspaceName the keyspace name to validate
     * @throws AstraDBException if the keyspace name is invalid
     */
    public static void validateKeyspaceName(String keyspaceName) {
        if (StringUtils.isBlank(keyspaceName)) {
            throw AstraDBException.validationError("Keyspace name cannot be null or empty");
        }
        
        if (!KEYSPACE_NAME_PATTERN.matcher(keyspaceName).matches()) {
            throw AstraDBException.validationError(
                "Keyspace name must start with a letter and contain only letters, numbers, and underscores (max 48 characters): " + keyspaceName
            );
        }
    }

    /**
     * Validates API endpoint format
     *
     * @param apiEndpoint the API endpoint to validate
     * @throws AstraDBException if the API endpoint is invalid
     */
    public static void validateApiEndpoint(String apiEndpoint) {
        if (StringUtils.isBlank(apiEndpoint)) {
            throw AstraDBException.validationError("API endpoint cannot be null or empty");
        }
        
        if (!ENDPOINT_PATTERN.matcher(apiEndpoint).matches()) {
            throw AstraDBException.validationError(
                "API endpoint must be a valid AstraDB endpoint (https://<database-id>.apps.astra.datastax.com): " + apiEndpoint
            );
        }
    }

    /**
     * Validates application token format
     *
     * @param applicationToken the application token to validate
     * @throws AstraDBException if the application token is invalid
     */
    public static void validateApplicationToken(String applicationToken) {
        if (StringUtils.isBlank(applicationToken)) {
            throw AstraDBException.validationError("Application token cannot be null or empty");
        }
        
        if (applicationToken.length() < 10) {
            throw AstraDBException.validationError("Application token appears to be too short");
        }
    }

    /**
     * Validates vector dimensions
     *
     * @param dimensions the vector dimensions to validate
     * @throws AstraDBException if the dimensions are invalid
     */
    public static void validateVectorDimensions(Integer dimensions) {
        if (dimensions == null || dimensions <= 0) {
            throw AstraDBException.validationError("Vector dimensions must be a positive integer");
        }
        
        if (dimensions > 4096) {
            throw AstraDBException.validationError("Vector dimensions cannot exceed 4096");
        }
    }

    /**
     * Validates embedding vector
     *
     * @param embedding the embedding vector to validate
     * @param expectedDimensions the expected number of dimensions
     * @throws AstraDBException if the embedding is invalid
     */
    public static void validateEmbedding(List<? extends Number> embedding, Integer expectedDimensions) {
        if (embedding == null || embedding.isEmpty()) {
            throw AstraDBException.validationError("Embedding vector cannot be null or empty");
        }
        
        if (expectedDimensions != null && embedding.size() != expectedDimensions) {
            throw AstraDBException.validationError(
                String.format("Embedding vector size (%d) does not match expected dimensions (%d)", 
                    embedding.size(), expectedDimensions)
            );
        }
        
        // Check for null or invalid values
        for (int i = 0; i < embedding.size(); i++) {
            Number value = embedding.get(i);
            if (value == null) {
                throw AstraDBException.validationError("Embedding vector contains null value at index " + i);
            }
            
            double doubleValue = value.doubleValue();
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                throw AstraDBException.validationError("Embedding vector contains invalid value at index " + i + ": " + doubleValue);
            }
        }
    }

    /**
     * Validates document ID
     *
     * @param documentId the document ID to validate
     * @throws AstraDBException if the document ID is invalid
     */
    public static void validateDocumentId(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            throw AstraDBException.validationError("Document ID cannot be null or empty");
        }
        
        if (documentId.length() > 1000) {
            throw AstraDBException.validationError("Document ID cannot exceed 1000 characters");
        }
    }

    /**
     * Validates batch size for operations
     *
     * @param batchSize the batch size to validate
     * @throws AstraDBException if the batch size is invalid
     */
    public static void validateBatchSize(Integer batchSize) {
        if (batchSize == null || batchSize <= 0) {
            throw AstraDBException.validationError("Batch size must be a positive integer");
        }
        
        if (batchSize > Constants.DEFAULT_MAX_BATCH_SIZE) {
            throw AstraDBException.validationError(
                String.format("Batch size (%d) cannot exceed maximum allowed (%d)", 
                    batchSize, Constants.DEFAULT_MAX_BATCH_SIZE)
            );
        }
    }

    /**
     * Sanitizes collection name by replacing invalid characters
     *
     * @param collectionName the collection name to sanitize
     * @return sanitized collection name
     */
    public static String sanitizeCollectionName(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            return Constants.DEFAULT_COLLECTION_NAME;
        }
        
        // Replace invalid characters with underscores
        String sanitized = collectionName.replaceAll("[^a-zA-Z0-9_]", "_");
        
        // Ensure it starts with a letter
        if (!Character.isLetter(sanitized.charAt(0))) {
            sanitized = "collection_" + sanitized;
        }
        
        // Truncate if too long
        if (sanitized.length() > 48) {
            sanitized = sanitized.substring(0, 48);
        }
        
        return sanitized;
    }

    /**
     * Extracts database ID from API endpoint
     *
     * @param apiEndpoint the API endpoint
     * @return database ID or null if cannot be extracted
     */
    public static String extractDatabaseId(String apiEndpoint) {
        if (StringUtils.isBlank(apiEndpoint)) {
            return null;
        }
        
        try {
            // Extract database ID from https://<database-id>.apps.astra.datastax.com
            if (apiEndpoint.startsWith("https://") && apiEndpoint.contains(".apps.astra.datastax.com")) {
                String[] parts = apiEndpoint.replace("https://", "").split("\\.");
                if (parts.length > 0) {
                    return parts[0];
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        
        return null;
    }

    /**
     * Checks if the similarity function is supported
     *
     * @param similarityFunction the similarity function to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedSimilarityFunction(String similarityFunction) {
        if (StringUtils.isBlank(similarityFunction)) {
            return false;
        }
        
        return Constants.SIMILARITY_FUNCTION_COSINE.equals(similarityFunction) ||
               Constants.SIMILARITY_FUNCTION_DOT_PRODUCT.equals(similarityFunction) ||
               Constants.SIMILARITY_FUNCTION_EUCLIDEAN.equals(similarityFunction);
    }
}