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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.yahoofinance.YahooFinanceConfiguration.*;

/**
 * Yahoo Finance API Client for Java
 * This client provides methods to interact with the Yahoo Finance API.
 *
 * @author langengine
 */
@Slf4j
@Data
public class YahooFinanceClient {
    
    private final OkHttpClient client;
    
    private final ObjectMapper objectMapper;
    
    private final String baseUrl;
    
    private final String userAgent;
    
    /**
     * Constructs a YahooFinanceClient with a specified base URL and user agent.
     * 
     * @param baseUrl the base URL for the Yahoo Finance service
     * @param userAgent the user agent for HTTP requests
     */
    public YahooFinanceClient(String baseUrl, String userAgent) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.objectMapper = new ObjectMapper();
        
        // Create OkHttpClient with configuration
        this.client = new OkHttpClient.Builder()
                .connectTimeout(YAHOO_FINANCE_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(YAHOO_FINANCE_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(YAHOO_FINANCE_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * Constructs a YahooFinanceClient using the default base URL and user agent from configuration.
     */
    public YahooFinanceClient() {
        this(YAHOO_FINANCE_API_URL, USER_AGENT);
    }
    
    /**
     * Constructs a YahooFinanceClient with a specified base URL, user agent and custom OkHttpClient.
     * 
     * @param baseUrl the base URL for the Yahoo Finance service
     * @param userAgent the user agent for HTTP requests
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public YahooFinanceClient(String baseUrl, String userAgent, OkHttpClient okHttpClient) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Executes a request to the Yahoo Finance API.
     * 
     * @param symbol the stock symbol
     * @param range the time range
     * @param interval the time interval
     * @return the Yahoo Finance response result
     * @throws YahooFinanceException thrown when the API call fails
     */
    public YahooFinanceResponse getChartData(String symbol, String range, String interval) throws YahooFinanceException {
        try {
            // Build URL with parameters
            HttpUrl url = HttpUrl.parse(baseUrl + "/" + symbol)
                    .newBuilder()
                    .addQueryParameter("range", range)
                    .addQueryParameter("interval", interval)
                    .addQueryParameter("includePrePost", "true")
                    .addQueryParameter("useYfid", "true")
                    .addQueryParameter("corsDomain", "finance.yahoo.com")
                    .addQueryParameter(".tsrc", "finance")
                    .build();
            
            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Referer", "https://finance.yahoo.com/")
                    .build();
            
            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new YahooFinanceException("API request failed: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new YahooFinanceException("API returned empty response");
                }
                
                // Parse the response
                YahooFinanceResponse yahooFinanceResponse = objectMapper.readValue(body.string(), YahooFinanceResponse.class);
                
                // Check if the response indicates an error
                if (yahooFinanceResponse.getChart() != null && 
                    yahooFinanceResponse.getChart().getError() != null) {
                    YahooFinanceResponse.Error error = yahooFinanceResponse.getChart().getError();
                    throw new YahooFinanceException("API returned error: " + error.getDescription());
                }
                
                return yahooFinanceResponse;
            }
        } catch (IOException e) {
            throw new YahooFinanceException("Error occurred during API call", e);
        }
    }
    
    /**
     * Get historical data for a symbol
     * 
     * @param symbol the stock symbol
     * @param range the time range
     * @param interval the time interval
     * @return the Yahoo Finance response result
     * @throws YahooFinanceException thrown when the API call fails
     */
    public YahooFinanceResponse getHistoricalData(String symbol, String range, String interval) throws YahooFinanceException {
        return getChartData(symbol, range, interval);
    }
    
    /**
     * Get real-time quote data
     * 
     * @param symbol the stock symbol
     * @return the Yahoo Finance response result
     * @throws YahooFinanceException thrown when the API call fails
     */
    public YahooFinanceResponse getQuote(String symbol) throws YahooFinanceException {
        return getChartData(symbol, YahooFinanceConstant.RANGE_1DAY, YahooFinanceConstant.INTERVAL_1MIN);
    }
    
    /**
     * Get daily data for a symbol
     * 
     * @param symbol the stock symbol
     * @param range the time range
     * @return the Yahoo Finance response result
     * @throws YahooFinanceException thrown when the API call fails
     */
    public YahooFinanceResponse getDailyData(String symbol, String range) throws YahooFinanceException {
        return getChartData(symbol, range, YahooFinanceConstant.INTERVAL_1DAY);
    }
    
    /**
     * Get intraday data for a symbol
     * 
     * @param symbol the stock symbol
     * @param interval the time interval
     * @return the Yahoo Finance response result
     * @throws YahooFinanceException thrown when the API call fails
     */
    public YahooFinanceResponse getIntradayData(String symbol, String interval) throws YahooFinanceException {
        return getChartData(symbol, YahooFinanceConstant.RANGE_1DAY, interval);
    }
}
