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
import com.alibaba.langengine.core.tool.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Yahoo Finance 工具工厂测试")
public class YahooFinanceToolFactoryTest {
    
    private YahooFinanceToolFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new YahooFinanceToolFactory();
    }
    
    @Nested
    @DisplayName("工厂初始化测试")
    class FactoryInitializationTests {
        
        @Test
        @DisplayName("默认构造函数测试")
        void testDefaultConstructor() {
            YahooFinanceToolFactory defaultFactory = new YahooFinanceToolFactory();
            assertNotNull(defaultFactory);
        }
        
        @Test
        @DisplayName("自定义配置构造函数测试")
        void testCustomConfigurationConstructor() {
            YahooFinanceConfiguration config = new YahooFinanceConfiguration();
            YahooFinanceToolFactory customFactory = new YahooFinanceToolFactory(config);
            assertNotNull(customFactory);
        }
    }
    
    @Nested
    @DisplayName("工具创建测试")
    class ToolCreationTests {
        
        @Test
        @DisplayName("创建默认工具")
        void testCreateDefaultTool() {
            YahooFinanceTool tool = factory.createYahooFinanceTool();
            assertNotNull(tool);
            assertEquals("YahooFinanceTool", tool.getName());
        }
        
        @Test
        @DisplayName("创建多个工具实例")
        void testCreateMultipleToolInstances() {
            YahooFinanceTool tool1 = factory.createYahooFinanceTool();
            YahooFinanceTool tool2 = factory.createYahooFinanceTool();
            
            assertNotNull(tool1);
            assertNotNull(tool2);
            assertNotSame(tool1, tool2); // 确保是不同的实例
        }
        
        @Test
        @DisplayName("工具实例独立性测试")
        void testToolInstanceIndependence() {
            YahooFinanceTool tool1 = factory.createYahooFinanceTool();
            YahooFinanceTool tool2 = factory.createYahooFinanceTool();
            
            // 验证两个工具实例是独立的
            assertNotSame(tool1, tool2);
            assertEquals(tool1.getName(), tool2.getName());
            assertEquals(tool1.getDescription(), tool2.getDescription());
        }
    }
    
    @Nested
    @DisplayName("工具列表测试")
    class ToolListTests {
        
        @Test
        @DisplayName("获取所有工具")
        void testGetAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            assertNotNull(tools);
            assertFalse(tools.isEmpty());
            assertEquals(1, tools.size());
            
            BaseTool tool = tools.get(0);
            assertNotNull(tool);
            assertTrue(tool instanceof YahooFinanceTool);
        }
        
        @Test
        @DisplayName("工具列表内容验证")
        void testToolListContent() {
            List<BaseTool> tools = factory.getAllTools();
            BaseTool tool = tools.get(0);
            
            assertEquals("YahooFinanceTool", tool.getName());
            assertTrue(tool.getDescription().contains("Yahoo Finance"));
        }
        
        @Test
        @DisplayName("工具列表不可变性测试")
        void testToolListImmutability() {
            List<BaseTool> tools1 = factory.getAllTools();
            List<BaseTool> tools2 = factory.getAllTools();
            
            // 验证每次调用都返回新的列表
            assertNotSame(tools1, tools2);
            assertEquals(tools1.size(), tools2.size());
        }
    }
    
    @Nested
    @DisplayName("工具功能测试")
    class ToolFunctionalityTests {
        
        @Test
        @DisplayName("工具基本功能测试")
        void testToolBasicFunctionality() throws Exception {
            YahooFinanceTool tool = factory.createYahooFinanceTool();
            
            // 测试工具名称
            assertEquals("YahooFinanceTool", tool.getName());
            
            // 测试工具描述
            assertNotNull(tool.getDescription());
            assertTrue(tool.getDescription().contains("Yahoo Finance"));
            
            // 测试工具类型
            assertTrue(tool instanceof BaseTool);
        }
        
        @Test
        @DisplayName("工具配置测试")
        void testToolConfiguration() {
            YahooFinanceTool tool = factory.createYahooFinanceTool();
            
            // 验证工具已正确初始化
            assertNotNull(tool.getName());
            assertNotNull(tool.getDescription());
        }
        
        @Test
        @DisplayName("工具描述内容测试")
        void testToolDescriptionContent() {
            YahooFinanceTool tool = factory.createYahooFinanceTool();
            String description = tool.getDescription();
            
            assertTrue(description.contains("Yahoo Finance"));
            assertTrue(description.contains("股票"));
            assertTrue(description.contains("历史数据"));
            assertTrue(description.contains("实时报价"));
        }
    }
    
    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        
        @Test
        @DisplayName("多次创建工具测试")
        void testMultipleToolCreation() {
            for (int i = 0; i < 10; i++) {
                YahooFinanceTool tool = factory.createYahooFinanceTool();
                assertNotNull(tool);
                assertEquals("YahooFinanceTool", tool.getName());
            }
        }
        
        @Test
        @DisplayName("多次获取工具列表测试")
        void testMultipleGetAllTools() {
            for (int i = 0; i < 5; i++) {
                List<BaseTool> tools = factory.getAllTools();
                assertNotNull(tools);
                assertEquals(1, tools.size());
                assertTrue(tools.get(0) instanceof YahooFinanceTool);
            }
        }
    }
    
    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {
        
        @Test
        @DisplayName("并发创建工具测试")
        void testConcurrentToolCreation() throws InterruptedException {
            int threadCount = 5;
            Thread[] threads = new Thread[threadCount];
            YahooFinanceTool[] tools = new YahooFinanceTool[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    tools[index] = factory.createYahooFinanceTool();
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // 验证所有工具都已创建
            for (YahooFinanceTool tool : tools) {
                assertNotNull(tool);
                assertEquals("YahooFinanceTool", tool.getName());
            }
        }
        
        @Test
        @DisplayName("并发获取工具列表测试")
        void testConcurrentGetAllTools() throws InterruptedException {
            int threadCount = 3;
            Thread[] threads = new Thread[threadCount];
            List<BaseTool>[] toolLists = new List[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    toolLists[index] = factory.getAllTools();
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // 验证所有工具列表都已获取
            for (List<BaseTool> toolList : toolLists) {
                assertNotNull(toolList);
                assertFalse(toolList.isEmpty());
                assertEquals(1, toolList.size());
            }
        }
        
        @Test
        @DisplayName("混合并发操作测试")
        void testMixedConcurrentOperations() throws InterruptedException {
            int threadCount = 6;
            Thread[] threads = new Thread[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    if (index % 2 == 0) {
                        // 偶数线程创建工具
                        YahooFinanceTool tool = factory.createYahooFinanceTool();
                        assertNotNull(tool);
                    } else {
                        // 奇数线程获取工具列表
                        List<BaseTool> tools = factory.getAllTools();
                        assertNotNull(tools);
                        assertEquals(1, tools.size());
                    }
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
        }
    }
    
    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {
        
        @Test
        @DisplayName("工具创建性能测试")
        void testToolCreationPerformance() {
            int iterations = 100;
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < iterations; i++) {
                YahooFinanceTool tool = factory.createYahooFinanceTool();
                assertNotNull(tool);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Created " + iterations + " tools in " + totalTime + "ms");
            System.out.println("Average time per tool: " + (totalTime / (double) iterations) + "ms");
            
            // 每个工具创建时间应该小于100ms
            assertTrue(totalTime / (double) iterations < 100);
        }
        
        @Test
        @DisplayName("工具列表获取性能测试")
        void testGetAllToolsPerformance() {
            int iterations = 1000;
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < iterations; i++) {
                List<BaseTool> tools = factory.getAllTools();
                assertNotNull(tools);
                assertEquals(1, tools.size());
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            System.out.println("Called getAllTools " + iterations + " times in " + totalTime + "ms");
            System.out.println("Average time per call: " + (totalTime / (double) iterations) + "ms");
            
            // 每次调用时间应该小于10ms
            assertTrue(totalTime / (double) iterations < 10);
        }
    }
    
    @Nested
    @DisplayName("内存测试")
    class MemoryTests {
        
        @Test
        @DisplayName("工具实例内存使用测试")
        void testToolInstanceMemoryUsage() {
            // 创建大量工具实例
            YahooFinanceTool[] tools = new YahooFinanceTool[1000];
            
            for (int i = 0; i < 1000; i++) {
                tools[i] = factory.createYahooFinanceTool();
                assertNotNull(tools[i]);
            }
            
            // 验证所有工具都能正常工作
            for (YahooFinanceTool tool : tools) {
                assertEquals("YahooFinanceTool", tool.getName());
                assertNotNull(tool.getDescription());
            }
            
            System.out.println("Successfully created 1000 tool instances");
        }
        
        @Test
        @DisplayName("工具列表内存使用测试")
        void testToolListMemoryUsage() {
            // 创建大量工具列表
            List<BaseTool>[] toolLists = new List[1000];
            
            for (int i = 0; i < 1000; i++) {
                toolLists[i] = factory.getAllTools();
                assertNotNull(toolLists[i]);
                assertEquals(1, toolLists[i].size());
            }
            
            // 验证所有工具列表都能正常工作
            for (List<BaseTool> toolList : toolLists) {
                assertFalse(toolList.isEmpty());
                assertTrue(toolList.get(0) instanceof YahooFinanceTool);
            }
            
            System.out.println("Successfully created 1000 tool lists");
        }
    }
}
