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
package com.alibaba.langengine.lucene;


public class LuceneException extends RuntimeException {

    /**
     * 错误代码枚举
     */
    public enum ErrorCode {
        INIT_FAILED("初始化失败"),
        ADD_DOCUMENT_FAILED("添加文档失败"),
        DELETE_DOCUMENT_FAILED("删除文档失败"),
        UPDATE_DOCUMENT_FAILED("更新文档失败"),
        SEARCH_FAILED("搜索失败"),
        SEARCH_TIMEOUT("搜索超时"),
        CLEAR_INDEX_FAILED("清空索引失败"),
        GET_COUNT_FAILED("获取文档数量失败"),
        OPTIMIZE_FAILED("优化索引失败"),
        COMMIT_FAILED("提交失败");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;

    public LuceneException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LuceneException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "LuceneException{" +
                "errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
