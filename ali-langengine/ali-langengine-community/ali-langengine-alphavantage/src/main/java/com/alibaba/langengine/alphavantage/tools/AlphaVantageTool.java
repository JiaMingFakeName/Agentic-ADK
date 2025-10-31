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
package com.alibaba.langengine.alphavantage.tools;

import com.alibaba.langengine.alphavantage.AlphaVantageClient;
import com.alibaba.langengine.alphavantage.AlphaVantageConfiguration;
import com.alibaba.langengine.alphavantage.AlphaVantageConstant;
import com.alibaba.langengine.alphavantage.AlphaVantageException;
import com.alibaba.langengine.alphavantage.AlphaVantageResponse;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Alpha Vantage 股票数据工具
 * 提供股票时间序列数据、实时报价、公司信息等功能
 *
 * @author langengine
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class AlphaVantageTool extends DefaultTool {
    
    private AlphaVantageClient alphaVantageClient;
    
    public AlphaVantageTool() {
        init();
    }
    
    public AlphaVantageTool(String apiKey) {
        this.alphaVantageClient = new AlphaVantageClient(AlphaVantageConfiguration.ALPHA_VANTAGE_API_URL, apiKey);
        init();
    }
    
    private void init() {
        setName("AlphaVantageTool");
        setDescription("Alpha Vantage 股票数据工具，提供股票时间序列数据、实时报价、公司信息等功能。支持获取股票的历史价格数据、实时报价、公司基本信息等。");
        
        if (alphaVantageClient == null) {
            this.alphaVantageClient = new AlphaVantageClient();
        }
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
                case "get_intraday":
                    return getIntradayData(inputMap);
                case "get_daily":
                    return getDailyData(inputMap);
                case "get_weekly":
                    return getWeeklyData(inputMap);
                case "get_monthly":
                    return getMonthlyData(inputMap);
                case "get_quote":
                    return getQuote(inputMap);
                case "search_symbol":
                    return searchSymbol(inputMap);
                case "get_overview":
                    return getOverview(inputMap);
                default:
                    return ToolExecuteResult.fail("不支持的操作: " + action);
            }
        } catch (Exception e) {
            log.error("Alpha Vantage 工具执行失败", e);
            return ToolExecuteResult.fail("工具执行失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getIntradayData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            String interval = (String) inputMap.getOrDefault("interval", AlphaVantageConstant.INTERVAL_1MIN);
            String outputSize = (String) inputMap.getOrDefault("outputsize", AlphaVantageConstant.OUTPUT_SIZE_COMPACT);
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            AlphaVantageResponse response = alphaVantageClient.getIntradayTimeSeries(symbol, interval, outputSize);
            return ToolExecuteResult.success("获取日内时间序列数据成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("获取日内数据失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getDailyData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            String outputSize = (String) inputMap.getOrDefault("outputsize", AlphaVantageConstant.OUTPUT_SIZE_COMPACT);
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            AlphaVantageResponse response = alphaVantageClient.getDailyTimeSeries(symbol, outputSize);
            return ToolExecuteResult.success("获取日线时间序列数据成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("获取日线数据失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getWeeklyData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            AlphaVantageResponse response = alphaVantageClient.getWeeklyTimeSeries(symbol);
            return ToolExecuteResult.success("获取周线时间序列数据成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("获取周线数据失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getMonthlyData(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            AlphaVantageResponse response = alphaVantageClient.getMonthlyTimeSeries(symbol);
            return ToolExecuteResult.success("获取月线时间序列数据成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("获取月线数据失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getQuote(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            AlphaVantageResponse response = alphaVantageClient.getGlobalQuote(symbol);
            return ToolExecuteResult.success("获取实时报价成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("获取实时报价失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult searchSymbol(Map<String, Object> inputMap) {
        try {
            String keywords = (String) inputMap.get("keywords");
            
            if (keywords == null) {
                return ToolExecuteResult.fail("缺少必需参数: keywords");
            }
            
            AlphaVantageResponse response = alphaVantageClient.searchSymbol(keywords);
            return ToolExecuteResult.success("搜索股票代码成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("搜索股票代码失败: " + e.getMessage());
        }
    }
    
    private ToolExecuteResult getOverview(Map<String, Object> inputMap) {
        try {
            String symbol = (String) inputMap.get("symbol");
            
            if (symbol == null) {
                return ToolExecuteResult.fail("缺少必需参数: symbol");
            }
            
            AlphaVantageResponse response = alphaVantageClient.getCompanyOverview(symbol);
            return ToolExecuteResult.success("获取公司概览成功", response);
        } catch (AlphaVantageException e) {
            return ToolExecuteResult.fail("获取公司概览失败: " + e.getMessage());
        }
    }
}
