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
package com.alibaba.langengine.examples;

import com.alibaba.langengine.alphavantage.AlphaVantageToolFactory;
import com.alibaba.langengine.yahoofinance.YahooFinanceToolFactory;
import com.alibaba.langengine.core.tool.BaseTool;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 金融工具使用示例
 * 
 * @author langengine
 */
public class FinanceToolsExample {
    
    public static void main(String[] args) {
        try {
            // 创建工具工厂
            AlphaVantageToolFactory alphaFactory = new AlphaVantageToolFactory();
            YahooFinanceToolFactory yahooFactory = new YahooFinanceToolFactory();
            
            // 获取工具实例
            List<BaseTool> alphaTools = alphaFactory.getAllTools();
            List<BaseTool> yahooTools = yahooFactory.getAllTools();
            
            ObjectMapper objectMapper = new ObjectMapper();
            
            // Alpha Vantage 示例
            System.out.println("=== Alpha Vantage 工具示例 ===");
            BaseTool alphaTool = alphaTools.get(0);
            
            // 获取实时报价
            Map<String, Object> input = new HashMap<>();
            input.put("action", "get_quote");
            input.put("symbol", "AAPL");
            String toolInput = objectMapper.writeValueAsString(input);
            var result = alphaTool.run(toolInput);
            System.out.println("Alpha Vantage 实时报价: " + result);
            
            // Yahoo Finance 示例
            System.out.println("\n=== Yahoo Finance 工具示例 ===");
            BaseTool yahooTool = yahooTools.get(0);
            
            // 获取历史数据
            input.clear();
            input.put("action", "get_historical");
            input.put("symbol", "AAPL");
            input.put("range", "1mo");
            input.put("interval", "1d");
            toolInput = objectMapper.writeValueAsString(input);
            result = yahooTool.run(toolInput);
            System.out.println("Yahoo Finance 历史数据: " + result);
            
        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
