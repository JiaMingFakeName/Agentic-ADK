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
package com.alibaba.langengine.zhihu;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * 知乎配置类
 *
 * @author aihe.ah
 */
public class ZhihuConfiguration {

    /**
     * 知乎API基础URL
     */
    public static final String ZHIHU_BASE_URL = WorkPropertiesUtils.get("zhihu_base_url", "https://www.zhihu.com/api/v4");

    /**
     * 知乎用户代理
     */
    public static final String ZHIHU_USER_AGENT = WorkPropertiesUtils.get("zhihu_user_agent", 
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

    /**
     * 知乎请求超时时间（秒）
     */
    public static final Integer ZHIHU_TIMEOUT = Integer.valueOf(WorkPropertiesUtils.get("zhihu_timeout", 30));

    /**
     * 知乎请求重试次数
     */
    public static final Integer ZHIHU_MAX_RETRIES = Integer.valueOf(WorkPropertiesUtils.get("zhihu_max_retries", 3));

    /**
     * 知乎请求间隔时间（毫秒）
     */
    public static final Long ZHIHU_REQUEST_INTERVAL = Long.valueOf(WorkPropertiesUtils.get("zhihu_request_interval", 1000L));

    private ZhihuConfiguration() {
        // 工具类，不允许实例化
    }
}