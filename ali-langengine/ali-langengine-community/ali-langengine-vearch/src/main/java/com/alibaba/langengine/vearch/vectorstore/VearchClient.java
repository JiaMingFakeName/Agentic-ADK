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
package com.alibaba.langengine.vearch.vectorstore;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class VearchClient extends RetrofitInitService<VearchApi> {

    private String databaseName;
    private String spaceName;

    public VearchClient(String serverUrl, Duration timeout) {
        super(serverUrl, timeout, false, null, null);
    }

    public VearchClient(String serverUrl, Duration timeout, String databaseName, String spaceName) {
        super(serverUrl, timeout, false, null, null);
        this.databaseName = databaseName;
        this.spaceName = spaceName;
    }

    @Override
    public Class<VearchApi> getServiceApiClass() {
        return VearchApi.class;
    }

    /**
     * 创建数据库
     */
    public VearchResponse createDatabase(String dbName) {
        return executeWithErrorHandling(() -> execute(getApi().createDatabase(dbName)),
                                      "create database", dbName);
    }

    /**
     * 创建表空间
     */
    public VearchResponse createSpace(String dbName, String spaceName, Map<String, Object> spaceConfig) {
        return executeWithErrorHandling(() -> execute(getApi().createSpace(dbName, spaceConfig)),
                                      "create space", spaceName + " in database " + dbName);
    }

    /**
     * 获取表空间信息
     */
    public VearchResponse getSpace(String dbName, String spaceName) {
        return executeWithErrorHandling(() -> execute(getApi().getSpace(dbName, spaceName)),
                                      "get space", spaceName + " in database " + dbName);
    }

    /**
     * 插入文档
     */
    public VearchResponse upsertDocuments(VearchUpsertRequest request) {
        if (databaseName == null || spaceName == null) {
            throw new VearchConfigurationException("Database name and space name must be configured");
        }

        return executeWithErrorHandling(() -> execute(getApi().upsertDocuments(databaseName, spaceName, request)),
                                      "upsert documents", "to space " + spaceName);
    }

    /**
     * 向量搜索
     */
    public VearchQueryResponse search(VearchQueryRequest request) {
        if (databaseName == null || spaceName == null) {
            throw new VearchConfigurationException("Database name and space name must be configured");
        }

        return executeWithErrorHandling(() -> execute(getApi().search(databaseName, spaceName, request)),
                                      "search", "in space " + spaceName);
    }

    /**
     * 删除文档
     */
    public VearchResponse deleteDocument(String documentId) {
        if (databaseName == null || spaceName == null) {
            throw new VearchConfigurationException("Database name and space name must be configured");
        }

        return executeWithErrorHandling(() -> execute(getApi().deleteDocument(databaseName, spaceName, documentId)),
                                      "delete document", documentId + " from space " + spaceName);
    }

    /**
     * 批量删除文档
     */
    public VearchResponse bulkDeleteDocuments(Map<String, Object> deleteRequest) {
        if (databaseName == null || spaceName == null) {
            throw new VearchConfigurationException("Database name and space name must be configured");
        }

        return executeWithErrorHandling(() -> execute(getApi().bulkDeleteDocuments(databaseName, spaceName, deleteRequest)),
                                      "bulk delete documents", "from space " + spaceName);
    }

    /**
     * 统一错误处理
     */
    private <T> T executeWithErrorHandling(VearchOperation<T> operation, String operationType, String target) {
        try {
            return operation.execute();
        } catch (Exception e) {
            String errorMessage = String.format("Failed to %s: %s", operationType, target);
            log.error(errorMessage, e);

            // 根据异常类型进行分类
            if (isConnectionError(e)) {
                throw new VearchConnectionException(errorMessage, e);
            } else if (isConfigurationError(e)) {
                throw new VearchConfigurationException(errorMessage, e);
            } else {
                throw new VearchOperationException(errorMessage, e);
            }
        }
    }

    /**
     * 判断是否为连接错误
     */
    private boolean isConnectionError(Exception e) {
        String message = e.getMessage();
        return message != null && (
            message.contains("connect") ||
            message.contains("timeout") ||
            message.contains("network") ||
            message.contains("socket")
        );
    }

    /**
     * 判断是否为配置错误
     */
    private boolean isConfigurationError(Exception e) {
        String message = e.getMessage();
        return message != null && (
            message.contains("authentication") ||
            message.contains("unauthorized") ||
            message.contains("forbidden")
        );
    }

    /**
     * 函数式接口，用于操作执行
     */
    @FunctionalInterface
    private interface VearchOperation<T> {
        T execute() throws Exception;
    }

}