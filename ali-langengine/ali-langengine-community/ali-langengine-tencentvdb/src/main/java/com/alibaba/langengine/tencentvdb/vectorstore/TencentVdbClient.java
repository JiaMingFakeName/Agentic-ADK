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
package com.alibaba.langengine.tencentvdb.vectorstore;

import com.alibaba.fastjson.JSON;
import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.Collection;
import com.tencent.tcvectordb.model.Database;
import com.tencent.tcvectordb.model.Document;
import com.tencent.tcvectordb.model.DocField;
import com.tencent.tcvectordb.model.param.collection.CreateCollectionParam;
import com.tencent.tcvectordb.model.param.collection.FilterIndex;
import com.tencent.tcvectordb.model.param.collection.VectorIndex;
import com.tencent.tcvectordb.model.param.collection.FieldType;
import com.tencent.tcvectordb.model.param.collection.IndexType;
import com.tencent.tcvectordb.model.param.collection.MetricType;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.enums.ReadConsistencyEnum;
import com.tencent.tcvectordb.model.param.dml.SearchByVectorParam;
import com.tencent.tcvectordb.model.param.dml.InsertParam;
import com.tencent.tcvectordb.model.param.dml.DeleteParam;
import com.tencent.tcvectordb.model.param.entity.SearchRes;
import com.tencent.tcvectordb.model.param.entity.AffectRes;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class TencentVdbClient implements VectorDbClient {

    private final String serverUrl;
    private final String username;
    private final String password;
    private final String databaseName;
    private final TencentVdbParam param;
    private VectorDBClient client;
    private Database database;

    public TencentVdbClient(String serverUrl, String username, String password, String databaseName, TencentVdbParam param) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.param = param != null ? param : new TencentVdbParam();
        
        // 自动连接
        connect();
    }

    /**
     * 初始化连接
     */
    @Override
    public void connect() {
        try {
            log.info("Connecting to Tencent Cloud VectorDB at {}", serverUrl);
            
            ConnectParam connectParam = ConnectParam.newBuilder()
                .withUrl(serverUrl)
                .withUsername(username)
                .withKey(password)
                .build();
            
            client = new VectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY);
            database = client.database(databaseName);
            
            log.info("Successfully connected to Tencent Cloud VectorDB");
        } catch (Exception e) {
            throw new TencentVdbException("CONNECT_FAILED", "Failed to connect to Tencent Cloud VectorDB", e);
        }
    }

    /**
     * 确保客户端已连接
     */
    private void ensureConnected() {
        if (client == null) {
            throw new TencentVdbException("CLIENT_NOT_CONNECTED", 
                "VectorDB client is not connected. Please check connection parameters.");
        }
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
                log.info("Tencent Cloud VectorDB client closed");
            }
        } catch (Exception e) {
            log.warn("Error closing Tencent Cloud VectorDB client", e);
        }
    }

    /**
     * 创建集合
     */
    @Override
    public void createCollection(String collectionName, int dimension) {
        try {
            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withName(collectionName)
                .withShardNum(param.getInitParam().getShardNum())
                .withReplicaNum(param.getInitParam().getReplicaNum())
                .withDescription("Created by Ali-LangEngine TencentVdb module")
                .addField(new FilterIndex(param.getFieldNameUniqueId(), FieldType.String, IndexType.PRIMARY_KEY))
                .addField(new FilterIndex(param.getFieldNamePageContent(), FieldType.String, IndexType.FILTER))
                .addField(new FilterIndex(param.getFieldNameMetadata(), FieldType.String, IndexType.FILTER))
                .addField(new VectorIndex(param.getFieldNameEmbedding(), dimension, FieldType.Vector,
                    IndexType.valueOf(param.getInitParam().getIndexType()),
                    MetricType.valueOf(param.getInitParam().getMetricType())))
                .build();
            
            database.createCollection(createParam);
            log.info("Created collection: {}", collectionName);
        } catch (Exception e) {
            throw new TencentVdbException("CREATE_COLLECTION_FAILED", 
                "Failed to create collection: " + collectionName, e);
        }
    }

    /**
     * 检查集合是否存在
     */
    @Override
    public boolean hasCollection(String collectionName) {
        ensureConnected();
        try {
            Collection collection = database.describeCollection(collectionName);
            return collection != null;
        } catch (Exception e) {
            log.debug("Collection {} does not exist or error occurred: {}", collectionName, e.getMessage());
            return false;
        }
    }

    /**
     * 插入向量数据
     */
    @Override
    public void insert(String collectionName, List<Map<String, Object>> documents) {
        ensureConnected();
        try {
            List<Document> tcvdbDocuments = new ArrayList<>();
            for (Map<String, Object> docMap : documents) {
                Document.Builder docBuilder = Document.newBuilder()
                    .withId(docMap.get(param.getFieldNameUniqueId()).toString())
                    .withVector(convertToDoubleList((List<Float>) docMap.get(param.getFieldNameEmbedding())));
                
                // 添加字段数据
                Object pageContent = docMap.get(param.getFieldNamePageContent());
                if (pageContent != null) {
                    docBuilder.addDocField(new DocField(param.getFieldNamePageContent(), pageContent));
                }
                
                Object metadata = docMap.get(param.getFieldNameMetadata());
                if (metadata != null) {
                    docBuilder.addDocField(new DocField(param.getFieldNameMetadata(), metadata));
                }
                
                tcvdbDocuments.add(docBuilder.build());
            }
            
            InsertParam insertParam = InsertParam.newBuilder()
                .withDocuments(tcvdbDocuments)
                .build();
            
            client.upsert(databaseName, collectionName, insertParam);
            log.debug("Inserted {} documents into collection {}", documents.size(), collectionName);
        } catch (Exception e) {
            throw new TencentVdbException("INSERT_FAILED", 
                "Failed to insert documents into collection: " + collectionName, e);
        }
    }

    /**
     * 向量搜索
     */
    @Override
    public List<Map<String, Object>> search(String collectionName, List<Float> vector, int topK, Map<String, Object> searchParams) {
        ensureConnected();
        try {
            SearchByVectorParam.Builder searchBuilder = SearchByVectorParam.newBuilder()
                .withVectors(Arrays.asList(convertToDoubleList(vector)))
                .withRetrieveVector(false)
                .withLimit(topK);
            
            SearchByVectorParam searchParam = searchBuilder.build();
            List<List<Document>> searchResults = client.search(databaseName, collectionName, searchParam);
            
            List<Map<String, Object>> documents = new ArrayList<>();
            if (searchResults != null && !searchResults.isEmpty()) {
                for (List<Document> docList : searchResults) {
                    for (Document doc : docList) {
                        Map<String, Object> docMap = new HashMap<>();
                        docMap.put(param.getFieldNameUniqueId(), doc.getId());
                        docMap.put("score", doc.getScore());
                        
                        // 获取文档字段
                        if (doc.getDocFields() != null) {
                            for (DocField field : doc.getDocFields()) {
                                if (param.getFieldNamePageContent().equals(field.getName())) {
                                    docMap.put(param.getFieldNamePageContent(), field.getValue());
                                } else if (param.getFieldNameMetadata().equals(field.getName())) {
                                    docMap.put(param.getFieldNameMetadata(), field.getValue());
                                }
                            }
                        }
                        
                        documents.add(docMap);
                    }
                }
            }
            
            return documents;
        } catch (Exception e) {
            throw new TencentVdbException("SEARCH_FAILED", 
                "Failed to search in collection: " + collectionName, e);
        }
    }

    /**
     * 删除文档
     */
    @Override
    public void delete(String collectionName, List<String> documentIds) {
        ensureConnected();
        try {
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withDocumentIds(documentIds)
                    .build();
            AffectRes result = client.delete(databaseName, collectionName, deleteParam);
            log.debug("Deleted {} documents from collection {}, affected: {}", 
                documentIds.size(), collectionName, result.getAffectedCount());
        } catch (Exception e) {
            throw new TencentVdbException("DELETE_FAILED", 
                "Failed to delete documents from collection: " + collectionName, e);
        }
    }

    /**
     * 将Float列表转换为Double列表（腾讯云SDK要求Double）
     */
    private List<Double> convertToDoubleList(List<Float> floatList) {
        if (floatList == null || floatList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Double> result = new ArrayList<>(floatList.size());
        for (Float f : floatList) {
            result.add(f.doubleValue());
        }
        return result;
    }

}
