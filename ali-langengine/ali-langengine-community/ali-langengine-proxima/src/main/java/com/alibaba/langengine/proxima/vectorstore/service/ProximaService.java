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
package com.alibaba.langengine.proxima.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.proxima.vectorstore.ProximaException;
import com.alibaba.langengine.proxima.vectorstore.ProximaParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
public class ProximaService {

    private final ProximaClient client;
    private final ProximaParam param;

    public ProximaService(ProximaClient client, ProximaParam param) {
        this.client = client;
        this.param = param;
    }

    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            ProximaInsertRequest request = new ProximaInsertRequest();
            request.setCollectionName(param.getCollectionName());
            
            List<ProximaInsertRequest.ProximaDocument> proximaDocuments = new ArrayList<>();
            
            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getPageContent())) {
                    continue;
                }
                
                ProximaInsertRequest.ProximaDocument proximaDoc = new ProximaInsertRequest.ProximaDocument();
                proximaDoc.setId(StringUtils.isEmpty(document.getUniqueId()) ? 
                        UUID.randomUUID().toString() : document.getUniqueId());
                proximaDoc.setContent(document.getPageContent());
                proximaDoc.setMetadata(document.getMetadata());
                
                if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
                    List<Float> vector = document.getEmbedding().stream()
                            .map(Double::floatValue)
                            .collect(Collectors.toList());
                    proximaDoc.setVector(vector);
                }
                
                proximaDocuments.add(proximaDoc);
            }
            
            request.setDocuments(proximaDocuments);
            client.insertDocuments(request);
            
        } catch (Exception e) {
            log.error("Failed to add documents to Proxima", e);
            throw new ProximaException("PROXIMA_ADD_ERROR", "Failed to add documents", e);
        }
    }

    public List<Document> queryDocuments(List<Float> queryVector, int topK) {
        // 输入验证
        if (queryVector == null || queryVector.isEmpty()) {
            throw new ProximaException("PROXIMA_QUERY_ERROR", "Query vector cannot be null or empty");
        }
        if (topK <= 0) {
            throw new ProximaException("PROXIMA_QUERY_ERROR", "TopK must be positive");
        }
        
        try {
            ProximaQueryRequest request = new ProximaQueryRequest();
            request.setCollectionName(param.getCollectionName());
            request.setQueryVector(queryVector);
            request.setTopK(topK);
            
            ProximaQueryResponse response = client.queryDocuments(request);
            
            List<Document> documents = new ArrayList<>();
            if (response.getResults() != null) {
                for (ProximaQueryResponse.ProximaResult result : response.getResults()) {
                    Document document = new Document();
                    document.setUniqueId(result.getId());
                    document.setPageContent(result.getContent());
                    document.setScore(result.getScore().doubleValue());
                    // 安全的类型转换
                    if (result.getMetadata() instanceof Map) {
                        document.setMetadata((Map<String, Object>) result.getMetadata());
                    } else {
                        document.setMetadata(new HashMap<>());
                    }
                    documents.add(document);
                }
            }
            
            return documents;
            
        } catch (Exception e) {
            log.error("Failed to query documents from Proxima", e);
            throw new ProximaException("PROXIMA_QUERY_ERROR", "Failed to query documents", e);
        }
    }
}