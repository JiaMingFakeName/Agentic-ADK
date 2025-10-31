package com.alibaba.langengine.douyin;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * 抖音配置类
 */
public class DouyinConfiguration {

    /**
     * 抖音API基础URL
     */
    public static final String DOUYIN_BASE_URL = WorkPropertiesUtils.get("douyin_base_url", "https://www.douyin.com/aweme/v1");

    /**
     * 抖音用户代理
     */
    public static final String DOUYIN_USER_AGENT = WorkPropertiesUtils.get("douyin_user_agent", 
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

    /**
     * 抖音请求超时时间（秒）
     */
    public static final Integer DOUYIN_TIMEOUT = Integer.valueOf(WorkPropertiesUtils.get("douyin_timeout", 30));

    /**
     * 抖音请求重试次数
     */
    public static final Integer DOUYIN_MAX_RETRIES = Integer.valueOf(WorkPropertiesUtils.get("douyin_max_retries", 3));

    /**
     * 抖音请求间隔时间（毫秒）
     */
    public static final Long DOUYIN_REQUEST_INTERVAL = Long.valueOf(WorkPropertiesUtils.get("douyin_request_interval", 2000L));

    private DouyinConfiguration() {
        // 工具类，不允许实例化
    }
}