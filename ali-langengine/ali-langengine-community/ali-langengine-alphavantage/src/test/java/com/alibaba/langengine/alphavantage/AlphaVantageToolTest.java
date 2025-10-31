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

import com.alibaba.langengine.alphavantage.tools.AlphaVantageTool;
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

@DisplayName("Alpha Vantage 工具测试")
public class AlphaVantageToolTest {
    
    private AlphaVantageTool tool;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        tool = new AlphaVantageTool();
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
        @DisplayName("获取日线数据")
        void testGetDailyData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_daily");
            input.put("symbol", "AAPL");
            input.put("outputsize", "compact");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Daily Data: " + result);
        }
        
        @Test
        @DisplayName("获取完整日线数据")
        void testGetFullDailyData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_daily");
            input.put("symbol", "AAPL");
            input.put("outputsize", "full");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Full Daily Data: " + result);
        }
        
        @Test
        @DisplayName("获取周线数据")
        void testGetWeeklyData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_weekly");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Weekly Data: " + result);
        }
        
        @Test
        @DisplayName("获取月线数据")
        void testGetMonthlyData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_monthly");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Monthly Data: " + result);
        }
        
        @ParameterizedTest
        @CsvSource({
            "AAPL, compact",
            "MSFT, full",
            "GOOGL, compact",
            "AMZN, full"
        })
        @DisplayName("参数化测试不同股票和输出大小")
        void testParameterizedDailyData(String symbol, String outputSize) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_daily");
            input.put("symbol", symbol);
            input.put("outputsize", outputSize);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(symbol + " " + outputSize + " Data: " + result);
        }
    }
    
    @Nested
    @DisplayName("日内数据测试")
    class IntradayDataTests {
        
        @Test
        @DisplayName("获取1分钟日内数据")
        void testGet1MinIntradayData() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_intraday");
            input.put("symbol", "AAPL");
            input.put("interval", "1min");
            input.put("outputsize", "compact");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("1min Intraday Data: " + result);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1min", "5min", "15min", "30min", "60min"})
        @DisplayName("获取不同时间间隔的日内数据")
        void testGetDifferentIntervalData(String interval) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_intraday");
            input.put("symbol", "AAPL");
            input.put("interval", interval);
            input.put("outputsize", "compact");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(interval + " Intraday Data: " + result);
        }
    }
    
    @Nested
    @DisplayName("搜索功能测试")
    class SearchTests {
        
        @Test
        @DisplayName("搜索苹果公司")
        void testSearchApple() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "search_symbol");
            input.put("keywords", "Apple");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Apple Search: " + result);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"Microsoft", "Google", "Amazon", "Tesla", "Meta"})
        @DisplayName("搜索多个公司")
        void testSearchMultipleCompanies(String company) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "search_symbol");
            input.put("keywords", company);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(company + " Search: " + result);
        }
        
        @Test
        @DisplayName("搜索无效关键词")
        void testSearchInvalidKeyword() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "search_symbol");
            input.put("keywords", "NonExistentCompany12345");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Invalid Search: " + result);
        }
    }
    
    @Nested
    @DisplayName("公司信息测试")
    class CompanyInfoTests {
        
        @Test
        @DisplayName("获取苹果公司概览")
        void testGetAppleOverview() throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_overview");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Apple Overview: " + result);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"})
        @DisplayName("获取多个公司概览")
        void testGetMultipleCompanyOverviews(String symbol) throws Exception {
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_overview");
            input.put("symbol", symbol);
            
            String toolInput = objectMapper.writeValueAsString(input);
            ToolExecuteResult result = tool.run(toolInput);
            
            assertNotNull(result);
            System.out.println(symbol + " Overview: " + result);
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
                
                // 添加延迟以避免API限制
                Thread.sleep(1000);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Total time for 5 requests: " + (endTime - startTime) + "ms");
        }
    }
}