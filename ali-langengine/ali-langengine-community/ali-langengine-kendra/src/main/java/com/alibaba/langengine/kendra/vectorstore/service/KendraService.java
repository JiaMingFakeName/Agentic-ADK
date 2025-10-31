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
package com.alibaba.langengine.kendra.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.kendra.vectorstore.KendraQueryException;
import com.alibaba.langengine.kendra.vectorstore.client.KendraClient;
import com.alibaba.langengine.kendra.vectorstore.model.KendraDocument;
import com.alibaba.langengine.kendra.vectorstore.model.KendraQueryRequest;
import com.alibaba.langengine.kendra.vectorstore.model.KendraQueryResponse;
import com.alibaba.langengine.kendra.vectorstore.model.KendraResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.kendra.model.BatchDeleteDocumentRequest;
import software.amazon.awssdk.services.kendra.model.BatchPutDocumentRequest;
import software.amazon.awssdk.services.kendra.model.ContentType;
import software.amazon.awssdk.services.kendra.model.DocumentAttribute;
import software.amazon.awssdk.services.kendra.model.DocumentAttributeValue;
import software.amazon.awssdk.services.kendra.model.QueryRequest;
import software.amazon.awssdk.services.kendra.model.QueryResponse;
import software.amazon.awssdk.services.kendra.model.QueryResultItem;
import software.amazon.awssdk.services.kendra.model.QueryResultType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class KendraService {

    private final KendraClient client;

    public KendraService(KendraClient client) {
        this.client = client;
    }

    /**
     * Add documents to Kendra index
     */
    public void addDocuments(List<KendraDocument> documents, String indexId) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            List<software.amazon.awssdk.services.kendra.model.Document> kendraDocuments = documents.stream()
                .map(this::convertToKendraDocument)
                .collect(Collectors.toList());

            BatchPutDocumentRequest request = BatchPutDocumentRequest.builder()
                .indexId(indexId)
                .documents(kendraDocuments)
                .build();

            client.getKendraClient().batchPutDocument(request);

            log.info("Added {} documents to Kendra index: {}", documents.size(), indexId);

        } catch (Exception e) {
            log.error("Failed to add documents to Kendra", e);
            throw new KendraQueryException("Failed to add documents: " + e.getMessage(), e);
        }
    }

    /**
     * Delete documents from Kendra index
     */
    public void deleteDocuments(List<String> documentIds, String indexId) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }

        try {
            BatchDeleteDocumentRequest request = BatchDeleteDocumentRequest.builder()
                .indexId(indexId)
                .documentIdList(documentIds)
                .build();

            client.getKendraClient().batchDeleteDocument(request);

            log.info("Deleted {} documents from Kendra index: {}", documentIds.size(), indexId);

        } catch (Exception e) {
            log.error("Failed to delete documents from Kendra", e);
            throw new KendraQueryException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * Query Kendra index
     */
    public KendraQueryResponse query(KendraQueryRequest queryRequest) {
        try {
            QueryRequest.Builder requestBuilder = QueryRequest.builder()
                .indexId(queryRequest.getIndexId())
                .queryText(queryRequest.getQueryText())
                .pageSize(queryRequest.getPageSize())
                .pageNumber(queryRequest.getPageNumber());

            if (queryRequest.getQueryResultTypes() != null && queryRequest.getQueryResultTypes().length > 0) {
                // Kendra only supports one result type at a time, use the first one
                QueryResultType resultType = QueryResultType.fromValue(queryRequest.getQueryResultTypes()[0]);
                requestBuilder.queryResultTypeFilter(resultType);
            }

            if (queryRequest.getRequestedDocumentAttributes() != null) {
                requestBuilder.requestedDocumentAttributes(Arrays.asList(queryRequest.getRequestedDocumentAttributes()));
            }

            QueryResponse response = client.getKendraClient().query(requestBuilder.build());

            return convertToKendraQueryResponse(response, queryRequest.getQueryText());

        } catch (Exception e) {
            log.error("Failed to query Kendra index", e);
            throw new KendraQueryException("Failed to query Kendra: " + e.getMessage(), e);
        }
    }

    /**
     * Convert Document to KendraDocument
     */
    public KendraDocument convertFromDocument(Document document) {
        KendraDocument kendraDoc = new KendraDocument();
        kendraDoc.setId(document.getUniqueId());
        kendraDoc.setContent(document.getPageContent());

        if (document.getMetadata() != null) {
            kendraDoc.setAttributes(document.getMetadata());

            String title = (String) document.getMetadata().get("title");
            if (StringUtils.isNotBlank(title)) {
                kendraDoc.setTitle(title);
            }

            String sourceUri = (String) document.getMetadata().get("source");
            if (StringUtils.isNotBlank(sourceUri)) {
                kendraDoc.setSourceUri(sourceUri);
            }

            String category = (String) document.getMetadata().get("category");
            if (StringUtils.isNotBlank(category)) {
                kendraDoc.setCategory(category);
            }
        }

        kendraDoc.setCreatedAt(System.currentTimeMillis());
        kendraDoc.setUpdatedAt(System.currentTimeMillis());

        return kendraDoc;
    }

    /**
     * Convert KendraResult to Document
     */
    public Document convertToDocument(KendraResult result) {
        Document document = new Document();
        document.setUniqueId(result.getId());
        document.setPageContent(result.getContent());
        document.setScore(result.getScore());

        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isNotBlank(result.getTitle())) {
            metadata.put("title", result.getTitle());
        }
        if (StringUtils.isNotBlank(result.getUri())) {
            metadata.put("source", result.getUri());
        }
        if (StringUtils.isNotBlank(result.getType())) {
            metadata.put("type", result.getType());
        }
        if (result.getDocumentAttributes() != null) {
            metadata.putAll(result.getDocumentAttributes());
        }
        if (result.getMetadata() != null) {
            metadata.putAll(result.getMetadata());
        }

        document.setMetadata(metadata);
        return document;
    }

    /**
     * Convert to AWS Kendra Document
     */
    private software.amazon.awssdk.services.kendra.model.Document convertToKendraDocument(KendraDocument kendraDoc) {
        software.amazon.awssdk.services.kendra.model.Document.Builder builder =
            software.amazon.awssdk.services.kendra.model.Document.builder()
                .id(kendraDoc.getId())
                .blob(software.amazon.awssdk.core.SdkBytes.fromUtf8String(kendraDoc.getContent()))
                .contentType(ContentType.fromValue(kendraDoc.getContentType()));

        if (StringUtils.isNotBlank(kendraDoc.getTitle())) {
            builder.title(kendraDoc.getTitle());
        }

        if (kendraDoc.getAttributes() != null && !kendraDoc.getAttributes().isEmpty()) {
            List<DocumentAttribute> attributes = kendraDoc.getAttributes().entrySet().stream()
                .map(entry -> DocumentAttribute.builder()
                    .key(entry.getKey())
                    .value(DocumentAttributeValue.builder()
                        .stringValue(entry.getValue().toString())
                        .build())
                    .build())
                .collect(Collectors.toList());
            builder.attributes(attributes);
        }

        // Language code is not supported in AWS Kendra Document API
        // Remove the languageCode setting

        return builder.build();
    }

    /**
     * Convert AWS QueryResponse to KendraQueryResponse
     */
    private KendraQueryResponse convertToKendraQueryResponse(QueryResponse response, String queryText) {
        KendraQueryResponse kendraResponse = new KendraQueryResponse();
        kendraResponse.setQueryId(response.queryId());
        kendraResponse.setTotalNumberOfResults(response.totalNumberOfResults());

        if (response.hasResultItems()) {
            List<KendraResult> results = response.resultItems().stream()
                .map(this::convertToKendraResult)
                .collect(Collectors.toList());
            kendraResponse.setResults(results);
        } else {
            kendraResponse.setResults(new ArrayList<>());
        }

        return kendraResponse;
    }

    /**
     * Convert AWS QueryResultItem to KendraResult
     */
    private KendraResult convertToKendraResult(QueryResultItem item) {
        KendraResult result = new KendraResult();
        result.setId(item.id());
        result.setType(item.typeAsString());
        result.setScore(item.scoreAttributes() != null ? Double.valueOf(item.scoreAttributes().scoreConfidence().toString()) : null);

        if (item.documentTitle() != null && item.documentTitle().text() != null) {
            result.setTitle(item.documentTitle().text());
        }

        if (item.documentExcerpt() != null && item.documentExcerpt().text() != null) {
            result.setContent(item.documentExcerpt().text());
        }

        if (item.documentURI() != null) {
            result.setUri(item.documentURI());
        }

        if (item.feedbackToken() != null) {
            result.setFeedbackToken(item.feedbackToken());
        }

        if (item.hasDocumentAttributes()) {
            Map<String, Object> attributes = new HashMap<>();
            item.documentAttributes().forEach(attr -> {
                if (attr.value() != null && attr.value().stringValue() != null) {
                    attributes.put(attr.key(), attr.value().stringValue());
                }
            });
            result.setDocumentAttributes(attributes);
        }

        return result;
    }
}