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
package com.alibaba.langengine.integration;

import com.alibaba.langengine.alphavantage.AlphaVantageToolFactory;
import com.alibaba.langengine.yahoofinance.YahooFinanceToolFactory;
import com.alibaba.langengine.core.tool.BaseTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("金融工具集成测试")
public class FinanceToolsIntegrationTest {
    
    private AlphaVantageToolFactory alphaFactory;
    private YahooFinanceToolFactory yahooFactory;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        alphaFactory = new AlphaVantageToolFactory();
        yahooFactory = new YahooFinanceToolFactory();
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("工具工厂集成测试")
    class ToolFactoryIntegrationTests {
        
        @Test
        @DisplayName("Alpha Vantage 工具工厂集成测试")
        void testAlphaVantageToolFactoryIntegration() {
            List<BaseTool> alphaTools = alphaFactory.getAllTools();
            assertNotNull(alphaTools);
            assertEquals(1, alphaTools.size());
            
            BaseTool alphaTool = alphaTools.get(0);
            assertNotNull(alphaTool);
            assertEquals("AlphaVantageTool", alphaTool.getName());
        }
        
        @Test
        @DisplayName("Yahoo Finance 工具工厂集成测试")
        void testYahooFinanceToolFactoryIntegration() {
            List<BaseTool> yahooTools = yahooFactory.getAllTools();
            assertNotNull(yahooTools);
            assertEquals(1, yahooTools.size());
            
            BaseTool yahooTool = yahooTools.get(0);
            assertNotNull(yahooTool);
            assertEquals("YahooFinanceTool", yahooTool.getName());
        }
        
        @Test
        @DisplayName("两个工具工厂共存测试")
        void testBothToolFactoriesCoexistence() {
            List<BaseTool> alphaTools = alphaFactory.getAllTools();
            List<BaseTool> yahooTools = yahooFactory.getAllTools();
            
            assertNotNull(alphaTools);
            assertNotNull(yahooTools);
            assertEquals(1, alphaTools.size());
            assertEquals(1, yahooTools.size());
            
            // 验证两个工具是不同的类型
            assertNotSame(alphaTools.get(0).getClass(), yahooTools.get(0).getClass());
        }
    }
    
    @Nested
    @DisplayName("工具功能集成测试")
    class ToolFunctionIntegrationTests {
        
        @Test
        @DisplayName("Alpha Vantage 工具基本功能测试")
        void testAlphaVantageToolBasicFunction() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            var result = alphaTool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Alpha Vantage Tool Result: " + result);
        }
        
        @Test
        @DisplayName("Yahoo Finance 工具基本功能测试")
        void testYahooFinanceToolBasicFunction() throws Exception {
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            var result = yahooTool.run(toolInput);
            
            assertNotNull(result);
            System.out.println("Yahoo Finance Tool Result: " + result);
        }
        
        @Test
        @DisplayName("两个工具同时使用测试")
        void testBothToolsSimultaneousUsage() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            
            // 同时运行两个工具
            var alphaResult = alphaTool.run(toolInput);
            var yahooResult = yahooTool.run(toolInput);
            
            assertNotNull(alphaResult);
            assertNotNull(yahooResult);
            
            System.out.println("Alpha Vantage Result: " + alphaResult);
            System.out.println("Yahoo Finance Result: " + yahooResult);
        }
    }
    
    @Nested
    @DisplayName("数据一致性测试")
    class DataConsistencyTests {
        
        @Test
        @DisplayName("相同股票代码数据对比测试")
        void testSameSymbolDataComparison() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            String symbol = "AAPL";
            
            // Alpha Vantage 实时报价
            Map<String, Object> alphaInput = new HashMap<>();
            alphaInput.put("action", "get_quote");
            alphaInput.put("symbol", symbol);
            String alphaToolInput = objectMapper.writeValueAsString(alphaInput);
            var alphaResult = alphaTool.run(alphaToolInput);
            
            // Yahoo Finance 实时报价
            Map<String, Object> yahooInput = new HashMap<>();
            yahooInput.put("action", "get_quote");
            yahooInput.put("symbol", symbol);
            String yahooToolInput = objectMapper.writeValueAsString(yahooInput);
            var yahooResult = yahooTool.run(yahooToolInput);
            
            assertNotNull(alphaResult);
            assertNotNull(yahooResult);
            
            System.out.println("Alpha Vantage " + symbol + " Quote: " + alphaResult);
            System.out.println("Yahoo Finance " + symbol + " Quote: " + yahooResult);
        }
        
        @Test
        @DisplayName("历史数据对比测试")
        void testHistoricalDataComparison() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            String symbol = "AAPL";
            
            // Alpha Vantage 日线数据
            Map<String, Object> alphaInput = new HashMap<>();
            alphaInput.put("action", "get_daily");
            alphaInput.put("symbol", symbol);
            alphaInput.put("outputsize", "compact");
            String alphaToolInput = objectMapper.writeValueAsString(alphaInput);
            var alphaResult = alphaTool.run(alphaToolInput);
            
            // Yahoo Finance 日线数据
            Map<String, Object> yahooInput = new HashMap<>();
            yahooInput.put("action", "get_daily");
            yahooInput.put("symbol", symbol);
            yahooInput.put("range", "1mo");
            String yahooToolInput = objectMapper.writeValueAsString(yahooInput);
            var yahooResult = yahooTool.run(yahooToolInput);
            
            assertNotNull(alphaResult);
            assertNotNull(yahooResult);
            
            System.out.println("Alpha Vantage " + symbol + " Daily: " + alphaResult);
            System.out.println("Yahoo Finance " + symbol + " Daily: " + yahooResult);
        }
    }
    
    @Nested
    @DisplayName("性能集成测试")
    class PerformanceIntegrationTests {
        
        @Test
        @DisplayName("两个工具性能对比测试")
        void testBothToolsPerformanceComparison() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            
            String toolInput = objectMapper.writeValueAsString(input);
            
            // Alpha Vantage 性能测试
            long alphaStartTime = System.currentTimeMillis();
            var alphaResult = alphaTool.run(toolInput);
            long alphaEndTime = System.currentTimeMillis();
            long alphaResponseTime = alphaEndTime - alphaStartTime;
            
            // Yahoo Finance 性能测试
            long yahooStartTime = System.currentTimeMillis();
            var yahooResult = yahooTool.run(toolInput);
            long yahooEndTime = System.currentTimeMillis();
            long yahooResponseTime = yahooEndTime - yahooStartTime;
            
            assertNotNull(alphaResult);
            assertNotNull(yahooResult);
            
            System.out.println("Alpha Vantage Response Time: " + alphaResponseTime + "ms");
            System.out.println("Yahoo Finance Response Time: " + yahooResponseTime + "ms");
            System.out.println("Alpha Vantage Result: " + alphaResult);
            System.out.println("Yahoo Finance Result: " + yahooResult);
        }
        
        @Test
        @DisplayName("批量请求性能测试")
        void testBatchRequestPerformance() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            String[] symbols = {"AAPL", "MSFT", "GOOGL"};
            
            long startTime = System.currentTimeMillis();
            
            for (String symbol : symbols) {
                Map<String, Object> input = new HashMap<>();
                input.put("action", "get_quote");
                input.put("symbol", symbol);
                
                String toolInput = objectMapper.writeValueAsString(input);
                
                var alphaResult = alphaTool.run(toolInput);
                var yahooResult = yahooTool.run(toolInput);
                
                assertNotNull(alphaResult);
                assertNotNull(yahooResult);
                
                System.out.println(symbol + " - Alpha: " + alphaResult);
                System.out.println(symbol + " - Yahoo: " + yahooResult);
                
                // 添加延迟以避免API限制
                Thread.sleep(1000);
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Total batch request time: " + (endTime - startTime) + "ms");
        }
    }
    
    @Nested
    @DisplayName("错误处理集成测试")
    class ErrorHandlingIntegrationTests {
        
        @Test
        @DisplayName("两个工具错误处理对比测试")
        void testBothToolsErrorHandlingComparison() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "INVALID_SYMBOL_12345");
            
            String toolInput = objectMapper.writeValueAsString(input);
            
            var alphaResult = alphaTool.run(toolInput);
            var yahooResult = yahooTool.run(toolInput);
            
            assertNotNull(alphaResult);
            assertNotNull(yahooResult);
            
            System.out.println("Alpha Vantage Error Result: " + alphaResult);
            System.out.println("Yahoo Finance Error Result: " + yahooResult);
        }
        
        @Test
        @DisplayName("网络异常处理测试")
        void testNetworkExceptionHandling() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            // 测试无效的输入
            String invalidInput = "invalid json";
            
            var alphaResult = alphaTool.run(invalidInput);
            var yahooResult = yahooTool.run(invalidInput);
            
            assertNotNull(alphaResult);
            assertNotNull(yahooResult);
            
            System.out.println("Alpha Vantage Network Error: " + alphaResult);
            System.out.println("Yahoo Finance Network Error: " + yahooResult);
        }
    }
    
    @Nested
    @DisplayName("功能覆盖测试")
    class FeatureCoverageTests {
        
        @Test
        @DisplayName("Alpha Vantage 功能覆盖测试")
        void testAlphaVantageFeatureCoverage() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            
            String[] actions = {"get_quote", "get_daily", "get_weekly", "get_monthly", "search_symbol", "get_overview"};
            
            for (String action : actions) {
                Map<String, Object> input = new HashMap<>();
                input.put("action", action);
                
                if (!action.equals("search_symbol")) {
                    input.put("symbol", "AAPL");
                } else {
                    input.put("keywords", "Apple");
                }
                
                String toolInput = objectMapper.writeValueAsString(input);
                var result = alphaTool.run(toolInput);
                
                assertNotNull(result);
                System.out.println("Alpha Vantage " + action + ": " + result);
                
                // 添加延迟以避免API限制
                Thread.sleep(2000);
            }
        }
        
        @Test
        @DisplayName("Yahoo Finance 功能覆盖测试")
        void testYahooFinanceFeatureCoverage() throws Exception {
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            String[] actions = {"get_quote", "get_historical", "get_daily", "get_intraday"};
            
            for (String action : actions) {
                Map<String, Object> input = new HashMap<>();
                input.put("action", action);
                input.put("symbol", "AAPL");
                
                if (action.equals("get_historical")) {
                    input.put("range", "1mo");
                    input.put("interval", "1d");
                } else if (action.equals("get_intraday")) {
                    input.put("interval", "5m");
                } else if (action.equals("get_daily")) {
                    input.put("range", "3mo");
                }
                
                String toolInput = objectMapper.writeValueAsString(input);
                var result = yahooTool.run(toolInput);
                
                assertNotNull(result);
                System.out.println("Yahoo Finance " + action + ": " + result);
                
                // 添加延迟以避免请求限制
                Thread.sleep(500);
            }
        }
    }
}
