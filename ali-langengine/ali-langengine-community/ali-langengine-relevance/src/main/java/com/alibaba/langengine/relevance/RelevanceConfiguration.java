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
package com.alibaba.langengine.relevance;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RelevanceConfiguration {

    /**
     * Relevance AI API 基础 URL
     */
    public static String RELEVANCE_API_URL = WorkPropertiesUtils.get("relevance_api_url", "https://api.relevanceai.com");

    /**
     * Relevance AI API 密钥（敏感信息）
     * 优先从环境变量获取，然后从配置文件获取
     */
    public static String RELEVANCE_API_KEY = getSecureProperty("relevance_api_key", "RELEVANCE_API_KEY");

    /**
     * 项目 ID（可能包含敏感信息）
     * 优先从环境变量获取，然后从配置文件获取
     */
    public static String RELEVANCE_PROJECT_ID = getSecureProperty("relevance_project_id", "RELEVANCE_PROJECT_ID");

    /**
     * 默认向量维度（字符串形式，保持向后兼容）
     */
    public static String RELEVANCE_DEFAULT_VECTOR_SIZE = WorkPropertiesUtils.get("relevance_default_vector_size", "1536");

    /**
     * 默认相似度度量
     */
    public static String RELEVANCE_DEFAULT_METRIC = WorkPropertiesUtils.get("relevance_default_metric", "cosine");

    /**
     * 请求超时时间（毫秒，字符串形式，保持向后兼容）
     */
    public static String RELEVANCE_REQUEST_TIMEOUT = WorkPropertiesUtils.get("relevance_request_timeout", "30000");

    /**
     * 连接超时时间（毫秒，字符串形式，保持向后兼容）
     */
    public static String RELEVANCE_CONNECTION_TIMEOUT = WorkPropertiesUtils.get("relevance_connection_timeout", "10000");

    /**
     * 最大重试次数（字符串形式，保持向后兼容）
     */
    public static String RELEVANCE_MAX_RETRIES = WorkPropertiesUtils.get("relevance_max_retries", "3");

    // 类型安全的访问方法

    /**
     * 获取默认向量维度（类型安全）
     * @return 向量维度，解析失败时返回默认值1536
     */
    public static Integer getDefaultVectorSize() {
        return safeParseInt(RELEVANCE_DEFAULT_VECTOR_SIZE, 1536, "relevance_default_vector_size");
    }

    /**
     * 获取请求超时时间（类型安全）
     * @return 请求超时时间（毫秒），解析失败时返回默认值30000
     */
    public static Integer getRequestTimeout() {
        return safeParseInt(RELEVANCE_REQUEST_TIMEOUT, 30000, "relevance_request_timeout");
    }

    /**
     * 获取连接超时时间（类型安全）
     * @return 连接超时时间（毫秒），解析失败时返回默认值10000
     */
    public static Integer getConnectionTimeout() {
        return safeParseInt(RELEVANCE_CONNECTION_TIMEOUT, 10000, "relevance_connection_timeout");
    }

    /**
     * 获取最大重试次数（类型安全）
     * @return 最大重试次数，解析失败时返回默认值3
     */
    public static Integer getMaxRetries() {
        return safeParseInt(RELEVANCE_MAX_RETRIES, 3, "relevance_max_retries");
    }

    /**
     * 安全解析整数，提供异常处理和默认值
     * @param value 待解析的字符串值
     * @param defaultValue 解析失败时的默认值
     * @param configKey 配置键名，用于日志记录
     * @return 解析后的整数值
     */
    private static Integer safeParseInt(String value, Integer defaultValue, String configKey) {
        if (value == null || value.trim().isEmpty()) {
            log.debug("Configuration '{}' is null or empty, using default value: {}", configKey, defaultValue);
            return defaultValue;
        }

        try {
            Integer parsed = Integer.valueOf(value.trim());
            if (parsed < 0) {
                log.warn("Configuration '{}' has negative value: {}, using default value: {}", configKey, parsed, defaultValue);
                return defaultValue;
            }
            return parsed;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse configuration '{}' value '{}' as integer: {}, using default value: {}",
                    configKey, value, e.getMessage(), defaultValue);
            return defaultValue;
        }
    }

    // 安全敏感信息处理方法

    /**
     * 安全获取敏感配置属性
     * 优先级：环境变量 > 系统属性 > 配置文件 > null
     *
     * @param propertyKey 配置文件中的键名
     * @param envVarName 环境变量名
     * @return 配置值，可能为null
     */
    private static String getSecureProperty(String propertyKey, String envVarName) {
        // 1. 优先从环境变量获取
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.trim().isEmpty()) {
            log.debug("Loaded sensitive property from environment variable: {}", envVarName);
            return envValue.trim();
        }

        // 2. 从系统属性获取（JVM参数 -D）
        String sysValue = System.getProperty(propertyKey);
        if (sysValue != null && !sysValue.trim().isEmpty()) {
            log.debug("Loaded sensitive property from system property: {}", propertyKey);
            return sysValue.trim();
        }

        // 3. 从配置文件获取（最后选择）
        String configValue = WorkPropertiesUtils.get(propertyKey);
        if (configValue != null && !configValue.trim().isEmpty()) {
            log.debug("Loaded sensitive property from config file: {}", propertyKey);
            return configValue.trim();
        }

        log.warn("Sensitive property not found in environment variables, system properties, or config file: {} / {}",
                 envVarName, propertyKey);
        return null;
    }

    /**
     * 验证必要的敏感信息是否已配置
     *
     * @throws IllegalStateException 如果必要的敏感信息缺失
     */
    public static void validateSecurityConfiguration() {
        List<String> missingConfigs = new ArrayList<>();

        if (RELEVANCE_API_KEY == null || RELEVANCE_API_KEY.trim().isEmpty()) {
            missingConfigs.add("RELEVANCE_API_KEY (env: RELEVANCE_API_KEY, property: relevance_api_key)");
        }

        if (RELEVANCE_PROJECT_ID == null || RELEVANCE_PROJECT_ID.trim().isEmpty()) {
            missingConfigs.add("RELEVANCE_PROJECT_ID (env: RELEVANCE_PROJECT_ID, property: relevance_project_id)");
        }

        if (!missingConfigs.isEmpty()) {
            String errorMessage = "Missing required security configuration: " + String.join(", ", missingConfigs) +
                                 ". Please set these as environment variables, system properties, or in configuration file.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // 验证API密钥格式（如果有特定格式要求）
        if (!isValidApiKey(RELEVANCE_API_KEY)) {
            log.error("Invalid API key format. API key should not be empty and should meet security requirements.");
            throw new IllegalStateException("Invalid API key format");
        }

        log.debug("Security configuration validation passed");
    }

    /**
     * 验证API密钥格式的有效性
     *
     * @param apiKey API密钥
     * @return 是否有效
     */
    private static boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        // 基本安全检查：
        // 1. 长度检查（避免过短的密钥）
        if (apiKey.length() < 8) {
            return false;
        }

        // 2. 避免明显的测试/占位符值
        String lowerKey = apiKey.toLowerCase();
        if (lowerKey.contains("test") || lowerKey.contains("demo") ||
            lowerKey.contains("example") || lowerKey.equals("your-api-key-here")) {
            return false;
        }

        return true;
    }

    /**
     * 安全地掩码敏感信息用于日志输出
     *
     * @param sensitiveValue 敏感值
     * @return 掩码后的字符串
     */
    public static String maskSensitiveValue(String sensitiveValue) {
        if (sensitiveValue == null) {
            return "null";
        }
        if (sensitiveValue.isEmpty()) {
            return "empty";
        }
        if (sensitiveValue.length() <= 4) {
            return "***";
        }

        // 显示前2个和后2个字符，中间用*替代
        String prefix = sensitiveValue.substring(0, 2);
        String suffix = sensitiveValue.substring(sensitiveValue.length() - 2);
        int maskLength = Math.min(8, sensitiveValue.length() - 4); // 最多8个*
        String mask = "*".repeat(maskLength);

        return prefix + mask + suffix;
    }
}