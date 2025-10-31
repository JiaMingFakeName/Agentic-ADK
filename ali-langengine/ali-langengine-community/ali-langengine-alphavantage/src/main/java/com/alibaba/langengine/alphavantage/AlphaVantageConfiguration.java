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
package com.alibaba.langengine.alphavantage;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class AlphaVantageConfiguration {
    /**
     * Alpha Vantage API key for authentication
     */
    public static String ALPHA_VANTAGE_API_KEY = WorkPropertiesUtils.get("alpha_vantage_api_key");

    /**
     * Alpha Vantage API base URL, defaults to the constant DEFAULT_BASE_URL if not configured
     */
    public static String ALPHA_VANTAGE_API_URL = WorkPropertiesUtils.get("alpha_vantage_api_url", AlphaVantageConstant.DEFAULT_BASE_URL);
    
    /**
     * Alpha Vantage API connect timeout in milliseconds
     */
    public static Long ALPHA_VANTAGE_CONNECT_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("alpha_vantage_connect_timeout", "30000"));
    
    /**
     * Alpha Vantage API read timeout in milliseconds
     */
    public static Long ALPHA_VANTAGE_READ_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("alpha_vantage_read_timeout", "30000"));
    
    /**
     * Alpha Vantage API write timeout in milliseconds
     */
    public static Long ALPHA_VANTAGE_WRITE_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("alpha_vantage_write_timeout", "30000"));
}
