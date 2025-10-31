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
package com.alibaba.langengine.yahoofinance;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class YahooFinanceConfiguration {
    /**
     * Yahoo Finance API base URL
     */
    public static String YAHOO_FINANCE_API_URL = WorkPropertiesUtils.get("yahoo_finance_api_url", YahooFinanceConstant.DEFAULT_BASE_URL);
    
    /**
     * Yahoo Finance API connect timeout in milliseconds
     */
    public static Long YAHOO_FINANCE_CONNECT_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("yahoo_finance_connect_timeout", "30000"));
    
    /**
     * Yahoo Finance API read timeout in milliseconds
     */
    public static Long YAHOO_FINANCE_READ_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("yahoo_finance_read_timeout", "30000"));
    
    /**
     * Yahoo Finance API write timeout in milliseconds
     */
    public static Long YAHOO_FINANCE_WRITE_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("yahoo_finance_write_timeout", "30000"));
    
    /**
     * User agent for requests
     */
    public static String USER_AGENT = WorkPropertiesUtils.get("yahoo_finance_user_agent", YahooFinanceConstant.DEFAULT_USER_AGENT);
}
