package com.alibaba.langengine.docusign;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * DocuSign 配置类
 * 支持从环境变量或配置文件加载配置信息
 */
public class DocuSignConfiguration {

    /**
     * DocuSign API 基础URL
     * 示例: https://demo.docusign.net (开发环境) 或 https://www.docusign.net (生产环境)
     */
    public static String DOCUSIGN_BASE_URL = WorkPropertiesUtils.get("docusign_base_url");
    
    /**
     * DocuSign 账户ID
     */
    public static String DOCUSIGN_ACCOUNT_ID = WorkPropertiesUtils.get("docusign_account_id");
    
    /**
     * DocuSign 访问令牌
     * 可以通过 OAuth 2.0 或 JWT 方式获取
     */
    public static String DOCUSIGN_ACCESS_TOKEN = WorkPropertiesUtils.get("docusign_access_token");
    
    /**
     * API 请求超时时间（毫秒）
     * 默认: 30000 (30秒)
     */
    public static int DOCUSIGN_REQUEST_TIMEOUT = Integer.parseInt(
        WorkPropertiesUtils.getOrDefault("docusign_request_timeout", "30000"));
    
    /**
     * 重试次数
     * 默认: 3
     */
    public static int DOCUSIGN_MAX_RETRIES = Integer.parseInt(
        WorkPropertiesUtils.getOrDefault("docusign_max_retries", "3"));
    
    /**
     * 重试间隔（毫秒）
     * 默认: 1000 (1秒)
     */
    public static int DOCUSIGN_RETRY_INTERVAL = Integer.parseInt(
        WorkPropertiesUtils.getOrDefault("docusign_retry_interval", "1000"));
    
    /**
     * 是否启用 Webhook
     */
    public static boolean DOCUSIGN_WEBHOOK_ENABLED = Boolean.parseBoolean(
        WorkPropertiesUtils.getOrDefault("docusign_webhook_enabled", "false"));
    
    /**
     * Webhook 回调 URL
     */
    public static String DOCUSIGN_WEBHOOK_URL = WorkPropertiesUtils.get("docusign_webhook_url");
    
    /**
     * 默认邮件主题
     */
    public static String DOCUSIGN_DEFAULT_EMAIL_SUBJECT = WorkPropertiesUtils.getOrDefault(
        "docusign_default_email_subject", "Please sign this document");
    
    /**
     * 默认邮件内容
     */
    public static String DOCUSIGN_DEFAULT_EMAIL_BLURB = WorkPropertiesUtils.getOrDefault(
        "docusign_default_email_blurb", "Please review and sign the document.");
    
    /**
     * 是否启用日志记录
     */
    public static boolean DOCUSIGN_ENABLE_LOGGING = Boolean.parseBoolean(
        WorkPropertiesUtils.getOrDefault("docusign_enable_logging", "true"));
}


