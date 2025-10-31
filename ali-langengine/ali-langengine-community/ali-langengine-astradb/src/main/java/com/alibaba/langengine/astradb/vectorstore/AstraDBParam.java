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
package com.alibaba.langengine.astradb.vectorstore;

import com.alibaba.langengine.astradb.utils.Constants;
import lombok.Data;


@Data
public class AstraDBParam {
    
    private InitParam initParam = new InitParam();
    private String fieldNamePageContent = Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT;
    private String fieldNameUniqueId = Constants.DEFAULT_FIELD_NAME_UNIQUE_ID;
    private String fieldMeta = Constants.DEFAULT_FIELD_META;
    private String fieldNameVector = Constants.DEFAULT_FIELD_NAME_VECTOR;

    @Data
    public static class InitParam {
        private String collectionName = Constants.DEFAULT_COLLECTION_NAME;
        private String vectorSimilarityFunction = Constants.DEFAULT_SIMILARITY_FUNCTION;
        private Integer vectorDimensions = Constants.DEFAULT_VECTOR_DIMENSIONS;
        private Integer requestTimeoutMs = Constants.DEFAULT_REQUEST_TIMEOUT_MS;
        private Integer maxBatchSize = Constants.DEFAULT_MAX_BATCH_SIZE;
        
        public String getCollectionName() {
            return collectionName;
        }
    }
}
