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
package com.alibaba.langengine.neon.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.neon.exception.*;
import com.alibaba.langengine.neon.pool.NeonConnectionPool;
import com.pgvector.PGvector;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Data
public class NeonService {

    private String url;
    private String username;
    private String password;
    private String tableName;
    private NeonParam neonParam;
    private NeonConnectionPool connectionPool;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Performance metrics
    private final AtomicLong queryCount = new AtomicLong(0);
    private final AtomicLong insertCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public NeonService(String url, String username, String password, String tableName, NeonParam neonParam) {
        validateParameters(url, username, tableName);
        this.url = url;
        this.username = username;
        this.password = password;
        this.tableName = sanitizeTableName(tableName);
        this.neonParam = neonParam != null ? neonParam : new NeonParam();
    }
    
    private void validateParameters(String url, String username, String tableName) {
        if (StringUtils.isBlank(url)) {
            throw new NeonValidationException("Database URL cannot be null or empty");
        }
        if (StringUtils.isBlank(username)) {
            throw new NeonValidationException("Username cannot be null or empty");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new NeonValidationException("Table name cannot be null or empty");
        }
    }
    
    private String sanitizeTableName(String tableName) {
        // Prevent SQL injection in table name
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new NeonValidationException("Invalid table name. Only alphanumeric characters and underscores are allowed");
        }
        return tableName;
    }

    public void init() {
        lock.writeLock().lock();
        try {
            // Initialize connection pool
            if (connectionPool == null) {
                connectionPool = new NeonConnectionPool(url, username, password);
            }
            
            // Create pgvector extension if not exists
            try (Connection conn = connectionPool.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");
            }
            
            // Create table if not exists
            createTableIfNotExists();
            
            log.info("Successfully initialized Neon table: {}", tableName);
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            log.error("Neon Service init failed", e);
            throw new NeonConnectionException("Failed to initialize Neon service", e);
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error("Neon Service init failed", e);
            throw new NeonException("INIT_ERROR", "Failed to initialize Neon service", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String contentField = neonParam.getFieldNamePageContent();
        String idField = neonParam.getFieldNameUniqueId();
        String metaField = neonParam.getFieldMeta();
        int dimension = neonParam.getInitParam().getDimension();

        String createTableSQL = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id SERIAL PRIMARY KEY, " +
            "%s TEXT, " +
            "%s TEXT, " +
            "%s JSONB, " +
            "embedding vector(%d), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            tableName, idField, contentField, metaField, dimension
        );

        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            
            // Create vector index
            String indexName = tableName + "_embedding_idx";
            String indexType = neonParam.getInitParam().getIndexType();
            String createIndexSQL = String.format(
                "CREATE INDEX IF NOT EXISTS %s ON %s USING %s (embedding vector_cosine_ops)",
                indexName, tableName, indexType
            );
            stmt.execute(createIndexSQL);
            
            // Create unique index on content_id
            String uniqueIndexSQL = String.format(
                "CREATE UNIQUE INDEX IF NOT EXISTS %s_unique_id_idx ON %s (%s)",
                tableName, tableName, idField
            );
            stmt.execute(uniqueIndexSQL);
        }
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            addDocumentsBatch(documents);
            insertCount.addAndGet(documents.size());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void addDocumentsBatch(List<Document> documents) {
        String contentField = neonParam.getFieldNamePageContent();
        String idField = neonParam.getFieldNameUniqueId();
        String metaField = neonParam.getFieldMeta();

        String insertSQL = String.format(
            "INSERT INTO %s (%s, %s, %s, embedding) VALUES (?, ?, ?::jsonb, ?) " +
            "ON CONFLICT (%s) DO UPDATE SET " +
            "%s = EXCLUDED.%s, %s = EXCLUDED.%s, embedding = EXCLUDED.embedding, updated_at = CURRENT_TIMESTAMP",
            tableName, idField, contentField, metaField, idField,
            contentField, contentField, metaField, metaField
        );

        try (Connection conn = connectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                int batchSize = 100;
                int count = 0;
                
                for (Document doc : documents) {
                    if (doc.getEmbedding() == null || doc.getEmbedding().isEmpty()) {
                        log.warn("Skipping document {} without embedding", doc.getUniqueId());
                        continue;
                    }
                    
                    if (StringUtils.isBlank(doc.getUniqueId())) {
                        throw new NeonValidationException("Document unique ID cannot be null or empty");
                    }

                    // Convert embedding to float array
                    float[] embedding = new float[doc.getEmbedding().size()];
                    for (int i = 0; i < doc.getEmbedding().size(); i++) {
                        embedding[i] = doc.getEmbedding().get(i).floatValue();
                    }

                    pstmt.setString(1, doc.getUniqueId());
                    pstmt.setString(2, doc.getPageContent());
                    pstmt.setString(3, doc.getMetadata() != null ? 
                        com.alibaba.fastjson.JSON.toJSONString(doc.getMetadata()) : "{}");
                    pstmt.setObject(4, new PGvector(embedding));

                    pstmt.addBatch();
                    count++;
                    
                    // Execute batch every batchSize records
                    if (count % batchSize == 0) {
                        pstmt.executeBatch();
                        pstmt.clearBatch();
                    }
                }
                
                // Execute remaining batch
                if (count % batchSize != 0) {
                    pstmt.executeBatch();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                errorCount.incrementAndGet();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Failed to add documents to Neon", e);
            throw new NeonQueryException("Failed to add documents", e);
        }
    }

    public List<Document> similaritySearch(List<Float> query, int k, Double maxDistanceValue, Integer type) {
        if (query == null || query.isEmpty()) {
            throw new NeonValidationException("Query vector cannot be null or empty");
        }
        if (k <= 0) {
            throw new NeonValidationException("K must be positive");
        }
        
        lock.readLock().lock();
        try {
            queryCount.incrementAndGet();
            return similaritySearchInternal(query, k, maxDistanceValue, type);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private List<Document> similaritySearchInternal(List<Float> query, int k, Double maxDistanceValue, Integer type) {
        String contentField = neonParam.getFieldNamePageContent();
        String idField = neonParam.getFieldNameUniqueId();
        String metaField = neonParam.getFieldMeta();
        String distance = neonParam.getInitParam().getVectorDistance();

        // Convert query to float array
        float[] queryVector = new float[query.size()];
        for (int i = 0; i < query.size(); i++) {
            queryVector[i] = query.get(i);
        }

        String distanceFunc = getDistanceFunction(distance);
        String selectSQL = String.format(
            "SELECT %s, %s, %s, embedding %s ? AS distance " +
            "FROM %s " +
            "ORDER BY embedding %s ? " +
            "LIMIT ?",
            idField, contentField, metaField, distanceFunc,
            tableName, getDistanceOperator(distance)
        );

        List<Document> results = new ArrayList<>();
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setObject(1, new PGvector(queryVector));
            pstmt.setObject(2, new PGvector(queryVector));
            pstmt.setInt(3, k);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double dist = rs.getDouble("distance");
                    
                    // Apply distance filter if specified
                    if (maxDistanceValue != null && dist > maxDistanceValue) {
                        continue;
                    }

                    Document doc = new Document();
                    doc.setUniqueId(rs.getString(idField));
                    doc.setPageContent(rs.getString(contentField));
                    
                    String metaJson = rs.getString(metaField);
                    if (StringUtils.isNotBlank(metaJson)) {
                        Map<String, Object> metadata = com.alibaba.fastjson.JSON.parseObject(metaJson, Map.class);
                        doc.setMetadata(metadata);
                    }
                    
                    doc.setScore(dist);
                    results.add(doc);
                }
            }
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            log.error("Failed to search similar documents", e);
            throw new NeonQueryException("Failed to search documents", e);
        }

        return results;
    }

    private String getDistanceFunction(String distance) {
        switch (distance.toLowerCase()) {
            case "cosine":
                return "<->";
            case "l2":
                return "<->";
            case "inner_product":
                return "<#>";
            default:
                return "<->";
        }
    }

    private String getDistanceOperator(String distance) {
        switch (distance.toLowerCase()) {
            case "cosine":
                return "<->";
            case "l2":
                return "<->";
            case "inner_product":
                return "<#>";
            default:
                return "<->";
        }
    }

    public void deleteDocumentsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            String idField = neonParam.getFieldNameUniqueId();
            String deleteSQL = String.format(
                "DELETE FROM %s WHERE %s = ANY(?)",
                tableName, idField
            );
            
            try (Connection conn = connectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                Array array = conn.createArrayOf("TEXT", ids.toArray());
                pstmt.setArray(1, array);
                int deleted = pstmt.executeUpdate();
                log.info("Deleted {} documents", deleted);
            }
        } catch (SQLException e) {
            errorCount.incrementAndGet();
            log.error("Failed to delete documents", e);
            throw new NeonQueryException("Failed to delete documents", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public long getDocumentCount() {
        lock.readLock().lock();
        try {
            String countSQL = String.format("SELECT COUNT(*) FROM %s", tableName);
            try (Connection conn = connectionPool.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSQL)) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get document count", e);
        } finally {
            lock.readLock().unlock();
        }
        return 0;
    }
    
    public boolean healthCheck() {
        if (connectionPool == null || connectionPool.isClosed()) {
            return false;
        }
        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            log.error("Health check failed", e);
            return false;
        }
    }
    
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("queryCount", queryCount.get());
        metrics.put("insertCount", insertCount.get());
        metrics.put("errorCount", errorCount.get());
        if (connectionPool != null) {
            metrics.put("activeConnections", connectionPool.getActiveConnections());
            metrics.put("idleConnections", connectionPool.getIdleConnections());
            metrics.put("totalConnections", connectionPool.getTotalConnections());
        }
        return metrics;
    }

    public void dropTable() {
        lock.writeLock().lock();
        try (Connection conn = connectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
            log.info("Dropped Neon table: {}", tableName);
        } catch (SQLException e) {
            log.error("Failed to drop table", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
            log.info("Neon service closed");
        }
    }
}
