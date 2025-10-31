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

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.yahoofinance.tools.YahooFinanceTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Yahoo Finance 工具工厂
 * 
 * @author langengine
 */
public class YahooFinanceToolFactory {
    
    private final YahooFinanceConfiguration config;
    
    public YahooFinanceToolFactory() {
        this.config = new YahooFinanceConfiguration();
    }
    
    public YahooFinanceToolFactory(YahooFinanceConfiguration config) {
        this.config = config;
    }
    
    /**
     * 创建 Yahoo Finance 工具
     */
    public YahooFinanceTool createYahooFinanceTool() {
        return new YahooFinanceTool();
    }
    
    /**
     * 获取所有工具
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createYahooFinanceTool());
        return tools;
    }
}
