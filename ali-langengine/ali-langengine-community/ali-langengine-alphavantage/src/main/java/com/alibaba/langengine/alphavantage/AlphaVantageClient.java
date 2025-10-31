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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.alphavantage.AlphaVantageConfiguration.*;

/**
 * Alpha Vantage API Client for Java
 * This client provides methods to interact with the Alpha Vantage stock API.
 *
 * @author langengine
 */
@Slf4j
@Data
public class AlphaVantageClient {
    
    private final OkHttpClient client;
    
    private final ObjectMapper objectMapper;
    
    private final String baseUrl;
    
    private final String apiKey;
    
    /**
     * Constructs an AlphaVantageClient with a specified base URL and API key.
     * 
     * @param baseUrl the base URL for the Alpha Vantage service
     * @param apiKey the API key for authentication with the Alpha Vantage service
     */
    public AlphaVantageClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        
        // Create OkHttpClient with configuration
        this.client = new OkHttpClient.Builder()
                .connectTimeout(ALPHA_VANTAGE_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ALPHA_VANTAGE_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ALPHA_VANTAGE_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * Constructs an AlphaVantageClient using the default base URL and API key from configuration.
     */
    public AlphaVantageClient() {
        this(ALPHA_VANTAGE_API_URL, ALPHA_VANTAGE_API_KEY);
    }
    
    /**
     * Constructs an AlphaVantageClient with a specified base URL, API key and custom OkHttpClient.
     * 
     * @param baseUrl the base URL for the Alpha Vantage service
     * @param apiKey the API key for authentication with the Alpha Vantage service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public AlphaVantageClient(String baseUrl, String apiKey, OkHttpClient okHttpClient) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Executes a request to the Alpha Vantage API.
     * 
     * @param params the request parameters
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse execute(Map<String, String> params) throws AlphaVantageException {
        try {
            // Add API key to parameters
            params.put("apikey", apiKey);
            
            // Build URL with parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            
            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();
            
            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new AlphaVantageException("API request failed: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new AlphaVantageException("API returned empty response");
                }
                
                // Parse the response
                AlphaVantageResponse alphaVantageResponse = objectMapper.readValue(body.string(), AlphaVantageResponse.class);
                
                // Check if the response indicates an error
                if (alphaVantageResponse.getErrorMessage() != null) {
                    throw new AlphaVantageException("API returned error: " + alphaVantageResponse.getErrorMessage());
                }
                
                if (alphaVantageResponse.getNote() != null) {
                    log.warn("API returned note: " + alphaVantageResponse.getNote());
                }
                
                return alphaVantageResponse;
            }
        } catch (IOException e) {
            throw new AlphaVantageException("Error occurred during API call", e);
        }
    }
    
    /**
     * Get intraday time series data
     * 
     * @param symbol the stock symbol
     * @param interval the time interval
     * @param outputSize the output size (compact or full)
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse getIntradayTimeSeries(String symbol, String interval, String outputSize) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_TIME_SERIES_INTRADAY);
        params.put("symbol", symbol);
        params.put("interval", interval);
        params.put("outputsize", outputSize);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
    
    /**
     * Get daily time series data
     * 
     * @param symbol the stock symbol
     * @param outputSize the output size (compact or full)
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse getDailyTimeSeries(String symbol, String outputSize) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_TIME_SERIES_DAILY);
        params.put("symbol", symbol);
        params.put("outputsize", outputSize);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
    
    /**
     * Get weekly time series data
     * 
     * @param symbol the stock symbol
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse getWeeklyTimeSeries(String symbol) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_TIME_SERIES_WEEKLY);
        params.put("symbol", symbol);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
    
    /**
     * Get monthly time series data
     * 
     * @param symbol the stock symbol
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse getMonthlyTimeSeries(String symbol) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_TIME_SERIES_MONTHLY);
        params.put("symbol", symbol);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
    
    /**
     * Get global quote (real-time quote)
     * 
     * @param symbol the stock symbol
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse getGlobalQuote(String symbol) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_GLOBAL_QUOTE);
        params.put("symbol", symbol);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
    
    /**
     * Search for symbols
     * 
     * @param keywords the search keywords
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse searchSymbol(String keywords) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_SYMBOL_SEARCH);
        params.put("keywords", keywords);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
    
    /**
     * Get company overview
     * 
     * @param symbol the stock symbol
     * @return the Alpha Vantage response result
     * @throws AlphaVantageException thrown when the API call fails
     */
    public AlphaVantageResponse getCompanyOverview(String symbol) throws AlphaVantageException {
        Map<String, String> params = new HashMap<>();
        params.put("function", AlphaVantageConstant.FUNCTION_OVERVIEW);
        params.put("symbol", symbol);
        params.put("datatype", AlphaVantageConstant.DATA_TYPE_JSON);
        
        return execute(params);
    }
}
