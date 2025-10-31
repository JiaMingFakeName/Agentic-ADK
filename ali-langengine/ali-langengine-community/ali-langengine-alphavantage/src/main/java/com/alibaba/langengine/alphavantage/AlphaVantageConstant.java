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

public class AlphaVantageConstant {
    /**
     * Default Alpha Vantage API base URL
     */
    public static final String DEFAULT_BASE_URL = "https://www.alphavantage.co/query";
    
    /**
     * API function names
     */
    public static final String FUNCTION_TIME_SERIES_INTRADAY = "TIME_SERIES_INTRADAY";
    public static final String FUNCTION_TIME_SERIES_DAILY = "TIME_SERIES_DAILY";
    public static final String FUNCTION_TIME_SERIES_WEEKLY = "TIME_SERIES_WEEKLY";
    public static final String FUNCTION_TIME_SERIES_MONTHLY = "TIME_SERIES_MONTHLY";
    public static final String FUNCTION_GLOBAL_QUOTE = "GLOBAL_QUOTE";
    public static final String FUNCTION_SYMBOL_SEARCH = "SYMBOL_SEARCH";
    public static final String FUNCTION_OVERVIEW = "OVERVIEW";
    public static final String FUNCTION_INCOME_STATEMENT = "INCOME_STATEMENT";
    public static final String FUNCTION_BALANCE_SHEET = "BALANCE_SHEET";
    public static final String FUNCTION_CASH_FLOW = "CASH_FLOW";
    
    /**
     * Interval constants
     */
    public static final String INTERVAL_1MIN = "1min";
    public static final String INTERVAL_5MIN = "5min";
    public static final String INTERVAL_15MIN = "15min";
    public static final String INTERVAL_30MIN = "30min";
    public static final String INTERVAL_60MIN = "60min";
    
    /**
     * Output size constants
     */
    public static final String OUTPUT_SIZE_COMPACT = "compact";
    public static final String OUTPUT_SIZE_FULL = "full";
    
    /**
     * Data type constants
     */
    public static final String DATA_TYPE_JSON = "json";
    public static final String DATA_TYPE_CSV = "csv";
}
