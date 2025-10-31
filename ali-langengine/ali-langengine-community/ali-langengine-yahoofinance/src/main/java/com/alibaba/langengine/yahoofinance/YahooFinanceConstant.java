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

public class YahooFinanceConstant {
    /**
     * Default Yahoo Finance API base URL
     */
    public static final String DEFAULT_BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart";
    
    /**
     * Default user agent
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * Interval constants
     */
    public static final String INTERVAL_1MIN = "1m";
    public static final String INTERVAL_2MIN = "2m";
    public static final String INTERVAL_5MIN = "5m";
    public static final String INTERVAL_15MIN = "15m";
    public static final String INTERVAL_30MIN = "30m";
    public static final String INTERVAL_60MIN = "60m";
    public static final String INTERVAL_90MIN = "90m";
    public static final String INTERVAL_1HOUR = "1h";
    public static final String INTERVAL_1DAY = "1d";
    public static final String INTERVAL_5DAY = "5d";
    public static final String INTERVAL_1WEEK = "1wk";
    public static final String INTERVAL_1MONTH = "1mo";
    public static final String INTERVAL_3MONTH = "3mo";
    
    /**
     * Range constants
     */
    public static final String RANGE_1DAY = "1d";
    public static final String RANGE_5DAY = "5d";
    public static final String RANGE_1MONTH = "1mo";
    public static final String RANGE_3MONTH = "3mo";
    public static final String RANGE_6MONTH = "6mo";
    public static final String RANGE_1YEAR = "1y";
    public static final String RANGE_2YEAR = "2y";
    public static final String RANGE_5YEAR = "5y";
    public static final String RANGE_10YEAR = "10y";
    public static final String RANGE_YTD = "ytd";
    public static final String RANGE_MAX = "max";
}
