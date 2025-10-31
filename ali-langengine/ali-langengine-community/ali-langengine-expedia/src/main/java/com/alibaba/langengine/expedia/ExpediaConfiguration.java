package com.alibaba.langengine.expedia;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * Expedia Group API 配置类
 * 
 * @author AIDC-AI
 */
public class ExpediaConfiguration {

    /**
     * Expedia API 基础 URL
     * 默认: https://api.ean.com (Expedia Affiliate Network)
     */
    public static String EXPEDIA_API_BASE_URL = WorkPropertiesUtils.getOrDefault(
        "expedia_api_base_url", "https://api.ean.com");
    
    /**
     * Expedia API Key
     * 从 Expedia Partner Central 获取
     */
    public static String EXPEDIA_API_KEY = WorkPropertiesUtils.get("expedia_api_key");
    
    /**
     * Expedia API Secret
     */
    public static String EXPEDIA_API_SECRET = WorkPropertiesUtils.get("expedia_api_secret");
    
    /**
     * 请求超时时间（秒）
     * 默认: 30
     */
    public static int EXPEDIA_REQUEST_TIMEOUT = Integer.parseInt(
        WorkPropertiesUtils.getOrDefault("expedia_request_timeout", "30"));
    
    /**
     * 默认语言代码
     * 默认: en-US
     */
    public static String EXPEDIA_DEFAULT_LANGUAGE = WorkPropertiesUtils.getOrDefault(
        "expedia_default_language", "en-US");
    
    /**
     * 默认货币代码
     * 默认: USD
     */
    public static String EXPEDIA_DEFAULT_CURRENCY = WorkPropertiesUtils.getOrDefault(
        "expedia_default_currency", "USD");
    
    /**
     * 是否使用沙箱环境
     * 默认: false
     */
    public static boolean EXPEDIA_USE_SANDBOX = Boolean.parseBoolean(
        WorkPropertiesUtils.getOrDefault("expedia_use_sandbox", "false"));
    
    /**
     * 用户代理字符串
     */
    public static String EXPEDIA_USER_AGENT = WorkPropertiesUtils.getOrDefault(
        "expedia_user_agent", "Ali-LangEngine-Expedia/1.0");
    
    /**
     * 最大重试次数
     * 默认: 3
     */
    public static int EXPEDIA_MAX_RETRIES = Integer.parseInt(
        WorkPropertiesUtils.getOrDefault("expedia_max_retries", "3"));
    
    /**
     * 重试间隔（毫秒）
     * 默认: 1000
     */
    public static int EXPEDIA_RETRY_INTERVAL = Integer.parseInt(
        WorkPropertiesUtils.getOrDefault("expedia_retry_interval", "1000"));
}
