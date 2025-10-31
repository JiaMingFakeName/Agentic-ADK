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
package com.alibaba.langengine.relevance.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;


public class RelevanceQueryResponse {

    /**
     * 查询结果文档列表
     */
    @JSONField(name = "documents")
    private List<RelevanceDocument> documents;

    /**
     * 总数量
     */
    @JSONField(name = "total")
    private Integer total;

    /**
     * 查询耗时（毫秒）
     */
    @JSONField(name = "took")
    private Long took;

    /**
     * 请求ID
     */
    @JSONField(name = "request_id")
    private String requestId;

    public RelevanceQueryResponse() {
    }

    public RelevanceQueryResponse(List<RelevanceDocument> documents) {
        this.documents = documents;
        this.total = documents != null ? documents.size() : 0;
    }

    public List<RelevanceDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<RelevanceDocument> documents) {
        this.documents = documents;
        this.total = documents != null ? documents.size() : 0;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "RelevanceQueryResponse{" +
                "documents=" + (documents != null ? documents.size() + " items" : "null") +
                ", total=" + total +
                ", took=" + took +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}