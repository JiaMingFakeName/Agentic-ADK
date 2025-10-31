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
package com.alibaba.langengine.usearch.vectorstore.service;

import lombok.Data;


@Data
public class USearchSearchResult {

    /**
     * 向量键
     */
    private long vectorKey;

    /**
     * 相似度分数
     */
    private float distance;

    /**
     * 文档记录
     */
    private USearchDocumentRecord documentRecord;

    public USearchSearchResult() {
    }

    public USearchSearchResult(long vectorKey, float distance) {
        this.vectorKey = vectorKey;
        this.distance = distance;
    }

    public USearchSearchResult(long vectorKey, float distance, USearchDocumentRecord documentRecord) {
        this.vectorKey = vectorKey;
        this.distance = distance;
        this.documentRecord = documentRecord;
    }

}
