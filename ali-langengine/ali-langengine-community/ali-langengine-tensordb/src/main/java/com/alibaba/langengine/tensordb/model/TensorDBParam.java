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

import com.alibaba.langengine.tensordb.TensorDBConfiguration;


public class TensorDBParam {

    /**
     * API 基础 URL
     */
    private String apiUrl;

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 项目 ID
     */
    private String projectId;

    /**
     * 数据集名称
     */
    private String datasetName;

    /**
     * 向量字段名
     */
    private String vectorField;

    /**
     * 文本字段名
     */
    private String textField;

    /**
     * 向量维度
     */
    private Integer vectorSize;

    /**
     * 相似度度量方式
     */
    private String metric;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer requestTimeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 使用默认配置构造
     */
    public TensorDBParam() {
        this.apiUrl = TensorDBConfiguration.TENSORDB_API_URL;
        this.apiKey = TensorDBConfiguration.TENSORDB_API_KEY;
        this.projectId = TensorDBConfiguration.TENSORDB_PROJECT_ID;
        this.vectorField = "vector_";
        this.textField = "text";
        this.vectorSize = TensorDBConfiguration.getDefaultVectorSize();
        this.metric = TensorDBConfiguration.TENSORDB_DEFAULT_METRIC;
        this.connectionTimeout = TensorDBConfiguration.getConnectionTimeout();
        this.requestTimeout = TensorDBConfiguration.getRequestTimeout();
        this.maxRetries = TensorDBConfiguration.getMaxRetries();
    }

    /**
     * Builder 模式构造器
     */
    public static class Builder {
        private TensorDBParam param;

        public Builder() {
            this.param = new TensorDBParam();
        }

        public Builder apiUrl(String apiUrl) {
            param.apiUrl = apiUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            param.apiKey = apiKey;
            return this;
        }

        public Builder projectId(String projectId) {
            param.projectId = projectId;
            return this;
        }

        public Builder datasetName(String datasetName) {
            param.datasetName = datasetName;
            return this;
        }

        public Builder vectorField(String vectorField) {
            param.vectorField = vectorField;
            return this;
        }

        public Builder textField(String textField) {
            param.textField = textField;
            return this;
        }

        public Builder vectorSize(Integer vectorSize) {
            param.vectorSize = vectorSize;
            return this;
        }

        public Builder metric(String metric) {
            param.metric = metric;
            return this;
        }

        public Builder connectionTimeout(Integer connectionTimeout) {
            param.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder requestTimeout(Integer requestTimeout) {
            param.requestTimeout = requestTimeout;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            param.maxRetries = maxRetries;
            return this;
        }

        public TensorDBParam build() {
            return param;
        }
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getVectorField() {
        return vectorField;
    }

    public void setVectorField(String vectorField) {
        this.vectorField = vectorField;
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public Integer getVectorSize() {
        return vectorSize;
    }

    public void setVectorSize(Integer vectorSize) {
        this.vectorSize = vectorSize;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public String toString() {
        return "TensorDBParam{" +
                "apiUrl='" + apiUrl + '\'' +
                ", apiKey='" + TensorDBConfiguration.maskSensitiveValue(apiKey) + '\'' +
                ", projectId='" + TensorDBConfiguration.maskSensitiveValue(projectId) + '\'' +
                ", datasetName='" + datasetName + '\'' +
                ", vectorField='" + vectorField + '\'' +
                ", textField='" + textField + '\'' +
                ", vectorSize=" + vectorSize +
                ", metric='" + metric + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", requestTimeout=" + requestTimeout +
                ", maxRetries=" + maxRetries +
                '}';
    }
}