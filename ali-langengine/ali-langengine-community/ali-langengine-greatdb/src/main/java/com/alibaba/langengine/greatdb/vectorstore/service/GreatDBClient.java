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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
public class GreatDBClient {

    private final DataSource dataSource;
    private final GreatDBParam param;

    public GreatDBClient(GreatDBParam param) {
        this.param = param;
        this.dataSource = createDataSource(param);
        initializeTable();
    }

    private DataSource createDataSource(GreatDBParam param) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(param.getUrl());
        config.setUsername(param.getUsername());
        config.setPassword(param.getPassword());
        config.setMaximumPoolSize(param.getPoolSize());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // Enhanced connection pool configuration
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTestQuery("SELECT 1");
        config.setLeakDetectionThreshold(60000); // 1 minute
        
        return new HikariDataSource(config);
    }

    private void initializeTable() {
        // Validate collection name to prevent SQL injection
        String collectionName = validateCollectionName(param.getCollectionName());
        
        String createTableSql = String.format(
            "CREATE TABLE IF NOT EXISTS `%s` (" +
            "id VARCHAR(255) PRIMARY KEY, " +
            "content TEXT NOT NULL, " +
            "embedding JSON NOT NULL, " +
            "metadata JSON, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")", collectionName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTableSql)) {
            stmt.executeUpdate();
            log.info("Table {} initialized successfully", param.getCollectionName());
        } catch (SQLException e) {
            throw new GreatDBException("Failed to initialize table", e);
        }
    }

    public void addDocument(String id, String content, List<Double> embedding, Map<String, Object> metadata) {
        String collectionName = validateCollectionName(param.getCollectionName());
        String sql = String.format(
            "INSERT INTO `%s` (id, content, embedding, metadata) VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE content = VALUES(content), embedding = VALUES(embedding), metadata = VALUES(metadata)",
            collectionName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, content);
            stmt.setString(3, convertEmbeddingToJson(embedding));
            stmt.setString(4, convertMetadataToJson(metadata));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GreatDBException("Failed to add document", e);
        }
    }

    public List<VectorSearchResult> similaritySearch(List<Double> queryEmbedding, int limit) {
        String collectionName = validateCollectionName(param.getCollectionName());
        String sql = String.format(
            "SELECT id, content, embedding, metadata, " +
            "JSON_DISTANCE(embedding, ?) as distance " +
            "FROM `%s` ORDER BY distance LIMIT ?",
            collectionName);

        List<VectorSearchResult> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, convertEmbeddingToJson(queryEmbedding));
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    VectorSearchResult result = VectorSearchResult.builder()
                        .id(rs.getString("id"))
                        .content(rs.getString("content"))
                        .distance(rs.getDouble("distance"))
                        .metadata(parseMetadataFromJson(rs.getString("metadata")))
                        .build();
                    results.add(result);
                }
            }
        } catch (SQLException e) {
            throw new GreatDBException("Failed to perform similarity search", e);
        }
        return results;
    }

    public void deleteDocument(String id) {
        String collectionName = validateCollectionName(param.getCollectionName());
        String sql = String.format("DELETE FROM `%s` WHERE id = ?", collectionName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GreatDBException("Failed to delete document", e);
        }
    }

    public void close() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    private String convertEmbeddingToJson(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJsonString(entry.getKey()))
              .append("\":\"").append(escapeJsonString(String.valueOf(entry.getValue()))).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, Object> parseMetadataFromJson(String json) {
        Map<String, Object> metadata = new java.util.HashMap<>();
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return metadata;
        }
        // Simple JSON parsing - in production, use a proper JSON library like Jackson
        try {
            // Remove braces and split by comma
            String content = json.trim().substring(1, json.length() - 1);
            if (!content.isEmpty()) {
                String[] pairs = content.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        metadata.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse metadata JSON: {}", json, e);
        }
        return metadata;
    }

    /**
     * Validate collection name to prevent SQL injection
     */
    private String validateCollectionName(String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new GreatDBException("Collection name cannot be null or empty");
        }
        
        // Only allow alphanumeric characters, underscores, and hyphens
        if (!collectionName.matches("^[a-zA-Z0-9_-]+$")) {
            throw new GreatDBException("Invalid collection name. Only alphanumeric characters, underscores, and hyphens are allowed");
        }
        
        // Limit length to prevent issues
        if (collectionName.length() > 64) {
            throw new GreatDBException("Collection name too long. Maximum 64 characters allowed");
        }
        
        return collectionName;
    }

    /**
     * Escape JSON string values to prevent injection
     */
    private String escapeJsonString(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}