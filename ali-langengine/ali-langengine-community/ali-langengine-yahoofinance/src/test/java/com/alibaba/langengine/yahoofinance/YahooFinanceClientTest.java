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

import com.alibaba.langengine.yahoofinance.YahooFinanceClient;
import com.alibaba.langengine.yahoofinance.YahooFinanceException;
import com.alibaba.langengine.yahoofinance.YahooFinanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Yahoo Finance 客户端测试")
public class YahooFinanceClientTest {
    
    private YahooFinanceClient client;
    
    @BeforeEach
    void setUp() {
        client = new YahooFinanceClient();
    }
    
    @Nested
    @DisplayName("客户端初始化测试")
    class ClientInitializationTests {
        
        @Test
        @DisplayName("默认构造函数测试")
        void testDefaultConstructor() {
            YahooFinanceClient defaultClient = new YahooFinanceClient();
            assertNotNull(defaultClient);
        }
        
        @Test
        @DisplayName("自定义参数构造函数测试")
        void testCustomConstructor() {
            YahooFinanceClient customClient = new YahooFinanceClient(
                "https://query1.finance.yahoo.com/v8/finance/chart",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            );
            assertNotNull(customClient);
        }
    }
    
    @Nested
    @DisplayName("实时报价测试")
    class QuoteTests {
        
        @Test
        @DisplayName("获取苹果公司实时报价")
        void testGetAppleQuote() throws Exception {
            YahooFinanceResponse response = client.getQuote("AAPL");
            assertNotNull(response);
            System.out.println("Apple Quote: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"})
        @DisplayName("获取多个股票实时报价")
        void testGetMultipleQuotes(String symbol) throws Exception {
            YahooFinanceResponse response = client.getQuote(symbol);
            assertNotNull(response);
            System.out.println(symbol + " Quote: " + response);
        }
        
        @Test
        @DisplayName("无效股票代码测试")
        void testInvalidSymbol() {
            assertThrows(YahooFinanceException.class, () -> {
                client.getQuote("INVALID_SYMBOL_12345");
            });
        }
    }
    
    @Nested
    @DisplayName("历史数据测试")
    class HistoricalDataTests {
        
        @Test
        @DisplayName("获取历史数据")
        void testGetHistoricalData() throws Exception {
            YahooFinanceResponse response = client.getHistoricalData("AAPL", "1mo", "1d");
            assertNotNull(response);
            System.out.println("Historical Data: " + response);
        }
        
        @ParameterizedTest
        @CsvSource({
            "AAPL, 1mo, 1d",
            "MSFT, 3mo, 1d",
            "GOOGL, 6mo, 1d",
            "AMZN, 1y, 1d",
            "TSLA, 2y, 1d"
        })
        @DisplayName("参数化测试不同股票和时间范围")
        void testParameterizedHistoricalData(String symbol, String range, String interval) throws Exception {
            YahooFinanceResponse response = client.getHistoricalData(symbol, range, interval);
            assertNotNull(response);
            System.out.println(symbol + " " + range + " " + interval + " Data: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1d", "5d", "1mo", "3mo", "6mo", "1y", "2y", "5y", "10y", "ytd", "max"})
        @DisplayName("测试不同时间范围")
        void testDifferentRanges(String range) throws Exception {
            YahooFinanceResponse response = client.getHistoricalData("AAPL", range, "1d");
            assertNotNull(response);
            System.out.println(range + " Range Data: " + response);
        }
    }
    
    @Nested
    @DisplayName("日线数据测试")
    class DailyDataTests {
        
        @Test
        @DisplayName("获取日线数据")
        void testGetDailyData() throws Exception {
            YahooFinanceResponse response = client.getDailyData("AAPL", "3mo");
            assertNotNull(response);
            System.out.println("Daily Data: " + response);
        }
        
        @ParameterizedTest
        @CsvSource({
            "AAPL, 1mo",
            "MSFT, 3mo",
            "GOOGL, 6mo",
            "AMZN, 1y",
            "TSLA, 2y"
        })
        @DisplayName("参数化测试不同股票的日线数据")
        void testParameterizedDailyData(String symbol, String range) throws Exception {
            YahooFinanceResponse response = client.getDailyData(symbol, range);
            assertNotNull(response);
            System.out.println(symbol + " " + range + " Daily Data: " + response);
        }
    }
    
    @Nested
    @DisplayName("日内数据测试")
    class IntradayDataTests {
        
        @Test
        @DisplayName("获取日内数据")
        void testGetIntradayData() throws Exception {
            YahooFinanceResponse response = client.getIntradayData("AAPL", "5m");
            assertNotNull(response);
            System.out.println("Intraday Data: " + response);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1m", "2m", "5m", "15m", "30m", "60m", "90m", "1h"})
        @DisplayName("获取不同时间间隔的日内数据")
        void testGetDifferentIntervalData(String interval) throws Exception {
            YahooFinanceResponse response = client.getIntradayData("AAPL", interval);
            assertNotNull(response);
            System.out.println(interval + " Intraday Data: " + response);
        }
        
        @ParameterizedTest
        @CsvSource({
            "AAPL, 1m",
            "MSFT, 5m",
            "GOOGL, 15m",
            "AMZN, 30m",
            "TSLA, 60m"
        })
        @DisplayName("参数化测试不同股票的日内数据")
        void testParameterizedIntradayData(String symbol, String interval) throws Exception {
            YahooFinanceResponse response = client.getIntradayData(symbol, interval);
            assertNotNull(response);
            System.out.println(symbol + " " + interval + " Intraday Data: " + response);
        }
    }
    
    @Nested
    @DisplayName("图表数据测试")
    class ChartDataTests {
        
        @Test
        @DisplayName("获取图表数据")
        void testGetChartData() throws Exception {
            YahooFinanceResponse response = client.getChartData("AAPL", "1mo", "1d");
            assertNotNull(response);
            System.out.println("Chart Data: " + response);
        }
        
        @ParameterizedTest
        @CsvSource({
            "AAPL, 1d, 1m",
            "MSFT, 5d, 5m",
            "GOOGL, 1mo, 1d",
            "AMZN, 3mo, 1d",
            "TSLA, 1y, 1d"
        })
        @DisplayName("参数化测试不同参数的图表数据")
        void testParameterizedChartData(String symbol, String range, String interval) throws Exception {
            YahooFinanceResponse response = client.getChartData(symbol, range, interval);
            assertNotNull(response);
            System.out.println(symbol + " " + range + " " + interval + " Chart Data: " + response);
        }
    }
    
    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("空字符串参数测试")
        void testEmptyStringParameter() {
            assertThrows(YahooFinanceException.class, () -> {
                client.getQuote("");
            });
        }
        
        @Test
        @DisplayName("null参数测试")
        void testNullParameter() {
            assertThrows(YahooFinanceException.class, () -> {
                client.getQuote(null);
            });
        }
        
        @Test
        @DisplayName("特殊字符参数测试")
        void testSpecialCharacterParameter() {
            assertThrows(YahooFinanceException.class, () -> {
                client.getQuote("!@#$%^&*()");
            });
        }
        
        @Test
        @DisplayName("无效时间间隔测试")
        void testInvalidInterval() {
            assertThrows(YahooFinanceException.class, () -> {
                client.getIntradayData("AAPL", "invalid_interval");
            });
        }
        
        @Test
        @DisplayName("无效时间范围测试")
        void testInvalidRange() {
            assertThrows(YahooFinanceException.class, () -> {
                client.getHistoricalData("AAPL", "invalid_range", "1d");
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
                YahooFinanceResponse response = client.getQuote("AAPL");
                assertNotNull(response);
                System.out.println("Request " + (i + 1) + ": " + response);
                
                // 添加延迟以避免请求限制
                Thread.sleep(1000);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Total time for 3 requests: " + (endTime - startTime) + "ms");
        }
        
        @Test
        @DisplayName("批量请求测试")
        void testBatchRequests() throws Exception {
            String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"};
            
            long startTime = System.currentTimeMillis();
            
            for (String symbol : symbols) {
                YahooFinanceResponse response = client.getQuote(symbol);
                assertNotNull(response);
                System.out.println(symbol + " Batch Request: " + response);
                
                // 添加延迟以避免请求限制
                Thread.sleep(200);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Total time for batch requests: " + (endTime - startTime) + "ms");
        }
    }
    
    @Nested
    @DisplayName("数据验证测试")
    class DataValidationTests {
        
        @Test
        @DisplayName("响应数据结构验证")
        void testResponseDataStructure() throws Exception {
            YahooFinanceResponse response = client.getQuote("AAPL");
            assertNotNull(response);
            assertNotNull(response.getChart());
            System.out.println("Response Data Structure: " + response);
        }
        
        @Test
        @DisplayName("响应时间测试")
        void testResponseTime() throws Exception {
            long startTime = System.currentTimeMillis();
            YahooFinanceResponse response = client.getQuote("AAPL");
            long endTime = System.currentTimeMillis();
            
            assertNotNull(response);
            long responseTime = endTime - startTime;
            System.out.println("Response time: " + responseTime + "ms");
            
            // 响应时间应该合理（小于30秒）
            assertTrue(responseTime < 30000);
        }
        
        @Test
        @DisplayName("数据完整性测试")
        void testDataIntegrity() throws Exception {
            YahooFinanceResponse response = client.getHistoricalData("AAPL", "1mo", "1d");
            assertNotNull(response);
            
            if (response.getChart() != null && 
                response.getChart().getResult() != null && 
                !response.getChart().getResult().isEmpty()) {
                
                YahooFinanceResponse.Result result = response.getChart().getResult().get(0);
                assertNotNull(result.getMeta());
                assertNotNull(result.getTimestamp());
                assertNotNull(result.getIndicators());
                
                System.out.println("Data Integrity: " + result.getMeta().getSymbol());
            }
        }
    }
    
    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        
        @Test
        @DisplayName("极短时间范围测试")
        void testVeryShortRange() throws Exception {
            YahooFinanceResponse response = client.getHistoricalData("AAPL", "1d", "1m");
            assertNotNull(response);
            System.out.println("Very Short Range: " + response);
        }
        
        @Test
        @DisplayName("极长时间范围测试")
        void testVeryLongRange() throws Exception {
            YahooFinanceResponse response = client.getHistoricalData("AAPL", "max", "1d");
            assertNotNull(response);
            System.out.println("Very Long Range: " + response);
        }
        
        @Test
        @DisplayName("最小时间间隔测试")
        void testMinimalInterval() throws Exception {
            YahooFinanceResponse response = client.getIntradayData("AAPL", "1m");
            assertNotNull(response);
            System.out.println("Minimal Interval: " + response);
        }
        
        @Test
        @DisplayName("最大时间间隔测试")
        void testMaximalInterval() throws Exception {
            YahooFinanceResponse response = client.getIntradayData("AAPL", "1h");
            assertNotNull(response);
            System.out.println("Maximal Interval: " + response);
        }
    }
}
