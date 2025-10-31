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

import com.alibaba.langengine.yahoofinance.tools.YahooFinanceTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Yahoo Finance 工具测试")
public class YahooFinanceToolTest {
    
    private YahooFinanceTool tool;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        tool = new YahooFinanceTool();
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("实时报价测试")
    class QuoteTests {
        
        @Test
        @DisplayName("获取苹果公司实时报价")
        void testGetAppleQuote() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Apple Quote: " + result);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"})
        @DisplayName("获取多个股票实时报价")
        void testGetMultipleQuotes(String symbol) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", symbol);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(symbol + " Quote: " + result);
        }
        
        @Test
        @DisplayName("无效股票代码测试")
        void testInvalidSymbol() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "INVALID_SYMBOL_12345");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Invalid Symbol Result: " + result);
        }
    }
    
    @Nested
    @DisplayName("历史数据测试")
    class HistoricalDataTests {
        
        @Test
        @DisplayName("获取历史数据")
        void testGetHistoricalData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_historical");
            input.put("symbol", "AAPL");
            input.put("range", "1mo");
            input.put("interval", "1d");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Historical Data: " + result);
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
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_historical");
            input.put("symbol", symbol);
            input.put("range", range);
            input.put("interval", interval);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(symbol + " " + range + " " + interval + " Data: " + result);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1d", "5d", "1mo", "3mo", "6mo", "1y", "2y", "5y", "10y", "ytd", "max"})
        @DisplayName("测试不同时间范围")
        void testDifferentRanges(String range) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_historical");
            input.put("symbol", "AAPL");
            input.put("range", range);
            input.put("interval", "1d");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(range + " Range Data: " + result);
        }
    }
    
    @Nested
    @DisplayName("日线数据测试")
    class DailyDataTests {
        
        @Test
        @DisplayName("获取日线数据")
        void testGetDailyData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_daily");
            input.put("symbol", "AAPL");
            input.put("range", "3mo");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Daily Data: " + result);
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
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_daily");
            input.put("symbol", symbol);
            input.put("range", range);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(symbol + " " + range + " Daily Data: " + result);
        }
    }
    
    @Nested
    @DisplayName("日内数据测试")
    class IntradayDataTests {
        
        @Test
        @DisplayName("获取日内数据")
        void testGetIntradayData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_intraday");
            input.put("symbol", "AAPL");
            input.put("interval", "5m");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Intraday Data: " + result);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1m", "2m", "5m", "15m", "30m", "60m", "90m", "1h"})
        @DisplayName("获取不同时间间隔的日内数据")
        void testGetDifferentIntervalData(String interval) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_intraday");
            input.put("symbol", "AAPL");
            input.put("interval", interval);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(interval + " Intraday Data: " + result);
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
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_intraday");
            input.put("symbol", symbol);
            input.put("interval", interval);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(symbol + " " + interval + " Intraday Data: " + result);
        }
    }
    
    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("缺少必需参数测试")
        void testMissingRequiredParameter() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            // 故意不设置 symbol 参数
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            assertFalse(result.isSuccess());
            System.out.println("Missing Parameter Result: " + result);
        }
        
        @Test
        @DisplayName("无效操作测试")
        void testInvalidAction() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "invalid_action");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            assertFalse(result.isSuccess());
            System.out.println("Invalid Action Result: " + result);
        }
        
        @Test
        @DisplayName("空输入测试")
        void testEmptyInput() throws Exception {
            String toolInput = "{}";
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            assertFalse(result.isSuccess());
            System.out.println("Empty Input Result: " + result);
        }
        
        @Test
        @DisplayName("无效JSON测试")
        void testInvalidJson() throws Exception {
            String toolInput = "invalid json string";
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            assertFalse(result.isSuccess());
            System.out.println("Invalid JSON Result: " + result);
        }
    }
    
    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        
        @Test
        @DisplayName("空字符串参数测试")
        void testEmptyStringParameter() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Empty String Parameter Result: " + result);
        }
        
        @Test
        @DisplayName("null参数测试")
        void testNullParameter() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", null);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Null Parameter Result: " + result);
        }
        
        @Test
        @DisplayName("特殊字符参数测试")
        void testSpecialCharacterParameter() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "!@#$%^&*()");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Special Character Parameter Result: " + result);
        }
        
        @Test
        @DisplayName("无效时间间隔测试")
        void testInvalidInterval() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_intraday");
            input.put("symbol", "AAPL");
            input.put("interval", "invalid_interval");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Invalid Interval Result: " + result);
        }
        
        @Test
        @DisplayName("无效时间范围测试")
        void testInvalidRange() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_historical");
            input.put("symbol", "AAPL");
            input.put("range", "invalid_range");
            input.put("interval", "1d");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Invalid Range Result: " + result);
        }
    }
    
    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {
        
        @Test
        @DisplayName("连续请求测试")
        void testConsecutiveRequests() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 5; i++) {
                ToolExecuteResult result = tool.run(toolInput);
                assertNotNull(result);
                System.out.println("Request " + (i + 1) + ": " + result);
                
                // 添加延迟以避免请求限制
                Thread.sleep(500);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Total time for 5 requests: " + (endTime - startTime) + "ms");
        }
        
        @Test
        @DisplayName("批量请求测试")
        void testBatchRequests() throws Exception {
            String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"};
            
            long startTime = System.currentTimeMillis();
            
            for (String symbol : symbols) {
                Map<String, Object> input = new HashMap<>();
                input.put("action", "get_quote");
                input.put("symbol", symbol);
                
                String toolInput = objectMapper.writeValueAsString(input);
                ToolExecuteResult result = tool.run(toolInput);
                
                assertNotNull(result);
                System.out.println(symbol + " Batch Request: " + result);
                
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
        @DisplayName("数据格式验证")
        void testDataFormatValidation() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            assertNotNull(result.getResult());
            System.out.println("Data Format Validation: " + result);
        }
        
        @Test
        @DisplayName("响应时间测试")
        void testResponseTime() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            
            long startTime = System.currentTimeMillis();
            ToolExecuteResult result = tool.run(toolInput);
            long endTime = System.currentTimeMillis();
            
            assertNotNull(result);
            long responseTime = endTime - startTime;
            System.out.println("Response time: " + responseTime + "ms");
            
            // 响应时间应该合理（小于30秒）
            assertTrue(responseTime < 30000);
        }
    }
}