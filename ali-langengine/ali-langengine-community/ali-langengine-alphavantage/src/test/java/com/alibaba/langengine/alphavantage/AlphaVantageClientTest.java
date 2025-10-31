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

import com.alibaba.langengine.alphavantage.AlphaVantageClient;
import com.alibaba.langengine.alphavantage.AlphaVantageException;
import com.alibaba.langengine.alphavantage.AlphaVantageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Alpha Vantage 客户端测试")
public class AlphaVantageClientTest {
    
    private AlphaVantageClient client;
    
    @BeforeEach
    void setUp() {
        client = new AlphaVantageClient();
    }
    
    @Nested
    @DisplayName("客户端初始化测试")
    class ClientInitializationTests {
        
        @Test
        @DisplayName("默认构造函数测试")
        void testDefaultConstructor() {
            AlphaVantageClient defaultClient = new AlphaVantageClient();
            assertNotNull(defaultClient);
        }
        
        @Test
        @DisplayName("自定义参数构造函数测试")
        void testCustomConstructor() {
            AlphaVantageClient customClient = new AlphaVantageClient(
                "https://www.alphavantage.co/query", 
                "test-api-key"
            );
            assertNotNull(customClient);
        }
    }
    
    @Nested
    @DisplayName("实时报价测试")
    class GlobalQuoteTests {
        
        @Test
        @DisplayName("获取苹果公司实时报价")
        void testGetAppleGlobalQuote() throws Exception {
            AlphaVantageResponse response = client.getGlobalQuote("AAPL");
            assertNotNull(response);
            System.out.println("Apple Global Quote: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"})
        @DisplayName("获取多个股票实时报价")
        void testGetMultipleGlobalQuotes(String symbol) throws Exception {
            AlphaVantageResponse response = client.getGlobalQuote(symbol);
            assertNotNull(response);
            System.out.println(symbol + " Global Quote: " + response);
        }
        
        @Test
        @DisplayName("无效股票代码测试")
        void testInvalidSymbolGlobalQuote() {
            assertThrows(AlphaVantageException.class, () -> {
                client.getGlobalQuote("INVALID_SYMBOL_12345");
            });
        }
    }
    
    @Nested
    @DisplayName("时间序列数据测试")
    class TimeSeriesTests {
        
        @Test
        @DisplayName("获取日线数据")
        void testGetDailyTimeSeries() throws Exception {
            AlphaVantageResponse response = client.getDailyTimeSeries("AAPL", "compact");
            assertNotNull(response);
            System.out.println("Daily Time Series: " + response);
        }
        
        @Test
        @DisplayName("获取完整日线数据")
        void testGetFullDailyTimeSeries() throws Exception {
            AlphaVantageResponse response = client.getDailyTimeSeries("AAPL", "full");
            assertNotNull(response);
            System.out.println("Full Daily Time Series: " + response);
        }
        
        @Test
        @DisplayName("获取周线数据")
        void testGetWeeklyTimeSeries() throws Exception {
            AlphaVantageResponse response = client.getWeeklyTimeSeries("AAPL");
            assertNotNull(response);
            System.out.println("Weekly Time Series: " + response);
        }
        
        @Test
        @DisplayName("获取月线数据")
        void testGetMonthlyTimeSeries() throws Exception {
            AlphaVantageResponse response = client.getMonthlyTimeSeries("AAPL");
            assertNotNull(response);
            System.out.println("Monthly Time Series: " + response);
        }
        
        @Test
        @DisplayName("获取日内数据")
        void testGetIntradayTimeSeries() throws Exception {
            AlphaVantageResponse response = client.getIntradayTimeSeries("AAPL", "1min", "compact");
            assertNotNull(response);
            System.out.println("Intraday Time Series: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1min", "5min", "15min", "30min", "60min"})
        @DisplayName("获取不同时间间隔的日内数据")
        void testGetDifferentIntervalIntradayData(String interval) throws Exception {
            AlphaVantageResponse response = client.getIntradayTimeSeries("AAPL", interval, "compact");
            assertNotNull(response);
            System.out.println(interval + " Intraday Data: " + response);
        }
    }
    
    @Nested
    @DisplayName("搜索功能测试")
    class SymbolSearchTests {
        
        @Test
        @DisplayName("搜索苹果公司")
        void testSearchApple() throws Exception {
            AlphaVantageResponse response = client.searchSymbol("Apple");
            assertNotNull(response);
            System.out.println("Apple Search: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"Microsoft", "Google", "Amazon", "Tesla", "Meta"})
        @DisplayName("搜索多个公司")
        void testSearchMultipleCompanies(String company) throws Exception {
            AlphaVantageResponse response = client.searchSymbol(company);
            assertNotNull(response);
            System.out.println(company + " Search: " + response);
        }
        
        @Test
        @DisplayName("搜索无效关键词")
        void testSearchInvalidKeyword() throws Exception {
            AlphaVantageResponse response = client.searchSymbol("NonExistentCompany12345");
            assertNotNull(response);
            System.out.println("Invalid Search: " + response);
        }
    }
    
    @Nested
    @DisplayName("公司信息测试")
    class CompanyOverviewTests {
        
        @Test
        @DisplayName("获取苹果公司概览")
        void testGetAppleOverview() throws Exception {
            AlphaVantageResponse response = client.getCompanyOverview("AAPL");
            assertNotNull(response);
            System.out.println("Apple Overview: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"})
        @DisplayName("获取多个公司概览")
        void testGetMultipleCompanyOverviews(String symbol) throws Exception {
            AlphaVantageResponse response = client.getCompanyOverview(symbol);
            assertNotNull(response);
            System.out.println(symbol + " Overview: " + response);
        }
    }
    
    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("空字符串参数测试")
        void testEmptyStringParameter() {
            assertThrows(AlphaVantageException.class, () -> {
                client.getGlobalQuote("");
            });
        }
        
        @Test
        @DisplayName("null参数测试")
        void testNullParameter() {
            assertThrows(AlphaVantageException.class, () -> {
                client.getGlobalQuote(null);
            });
        }
        
        @Test
        @DisplayName("特殊字符参数测试")
        void testSpecialCharacterParameter() {
            assertThrows(AlphaVantageException.class, () -> {
                client.getGlobalQuote("!@#$%^&*()");
            });
        }
    }
    
    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {
        
        @Test
        @DisplayName("连续请求测试")
        void testConsecutiveRequests() throws Exception {
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 3; i++) {
                AlphaVantageResponse response = client.getGlobalQuote("AAPL");
                assertNotNull(response);
                System.out.println("Request " + (i + 1) + ": " + response);
                
                // 添加延迟以避免API限制
                Thread.sleep(2000);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Total time for 3 requests: " + (endTime - startTime) + "ms");
        }
    }
}
