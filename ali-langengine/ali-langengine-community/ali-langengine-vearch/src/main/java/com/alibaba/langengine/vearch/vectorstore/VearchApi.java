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

import io.reactivex.Single;
import retrofit2.http.*;

import java.util.Map;


public interface VearchApi {

    /**
     * 创建数据库
     */
    @POST("/dbs/{db_name}")
    @Headers({"Content-Type: application/json"})
    Single<VearchResponse> createDatabase(@Path("db_name") String dbName);

    /**
     * 创建表空间
     */
    @POST("/dbs/{db_name}/spaces")
    @Headers({"Content-Type: application/json"})
    Single<VearchResponse> createSpace(@Path("db_name") String dbName, @Body Map<String, Object> spaceConfig);

    /**
     * 获取表空间信息
     */
    @GET("/dbs/{db_name}/spaces/{space_name}")
    @Headers({"Content-Type: application/json"})
    Single<VearchResponse> getSpace(@Path("db_name") String dbName, @Path("space_name") String spaceName);

    /**
     * 插入文档
     */
    @POST("/dbs/{db_name}/spaces/{space_name}/documents")
    @Headers({"Content-Type: application/json"})
    Single<VearchResponse> upsertDocuments(
        @Path("db_name") String dbName,
        @Path("space_name") String spaceName,
        @Body VearchUpsertRequest request);

    /**
     * 向量搜索
     */
    @POST("/dbs/{db_name}/spaces/{space_name}/_search")
    @Headers({"Content-Type: application/json"})
    Single<VearchQueryResponse> search(
        @Path("db_name") String dbName,
        @Path("space_name") String spaceName,
        @Body VearchQueryRequest request);

    /**
     * 根据ID删除文档
     */
    @DELETE("/dbs/{db_name}/spaces/{space_name}/documents/{document_id}")
    @Headers({"Content-Type: application/json"})
    Single<VearchResponse> deleteDocument(
        @Path("db_name") String dbName,
        @Path("space_name") String spaceName,
        @Path("document_id") String documentId);

    /**
     * 批量删除文档
     */
    @POST("/dbs/{db_name}/spaces/{space_name}/documents/_bulk_delete")
    @Headers({"Content-Type: application/json"})
    Single<VearchResponse> bulkDeleteDocuments(
        @Path("db_name") String dbName,
        @Path("space_name") String spaceName,
        @Body Map<String, Object> deleteRequest);

}