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
package com.alibaba.langengine.tensordb.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;


public class TensorDBQueryResponse {

    /**
     * 查询结果文档列表
     */
    @JSONField(name = "results")
    private List<TensorDBDocument> documents;

    /**
     * 查询是否成功
     */
    @JSONField(name = "success")
    private Boolean success = true;

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

    /**
     * 错误信息（如果查询失败）
     */
    @JSONField(name = "error")
    private String error;

    /**
     * 错误码
     */
    @JSONField(name = "error_code")
    private String errorCode;

    public TensorDBQueryResponse() {
    }

    public TensorDBQueryResponse(List<TensorDBDocument> documents) {
        this.documents = documents;
        this.total = documents != null ? documents.size() : 0;
        this.success = true;
    }

    public TensorDBQueryResponse(String error, String errorCode) {
        this.error = error;
        this.errorCode = errorCode;
        this.success = false;
        this.total = 0;
    }

    public List<TensorDBDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TensorDBDocument> documents) {
        this.documents = documents;
        this.total = documents != null ? documents.size() : 0;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        if (error != null) {
            this.success = false;
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 检查响应是否成功
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(success) && error == null;
    }

    /**
     * 检查是否有结果
     */
    public boolean hasResults() {
        return documents != null && !documents.isEmpty();
    }

    @Override
    public String toString() {
        return "TensorDBQueryResponse{" +
                "documents=" + (documents != null ? documents.size() + " items" : "null") +
                ", success=" + success +
                ", total=" + total +
                ", took=" + took +
                ", requestId='" + requestId + '\'' +
                ", error='" + error + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}