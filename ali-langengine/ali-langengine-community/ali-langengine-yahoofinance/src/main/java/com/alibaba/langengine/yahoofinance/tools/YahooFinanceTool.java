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
package com.alibaba.langengine.yahoofinance.tools;

import com.alibaba.langengine.yahoofinance.YahooFinanceClient;
import com.alibaba.langengine.yahoofinance.YahooFinanceConfiguration;
import com.alibaba.langengine.yahoofinance.YahooFinanceConstant;
import com.alibaba.langengine.yahoofinance.YahooFinanceException;
import com.alibaba.langengine.yahoofinance.YahooFinanceResponse;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Yahoo Finance 股票数据工具
 * 提供股票历史数据、实时报价等功能
 *
 * @author langengine
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class YahooFinanceTool extends DefaultTool {
    
    private YahooFinanceClient yahooFinanceClient;
    
    public YahooFinanceTool() {
        init();
    }
    
    private void init() {
        setName("YahooFinanceTool");
        setDescription("Yahoo Finance 股票数据工具，提供股票历史数据、实时报价等功能。支持获取股票的历史价格数据、实时报价、技术指标等。");
        
        this.yahooFinanceClient = new YahooFinanceClient();
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> inputMap = objectMapper.readValue(toolInput, Map.class);
            
            String action = (String) inputMap.get("action");
            if (action == null) {
                return ToolExecuteResult.fail("缺少必需参数: action");
            }
            
            switch (action) {
                case "get_quote":
                    return getQuote(inputMap);
                case "get_historical":
                    return getHistoricalData(inputMap);
                case "get_daily":
                    return getDailyData(inputMap);
                case "get_intraday":
                    return getIntradayData(inputMap);
                default:
                    return ToolExecuteResult.fail("不支持的操作: " + action);
            }
        } catch (Exception e) {
            log.error("Yahoo Finance 工具执行失败", e);
            return ToolExecuteResult.fail("工具执行失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getQuote(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            YahooFinanceResponse response = yahooFinanceClient.getQuote(symbol);
            return ToolExecuteResult.success("获取实时报价成功", response);
        } catch (YahooFinanceException e) {
            return ToolExecuteResult.fail("获取实时报价失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getHistoricalData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            String range = (String) inputMap.getOrDefault("range", YahooFinanceConstant.RANGE_1MONTH);
            String interval = (String) inputMap.getOrDefault("interval", YahooFinanceConstant.INTERVAL_1DAY);
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            YahooFinanceResponse response = yahooFinanceClient.getHistoricalData(symbol, range, interval);
            return ToolExecuteResult.success("获取历史数据成功", response);
        } catch (YahooFinanceException e) {
            return ToolExecuteResult.fail("获取历史数据失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getDailyData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            String range = (String) inputMap.getOrDefault("range", YahooFinanceConstant.RANGE_1MONTH);
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            YahooFinanceResponse response = yahooFinanceClient.getDailyData(symbol, range);
            return ToolExecuteResult.success("获取日线数据成功", response);
        } catch (YahooFinanceException e) {
            return ToolExecuteResult.fail("获取日线数据失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getIntradayData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            String interval = (String) inputMap.getOrDefault("interval", YahooFinanceConstant.INTERVAL_1MIN);
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            YahooFinanceResponse response = yahooFinanceClient.getIntradayData(symbol, interval);
            return ToolExecuteResult.success("获取日内数据成功", response);
        } catch (YahooFinanceException e) {
            return ToolExecuteResult.fail("获取日内数据失败: " + e.getMessage());
        }
    }
}
