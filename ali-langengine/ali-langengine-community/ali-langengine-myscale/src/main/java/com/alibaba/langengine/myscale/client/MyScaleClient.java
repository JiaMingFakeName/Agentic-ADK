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
package com.alibaba.langengine.myscale.client;

import com.alibaba.langengine.myscale.exception.MyScaleException;
import com.alibaba.langengine.myscale.model.MyScaleParam;
import com.alibaba.langengine.myscale.model.MyScaleQueryRequest;
import com.alibaba.langengine.myscale.model.MyScaleQueryResponse;
import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class MyScaleClient {

    private final MyScaleParam param;
    private final ClickHouseDataSource dataSource;

    public MyScaleClient(MyScaleParam param) {
        this.param = param;
        this.dataSource = createDataSource(param);

        if (param.getAutoCreateTable()) {
            createTableIfNotExists();
        }
    }

    private ClickHouseDataSource createDataSource(MyScaleParam param) {
        try {
            Properties props = new Properties();
            props.setProperty("user", param.getUsername());
            props.setProperty("password", param.getPassword());
            props.setProperty("database", param.getDatabase());
            props.setProperty("socket_timeout", String.valueOf(param.getReadTimeout()));
            props.setProperty("connect_timeout", String.valueOf(param.getConnectionTimeout()));

            return new ClickHouseDataSource(param.getServerUrl(), props);
        } catch (Exception e) {
            throw new MyScaleException("Failed to create MyScale data source", e);
        }
    }

    public void createTableIfNotExists() {
        String createTableSQL = String.format(
            "CREATE TABLE IF NOT EXISTS %s.%s (" +
            "  id String," +
            "  content String," +
            "  vector Array(Float32)," +
            "  metadata String," +
            "  CONSTRAINT check_non_empty_vector CHECK length(vector) = %d," +
            "  VECTOR INDEX vector_idx vector TYPE MSTG('metric_type=%s')" +
            ") ENGINE = MergeTree() ORDER BY id",
            param.getDatabase(), param.getTableName(),
            param.getVectorDimension(), param.getDistanceType()
        );

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            log.info("Table {} created or already exists", param.getTableName());

        } catch (SQLException e) {
            throw new MyScaleException("Failed to create table", e);
        }
    }

    public void insertDocuments(List<DocumentInsert> documents) {
        if (documents.isEmpty()) {
            return;
        }

        String insertSQL = String.format(
            "INSERT INTO %s.%s (id, content, vector, metadata) VALUES (?, ?, ?, ?)",
            param.getDatabase(), param.getTableName()
        );

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            int count = 0;
            for (DocumentInsert doc : documents) {
                pstmt.setString(1, doc.getId());
                pstmt.setString(2, doc.getContent());
                pstmt.setArray(3, conn.createArrayOf("Float32", doc.getVector().toArray()));
                pstmt.setString(4, doc.getMetadataJson());
                pstmt.addBatch();

                count++;
                if (count % param.getBatchSize() == 0) {
                    pstmt.executeBatch();
                    log.debug("Inserted {} documents", count);
                }
            }

            if (count % param.getBatchSize() != 0) {
                pstmt.executeBatch();
            }

            log.info("Successfully inserted {} documents", documents.size());

        } catch (SQLException e) {
            throw new MyScaleException("Failed to insert documents", e);
        }
    }

    public MyScaleQueryResponse search(MyScaleQueryRequest request) {
        String searchSQL = buildSearchSQL(request);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            setSearchParameters(pstmt, request);

            try (ResultSet rs = pstmt.executeQuery()) {
                return parseSearchResults(rs);
            }

        } catch (SQLException e) {
            throw new MyScaleException("Failed to execute search query", e);
        }
    }

    private String buildSearchSQL(MyScaleQueryRequest request) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, content, vector, metadata, ");

        switch (param.getDistanceType().toLowerCase()) {
            case "cosine":
                sql.append("cosineDistance(vector, ?) as distance ");
                break;
            case "l2":
                sql.append("L2Distance(vector, ?) as distance ");
                break;
            case "innerproduct":
                sql.append("-innerProduct(vector, ?) as distance ");
                break;
            default:
                sql.append("cosineDistance(vector, ?) as distance ");
        }

        sql.append(String.format("FROM %s.%s ", param.getDatabase(), param.getTableName()));

        if (StringUtils.isNotBlank(request.getWhereClause())) {
            sql.append("WHERE ").append(request.getWhereClause()).append(" ");
        }

        if (request.getMaxDistance() != null) {
            sql.append(sql.toString().contains("WHERE") ? "AND " : "WHERE ");
            sql.append("distance <= ? ");
        }

        sql.append("ORDER BY distance ASC LIMIT ?");

        return sql.toString();
    }

    private void setSearchParameters(PreparedStatement pstmt, MyScaleQueryRequest request) throws SQLException {
        int paramIndex = 1;

        Array vectorArray = pstmt.getConnection().createArrayOf("Float32",
            request.getQueryVector().toArray(new Double[0]));
        pstmt.setArray(paramIndex++, vectorArray);

        if (request.getMaxDistance() != null) {
            pstmt.setDouble(paramIndex++, request.getMaxDistance());
        }

        pstmt.setInt(paramIndex, request.getLimit());
    }

    private MyScaleQueryResponse parseSearchResults(ResultSet rs) throws SQLException {
        MyScaleQueryResponse response = new MyScaleQueryResponse();
        List<MyScaleQueryResponse.QueryResult> results = new ArrayList<>();

        while (rs.next()) {
            MyScaleQueryResponse.QueryResult result = new MyScaleQueryResponse.QueryResult();
            result.setId(rs.getString("id"));
            result.setContent(rs.getString("content"));
            result.setDistance(rs.getDouble("distance"));

            Array vectorArray = rs.getArray("vector");
            if (vectorArray != null) {
                Float[] vectorData = (Float[]) vectorArray.getArray();
                result.setVector(Arrays.stream(vectorData)
                    .map(Float::doubleValue)
                    .collect(Collectors.toList()));
            }

            String metadataJson = rs.getString("metadata");
            if (StringUtils.isNotBlank(metadataJson)) {
                // Parse JSON metadata if needed
                result.setMetadata(new HashMap<>());
            }

            results.add(result);
        }

        response.setResults(results);
        response.setTotal(results.size());
        return response;
    }

    public void deleteById(String id) {
        String deleteSQL = String.format("DELETE FROM %s.%s WHERE id = ?",
            param.getDatabase(), param.getTableName());

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setString(1, id);
            int affected = pstmt.executeUpdate();
            log.info("Deleted {} documents with id: {}", affected, id);

        } catch (SQLException e) {
            throw new MyScaleException("Failed to delete document", e);
        }
    }

    public void close() {
        // ClickHouseDataSource doesn't implement Closeable
        // Connection pooling is managed by the driver itself
        log.info("MyScale client resources cleaned up");
    }

    public static class DocumentInsert {
        private String id;
        private String content;
        private List<Double> vector;
        private String metadataJson;

        public DocumentInsert(String id, String content, List<Double> vector, String metadataJson) {
            this.id = id;
            this.content = content;
            this.vector = vector;
            this.metadataJson = metadataJson;
        }

        public String getId() { return id; }
        public String getContent() { return content; }
        public List<Double> getVector() { return vector; }
        public String getMetadataJson() { return metadataJson; }
    }
}