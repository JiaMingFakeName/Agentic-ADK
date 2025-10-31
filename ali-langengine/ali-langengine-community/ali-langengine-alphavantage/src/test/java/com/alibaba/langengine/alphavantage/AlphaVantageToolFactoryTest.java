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
import com.alibaba.langengine.core.tool.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Alpha Vantage 工具工厂测试")
public class AlphaVantageToolFactoryTest {
    
    private AlphaVantageToolFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new AlphaVantageToolFactory();
    }
    
    @Nested
    @DisplayName("工厂初始化测试")
    class FactoryInitializationTests {
        
        @Test
        @DisplayName("默认构造函数测试")
        void testDefaultConstructor() {
            AlphaVantageToolFactory defaultFactory = new AlphaVantageToolFactory();
            assertNotNull(defaultFactory);
        }
        
        @Test
        @DisplayName("自定义配置构造函数测试")
        void testCustomConfigurationConstructor() {
            AlphaVantageConfiguration config = new AlphaVantageConfiguration();
            AlphaVantageToolFactory customFactory = new AlphaVantageToolFactory(config);
            assertNotNull(customFactory);
        }
    }
    
    @Nested
    @DisplayName("工具创建测试")
    class ToolCreationTests {
        
        @Test
        @DisplayName("创建默认工具")
        void testCreateDefaultTool() {
            AlphaVantageTool tool = factory.createAlphaVantageTool();
            assertNotNull(tool);
            assertEquals("AlphaVantageTool", tool.getName());
        }
        
        @Test
        @DisplayName("创建自定义API Key工具")
        void testCreateCustomApiKeyTool() {
            String customApiKey = "test-api-key";
            AlphaVantageTool tool = factory.createAlphaVantageTool(customApiKey);
            assertNotNull(tool);
            assertEquals("AlphaVantageTool", tool.getName());
        }
        
        @Test
        @DisplayName("创建多个工具实例")
        void testCreateMultipleToolInstances() {
            AlphaVantageTool tool1 = factory.createAlphaVantageTool();
            AlphaVantageTool tool2 = factory.createAlphaVantageTool();
            
            assertNotNull(tool1);
            assertNotNull(tool2);
            assertNotSame(tool1, tool2); // 确保是不同的实例
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
            assertTrue(tool instanceof AlphaVantageTool);
        }
        
        @Test
        @DisplayName("工具列表内容验证")
        void testToolListContent() {
            List<BaseTool> tools = factory.getAllTools();
            BaseTool tool = tools.get(0);
            
            assertEquals("AlphaVantageTool", tool.getName());
            assertTrue(tool.getDescription().contains("Alpha Vantage"));
        }
    }
    
    @Nested
    @DisplayName("工具功能测试")
    class ToolFunctionalityTests {
        
        @Test
        @DisplayName("工具基本功能测试")
        void testToolBasicFunctionality() throws Exception {
            AlphaVantageTool tool = factory.createAlphaVantageTool();
            
            // 测试工具名称
            assertEquals("AlphaVantageTool", tool.getName());
            
            // 测试工具描述
            assertNotNull(tool.getDescription());
            assertTrue(tool.getDescription().contains("Alpha Vantage"));
            
            // 测试工具类型
            assertTrue(tool instanceof BaseTool);
        }
        
        @Test
        @DisplayName("工具配置测试")
        void testToolConfiguration() {
            AlphaVantageTool tool = factory.createAlphaVantageTool();
            
            // 验证工具已正确初始化
            assertNotNull(tool.getName());
            assertNotNull(tool.getDescription());
        }
    }
    
    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        
        @Test
        @DisplayName("空API Key测试")
        void testEmptyApiKey() {
            AlphaVantageTool tool = factory.createAlphaVantageTool("");
            assertNotNull(tool);
        }
        
        @Test
        @DisplayName("null API Key测试")
        void testNullApiKey() {
            AlphaVantageTool tool = factory.createAlphaVantageTool(null);
            assertNotNull(tool);
        }
        
        @Test
        @DisplayName("特殊字符API Key测试")
        void testSpecialCharacterApiKey() {
            String specialApiKey = "!@#$%^&*()";
            AlphaVantageTool tool = factory.createAlphaVantageTool(specialApiKey);
            assertNotNull(tool);
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
            AlphaVantageTool[] tools = new AlphaVantageTool[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    tools[index] = factory.createAlphaVantageTool();
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // 验证所有工具都已创建
            for (AlphaVantageTool tool : tools) {
                assertNotNull(tool);
                assertEquals("AlphaVantageTool", tool.getName());
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
    }
}
