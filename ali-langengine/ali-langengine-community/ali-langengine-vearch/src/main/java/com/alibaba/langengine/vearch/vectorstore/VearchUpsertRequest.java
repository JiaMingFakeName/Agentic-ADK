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

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class VearchUpsertRequest {

    /**
     * 文档列表
     */
    @JSONField(name = "documents")
    private List<VearchDocument> documents;

    @Data
    public static class VearchDocument {

        /**
         * 文档ID
         */
        @JSONField(name = "_id")
        private String id;

        /**
         * 向量数据
         */
        @JSONField(name = "vector")
        private List<Float> vector;

        /**
         * 文档字段数据
         */
        @JSONField(name = "fields")
        private Map<String, Object> fields;

    }

}