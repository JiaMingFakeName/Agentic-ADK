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
package com.alibaba.langengine.stress;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("金融工具压力测试")
public class FinanceToolsStressTest {
    
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
    @DisplayName("Alpha Vantage 压力测试")
    class AlphaVantageStressTests {
        
        @Test
        @DisplayName("高并发请求测试")
        void testHighConcurrencyRequests() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            int threadCount = 10;
            int requestsPerThread = 5;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            Map<String, Object> input = new HashMap<>();
                            input.put("action", "get_quote");
                            input.put("symbol", "AAPL");
                            
                            String toolInput = objectMapper.writeValueAsString(input);
                            var result = alphaTool.run(toolInput);
                            
                            if (result != null) {
                                successCount.incrementAndGet();
                            } else {
                                failureCount.incrementAndGet();
                            }
                            
                            // 添加延迟以避免API限制
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Thread error: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(5, TimeUnit.MINUTES);
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Alpha Vantage Stress Test Results:");
            System.out.println("Total requests: " + (threadCount * requestsPerThread));
            System.out.println("Successful requests: " + successCount.get());
            System.out.println("Failed requests: " + failureCount.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average time per request: " + (totalTime / (double) (threadCount * requestsPerThread)) + "ms");
            
            // 验证至少有一些请求成功
            assertTrue(successCount.get() > 0);
        }
        
        @Test
        @DisplayName("长时间运行测试")
        void testLongRunningRequests() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            int durationMinutes = 2;
            long endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
            AtomicInteger requestCount = new AtomicInteger(0);
            
            System.out.println("Starting long running test for " + durationMinutes + " minutes...");
            
            while (System.currentTimeMillis() < endTime) {
                Map<String, Object> input = new HashMap<>();
                input.put("action", "get_quote");
                input.put("symbol", "AAPL");
                
                String toolInput = objectMapper.writeValueAsString(input);
                var result = alphaTool.run(toolInput);
                
                requestCount.incrementAndGet();
                
                if (result != null) {
                    System.out.println("Request " + requestCount.get() + " completed");
                } else {
                    System.out.println("Request " + requestCount.get() + " failed");
                }
                
                // 添加延迟以避免API限制
                Thread.sleep(2000);
            }
            
            System.out.println("Long running test completed. Total requests: " + requestCount.get());
            assertTrue(requestCount.get() > 0);
        }
        
        @Test
        @DisplayName("内存使用压力测试")
        void testMemoryUsageStress() throws Exception {
            int iterations = 100;
            BaseTool[] tools = new BaseTool[iterations];
            
            System.out.println("Creating " + iterations + " tool instances...");
            
            for (int i = 0; i < iterations; i++) {
                tools[i] = alphaFactory.createAlphaVantageTool();
                assertNotNull(tools[i]);
                
                if (i % 10 == 0) {
                    System.out.println("Created " + (i + 1) + " tools");
                }
            }
            
            System.out.println("All tools created successfully");
            
            // 验证所有工具都能正常工作
            for (int i = 0; i < iterations; i++) {
                assertEquals("AlphaVantageTool", tools[i].getName());
            }
            
            System.out.println("Memory stress test completed");
        }
    }
    
    @Nested
    @DisplayName("Yahoo Finance 压力测试")
    class YahooFinanceStressTests {
        
        @Test
        @DisplayName("高并发请求测试")
        void testHighConcurrencyRequests() throws Exception {
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            int threadCount = 20;
            int requestsPerThread = 10;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            Map<String, Object> input = new HashMap<>();
                            input.put("action", "get_quote");
                            input.put("symbol", "AAPL");
                            
                            String toolInput = objectMapper.writeValueAsString(input);
                            var result = yahooTool.run(toolInput);
                            
                            if (result != null) {
                                successCount.incrementAndGet();
                            } else {
                                failureCount.incrementAndGet();
                            }
                            
                            // 添加延迟以避免请求限制
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Thread error: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(5, TimeUnit.MINUTES);
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Yahoo Finance Stress Test Results:");
            System.out.println("Total requests: " + (threadCount * requestsPerThread));
            System.out.println("Successful requests: " + successCount.get());
            System.out.println("Failed requests: " + failureCount.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average time per request: " + (totalTime / (double) (threadCount * requestsPerThread)) + "ms");
            
            // 验证至少有一些请求成功
            assertTrue(successCount.get() > 0);
        }
        
        @Test
        @DisplayName("批量请求压力测试")
        void testBatchRequestStress() throws Exception {
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "NFLX", "ADBE", "CRM"};
            int batchSize = 50;
            
            long startTime = System.currentTimeMillis();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            for (int batch = 0; batch < batchSize; batch++) {
                for (String symbol : symbols) {
                    try {
                        Map<String, Object> input = new HashMap<>();
                        input.put("action", "get_quote");
                        input.put("symbol", symbol);
                        
                        String toolInput = objectMapper.writeValueAsString(input);
                        var result = yahooTool.run(toolInput);
                        
                        if (result != null) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                        
                        // 添加延迟以避免请求限制
                        Thread.sleep(50);
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Request error for " + symbol + ": " + e.getMessage());
                    }
                }
                
                if (batch % 10 == 0) {
                    System.out.println("Completed batch " + batch + "/" + batchSize);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            int totalRequests = batchSize * symbols.length;
            
            System.out.println("Yahoo Finance Batch Stress Test Results:");
            System.out.println("Total requests: " + totalRequests);
            System.out.println("Successful requests: " + successCount.get());
            System.out.println("Failed requests: " + failureCount.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average time per request: " + (totalTime / (double) totalRequests) + "ms");
            
            // 验证至少有一些请求成功
            assertTrue(successCount.get() > 0);
        }
        
        @Test
        @DisplayName("内存使用压力测试")
        void testMemoryUsageStress() throws Exception {
            int iterations = 200;
            BaseTool[] tools = new BaseTool[iterations];
            
            System.out.println("Creating " + iterations + " Yahoo Finance tool instances...");
            
            for (int i = 0; i < iterations; i++) {
                tools[i] = yahooFactory.createYahooFinanceTool();
                assertNotNull(tools[i]);
                
                if (i % 20 == 0) {
                    System.out.println("Created " + (i + 1) + " tools");
                }
            }
            
            System.out.println("All Yahoo Finance tools created successfully");
            
            // 验证所有工具都能正常工作
            for (int i = 0; i < iterations; i++) {
                assertEquals("YahooFinanceTool", tools[i].getName());
            }
            
            System.out.println("Yahoo Finance memory stress test completed");
        }
    }
    
    @Nested
    @DisplayName("混合压力测试")
    class MixedStressTests {
        
        @Test
        @DisplayName("两个工具混合压力测试")
        void testMixedToolsStress() throws Exception {
            BaseTool alphaTool = alphaFactory.getAllTools().get(0);
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            
            int threadCount = 15;
            int requestsPerThread = 5;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger alphaSuccessCount = new AtomicInteger(0);
            AtomicInteger yahooSuccessCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                final boolean useAlphaVantage = (i % 2 == 0);
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            Map<String, Object> input = new HashMap<>();
                            input.put("action", "get_quote");
                            input.put("symbol", "AAPL");
                            
                            String toolInput = objectMapper.writeValueAsString(input);
                            var result = useAlphaVantage ? 
                                alphaTool.run(toolInput) : 
                                yahooTool.run(toolInput);
                            
                            if (result != null) {
                                if (useAlphaVantage) {
                                    alphaSuccessCount.incrementAndGet();
                                } else {
                                    yahooSuccessCount.incrementAndGet();
                                }
                            } else {
                                failureCount.incrementAndGet();
                            }
                            
                            // 添加延迟以避免API限制
                            Thread.sleep(useAlphaVantage ? 1000 : 200);
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Thread error: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(5, TimeUnit.MINUTES);
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Mixed Tools Stress Test Results:");
            System.out.println("Total requests: " + (threadCount * requestsPerThread));
            System.out.println("Alpha Vantage successful requests: " + alphaSuccessCount.get());
            System.out.println("Yahoo Finance successful requests: " + yahooSuccessCount.get());
            System.out.println("Failed requests: " + failureCount.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average time per request: " + (totalTime / (double) (threadCount * requestsPerThread)) + "ms");
            
            // 验证至少有一些请求成功
            assertTrue(alphaSuccessCount.get() > 0 || yahooSuccessCount.get() > 0);
        }
        
        @Test
        @DisplayName("工具工厂压力测试")
        void testToolFactoryStress() throws Exception {
            int iterations = 1000;
            
            System.out.println("Testing tool factory creation stress...");
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < iterations; i++) {
                AlphaVantageToolFactory alphaFactory = new AlphaVantageToolFactory();
                YahooFinanceToolFactory yahooFactory = new YahooFinanceToolFactory();
                
                List<BaseTool> alphaTools = alphaFactory.getAllTools();
                List<BaseTool> yahooTools = yahooFactory.getAllTools();
                
                assertNotNull(alphaTools);
                assertNotNull(yahooTools);
                assertEquals(1, alphaTools.size());
                assertEquals(1, yahooTools.size());
                
                if (i % 100 == 0) {
                    System.out.println("Created " + (i + 1) + " tool factories");
                }
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Tool Factory Stress Test Results:");
            System.out.println("Total iterations: " + iterations);
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average time per iteration: " + (totalTime / (double) iterations) + "ms");
        }
    }
    
    @Nested
    @DisplayName("极限测试")
    class ExtremeTests {
        
        @Test
        @DisplayName("极限并发测试")
        void testExtremeConcurrency() throws Exception {
            BaseTool yahooTool = yahooFactory.getAllTools().get(0);
            int threadCount = 50;
            int requestsPerThread = 3;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            Map<String, Object> input = new HashMap<>();
                            input.put("action", "get_quote");
                            input.put("symbol", "AAPL");
                            
                            String toolInput = objectMapper.writeValueAsString(input);
                            var result = yahooTool.run(toolInput);
                            
                            if (result != null) {
                                successCount.incrementAndGet();
                            } else {
                                failureCount.incrementAndGet();
                            }
                            
                            // 最小延迟
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(10, TimeUnit.MINUTES);
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Extreme Concurrency Test Results:");
            System.out.println("Total requests: " + (threadCount * requestsPerThread));
            System.out.println("Successful requests: " + successCount.get());
            System.out.println("Failed requests: " + failureCount.get());
            System.out.println("Total time: " + totalTime + "ms");
            
            // 在极限测试中，我们只要求有一些请求成功
            assertTrue(successCount.get() > 0);
        }
        
        @Test
        @DisplayName("极限内存测试")
        void testExtremeMemoryUsage() throws Exception {
            int iterations = 500;
            BaseTool[] tools = new BaseTool[iterations];
            
            System.out.println("Creating " + iterations + " tool instances for extreme memory test...");
            
            for (int i = 0; i < iterations; i++) {
                if (i % 2 == 0) {
                    tools[i] = alphaFactory.createAlphaVantageTool();
                } else {
                    tools[i] = yahooFactory.createYahooFinanceTool();
                }
                assertNotNull(tools[i]);
                
                if (i % 50 == 0) {
                    System.out.println("Created " + (i + 1) + " tools");
                }
            }
            
            System.out.println("All tools created successfully");
            
            // 验证所有工具都能正常工作
            for (int i = 0; i < iterations; i++) {
                assertNotNull(tools[i].getName());
                assertNotNull(tools[i].getDescription());
            }
            
            System.out.println("Extreme memory test completed");
        }
    }
}
