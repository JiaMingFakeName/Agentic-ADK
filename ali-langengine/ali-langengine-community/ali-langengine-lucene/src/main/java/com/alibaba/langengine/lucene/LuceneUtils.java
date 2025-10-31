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

import com.alibaba.langengine.core.indexes.Document;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@Slf4j
@UtilityClass
public class LuceneUtils {

    /**
     * 验证文档参数
     * 
     * @param document 要验证的文档
     * @throws IllegalArgumentException 如果文档无效
     */
    public static void validateDocument(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("文档不能为null");
        }
        
        if (StringUtils.isBlank(document.getPageContent())) {
            log.warn("文档内容为空，ID: {}", getDocumentId(document));
        }
    }

    /**
     * 验证文档列表
     * 
     * @param documents 要验证的文档列表
     * @throws IllegalArgumentException 如果文档列表无效
     */
    public static void validateDocuments(List<Document> documents) {
        if (documents == null) {
            throw new IllegalArgumentException("文档列表不能为null");
        }
        
        if (documents.isEmpty()) {
            log.warn("文档列表为空");
            return;
        }
        
        for (int i = 0; i < documents.size(); i++) {
            try {
                validateDocument(documents.get(i));
            } catch (Exception e) {
                log.error("文档验证失败，索引: {}, 错误: {}", i, e.getMessage());
                throw new IllegalArgumentException("文档列表中第" + i + "个文档无效: " + e.getMessage());
            }
        }
    }

    /**
     * 获取文档ID，如果没有则生成一个
     * 
     * @param document 文档
     * @return 文档ID
     */
    public static String getDocumentId(Document document) {
        if (document.getMetadata() != null && document.getMetadata().containsKey("id")) {
            return String.valueOf(document.getMetadata().get("id"));
        }
        
        if (StringUtils.isNotBlank(document.getUniqueId())) {
            return document.getUniqueId();
        }
        
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * 验证查询参数
     * 
     * @param query 查询字符串
     * @param k 返回结果数量
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void validateSearchParams(String query, int k) {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("查询字符串不能为空");
        }
        
        if (k <= 0) {
            throw new IllegalArgumentException("返回结果数量必须大于0");
        }
        
        if (k > 10000) {
            log.warn("返回结果数量过大: {}, 建议不超过10000", k);
        }
    }

    /**
     * 安全地转换向量
     * 
     * @param embedVector 向量列表
     * @return 转换后的float数组
     */
    public static float[] convertToFloatArray(List<Double> embedVector) {
        if (embedVector == null || embedVector.isEmpty()) {
            return new float[0];
        }
        
        float[] result = new float[embedVector.size()];
        for (int i = 0; i < embedVector.size(); i++) {
            Double value = embedVector.get(i);
            result[i] = value != null ? value.floatValue() : 0.0f;
        }
        return result;
    }

    /**
     * 清理查询字符串
     * 
     * @param query 原始查询
     * @return 清理后的查询
     */
    public static String sanitizeQuery(String query) {
        if (StringUtils.isBlank(query)) {
            return "";
        }
        
        // 移除特殊字符以避免查询解析错误
        return query.replaceAll("[+\\-!(){}\\[\\]^\"~*?:\\\\]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    /**
     * 检查索引路径的有效性
     * 
     * @param indexPath 索引路径
     * @return 是否为有效路径
     */
    public static boolean isValidIndexPath(String indexPath) {
        if (StringUtils.isBlank(indexPath)) {
            return false;
        }
        
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(indexPath);
            return true;
        } catch (Exception e) {
            log.warn("无效的索引路径: {}, 错误: {}", indexPath, e.getMessage());
            return false;
        }
    }
}
