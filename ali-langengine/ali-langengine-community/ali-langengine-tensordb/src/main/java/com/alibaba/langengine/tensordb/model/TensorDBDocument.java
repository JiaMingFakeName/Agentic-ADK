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
import java.util.Map;


public class TensorDBDocument {

    /**
     * 文档ID
     */
    @JSONField(name = "_id")
    private String id;

    /**
     * 文档内容
     */
    @JSONField(name = "text")
    private String text;

    /**
     * 向量数据
     */
    @JSONField(name = "vector")
    private List<Double> vector;

    /**
     * 元数据
     */
    @JSONField(name = "metadata")
    private Map<String, Object> metadata;

    /**
     * 相似度分数（仅在查询结果中使用）
     */
    @JSONField(name = "score")
    private Double score;

    public TensorDBDocument() {
    }

    public TensorDBDocument(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public TensorDBDocument(String id, String text, List<Double> vector) {
        this.id = id;
        this.text = text;
        this.vector = vector;
    }

    public TensorDBDocument(String id, String text, List<Double> vector, Map<String, Object> metadata) {
        this.id = id;
        this.text = text;
        this.vector = vector;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Double> getVector() {
        return vector;
    }

    public void setVector(List<Double> vector) {
        this.vector = vector;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "TensorDBDocument{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", vector=" + (vector != null ? vector.size() + " dimensions" : "null") +
                ", metadata=" + metadata +
                ", score=" + score +
                '}';
    }
}