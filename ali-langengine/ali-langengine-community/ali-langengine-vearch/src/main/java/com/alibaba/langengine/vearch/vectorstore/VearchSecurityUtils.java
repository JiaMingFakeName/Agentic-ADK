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

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;


public class VearchSecurityUtils {

    // 数据库名和空间名的有效字符正则
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_\\-]{0,62}$");

    // 文档ID的有效字符正则
    private static final Pattern DOCUMENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_\\-\\.]{0,254}$");

    // 恶意字符或脚本检测
    private static final Pattern MALICIOUS_PATTERN = Pattern.compile(
        "(?i)(<script|</script|javascript:|vbscript:|onload=|onerror=|<iframe|</iframe|<object|</object)"
    );

    /**
     * 验证服务器URL
     */
    public static String validateServerUrl(String serverUrl) {
        if (StringUtils.isBlank(serverUrl)) {
            throw new VearchConfigurationException(VearchErrorCode.MISSING_CONFIG,
                                                  "Server URL cannot be null or blank");
        }

        try {
            URL url = new URL(serverUrl.trim());
            String protocol = url.getProtocol();

            // 只允许HTTP和HTTPS协议
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                      "Only HTTP and HTTPS protocols are supported");
            }

            // 检查主机名
            String host = url.getHost();
            if (StringUtils.isBlank(host)) {
                throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                      "Invalid host in server URL");
            }

            // 返回规范化的URL（移除末尾斜杠）
            String normalizedUrl = serverUrl.trim();
            if (normalizedUrl.endsWith("/")) {
                normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
            }

            return normalizedUrl;

        } catch (MalformedURLException e) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Invalid server URL format: " + serverUrl, e);
        }
    }

    /**
     * 验证数据库名称
     */
    public static String validateDatabaseName(String databaseName) {
        if (StringUtils.isBlank(databaseName)) {
            throw new VearchConfigurationException(VearchErrorCode.MISSING_CONFIG,
                                                  "Database name cannot be null or blank");
        }

        String trimmed = databaseName.trim();

        // 检查长度
        if (trimmed.length() < 1 || trimmed.length() > 63) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Database name must be 1-63 characters long");
        }

        // 检查字符规则
        if (!IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Database name must start with letter and contain only letters, numbers, underscore and hyphen");
        }

        return trimmed.toLowerCase();
    }

    /**
     * 验证空间名称
     */
    public static String validateSpaceName(String spaceName) {
        if (StringUtils.isBlank(spaceName)) {
            throw new VearchConfigurationException(VearchErrorCode.MISSING_CONFIG,
                                                  "Space name cannot be null or blank");
        }

        String trimmed = spaceName.trim();

        // 检查长度
        if (trimmed.length() < 1 || trimmed.length() > 63) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Space name must be 1-63 characters long");
        }

        // 检查字符规则
        if (!IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Space name must start with letter and contain only letters, numbers, underscore and hyphen");
        }

        return trimmed.toLowerCase();
    }

    /**
     * 验证文档ID
     */
    public static String validateDocumentId(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            throw new VearchConfigurationException(VearchErrorCode.MISSING_CONFIG,
                                                  "Document ID cannot be null or blank");
        }

        String trimmed = documentId.trim();

        // 检查长度
        if (trimmed.length() < 1 || trimmed.length() > 255) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Document ID must be 1-255 characters long");
        }

        // 检查字符规则
        if (!DOCUMENT_ID_PATTERN.matcher(trimmed).matches()) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_CONFIG,
                                                  "Document ID contains invalid characters");
        }

        return trimmed;
    }

    /**
     * 清理文本内容，移除潜在恶意字符
     */
    public static String sanitizeTextContent(String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        String cleaned = content.trim();

        // 检测恶意模式
        if (MALICIOUS_PATTERN.matcher(cleaned).find()) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_DATA_FORMAT,
                                                  "Content contains potentially malicious patterns");
        }

        // 限制长度
        if (cleaned.length() > 65536) { // 64KB limit
            throw new VearchConfigurationException(VearchErrorCode.DATA_TOO_LARGE,
                                                  "Content size exceeds maximum limit (64KB)");
        }

        return cleaned;
    }

    /**
     * 验证向量维度
     */
    public static int validateDimension(int dimension) {
        if (dimension <= 0) {
            throw new VearchConfigurationException(VearchErrorCode.INVALID_VECTOR_DIMENSION,
                                                  "Vector dimension must be positive: " + dimension);
        }

        if (dimension > 32768) { // 32K 维度限制
            throw new VearchConfigurationException(VearchErrorCode.INVALID_VECTOR_DIMENSION,
                                                  "Vector dimension exceeds maximum limit (32768): " + dimension);
        }

        return dimension;
    }

    /**
     * 安全地遮蔽敏感信息用于日志记录
     */
    public static String maskSensitiveUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return "[EMPTY_URL]";
        }

        try {
            URL parsedUrl = new URL(url);
            String userInfo = parsedUrl.getUserInfo();

            if (StringUtils.isNotBlank(userInfo)) {
                // 遮蔽认证信息
                return url.replace(userInfo, "***:***");
            }

            return url;
        } catch (MalformedURLException e) {
            return "[INVALID_URL]";
        }
    }

    /**
     * 安全地遮蔽长文本用于日志记录
     */
    public static String maskLongText(String text) {
        if (StringUtils.isBlank(text)) {
            return "[EMPTY]";
        }

        if (text.length() <= 100) {
            return text;
        }

        return text.substring(0, 50) + "...[" + (text.length() - 50) + " chars]..." + text.substring(text.length() - 20);
    }

    private VearchSecurityUtils() {
        // Utility class
    }
}