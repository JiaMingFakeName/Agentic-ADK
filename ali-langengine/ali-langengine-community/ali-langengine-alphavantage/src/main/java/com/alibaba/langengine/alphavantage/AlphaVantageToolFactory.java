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

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.alphavantage.tools.AlphaVantageTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Alpha Vantage 工具工厂
 * 
 * @author langengine
 */
public class AlphaVantageToolFactory {
    
    private final AlphaVantageConfiguration config;
    
    public AlphaVantageToolFactory() {
        this.config = new AlphaVantageConfiguration();
    }
    
    public AlphaVantageToolFactory(AlphaVantageConfiguration config) {
        this.config = config;
    }
    
    /**
     * 创建 Alpha Vantage 工具
     */
    public AlphaVantageTool createAlphaVantageTool() {
        return new AlphaVantageTool();
    }
    
    /**
     * 创建 Alpha Vantage 工具（使用自定义 API Key）
     */
    public AlphaVantageTool createAlphaVantageTool(String apiKey) {
        return new AlphaVantageTool(apiKey);
    }
    
    /**
     * 获取所有工具
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createAlphaVantageTool());
        return tools;
    }
}
