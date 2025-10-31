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

package com.alibaba.langengine.googlemaps;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.googlemaps.tools.GoogleMapsDirectionsTool;
import com.alibaba.langengine.googlemaps.tools.GoogleMapsGeocodingTool;
import com.alibaba.langengine.googlemaps.tools.GoogleMapsPlacesTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Google Maps 工具工厂
 */
@Slf4j
public class GoogleMapsToolFactory {

    private final GoogleMapsConfiguration config;

    public GoogleMapsToolFactory() {
        this.config = new GoogleMapsConfiguration();
    }

    public GoogleMapsToolFactory(GoogleMapsConfiguration config) {
        this.config = config;
    }

    /**
     * 创建地理编码工具
     */
    public GoogleMapsGeocodingTool getGeocodingTool() {
        return new GoogleMapsGeocodingTool(config);
    }

    /**
     * 创建路径规划工具
     */
    public GoogleMapsDirectionsTool getDirectionsTool() {
        return new GoogleMapsDirectionsTool(config);
    }

    /**
     * 创建地点搜索工具
     */
    public GoogleMapsPlacesTool getPlacesTool() {
        return new GoogleMapsPlacesTool(config);
    }

    /**
     * 创建所有 Google Maps 工具
     * 
     * @return 所有 Google Maps 工具的列表
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        
        tools.add(getGeocodingTool());
        tools.add(getDirectionsTool());
        tools.add(getPlacesTool());
        
        log.info("Created {} Google Maps tools", tools.size());
        return tools;
    }

    /**
     * 根据名称获取工具
     * 
     * @param name 工具名称
     * @return 对应的工具实例
     * @throws IllegalArgumentException 不支持的工具名称
     */
    public BaseTool getToolByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be null or empty");
        }
        
        switch (name.toLowerCase().trim()) {
            case "google_maps_geocoding":
                return getGeocodingTool();
            case "google_maps_directions":
                return getDirectionsTool();
            case "google_maps_places":
                return getPlacesTool();
            default:
                throw new IllegalArgumentException("Unsupported tool name: " + name);
        }
    }

    /**
     * 获取支持的工具类型
     * 
     * @return 支持的工具类型列表
     */
    public List<String> getSupportedToolTypes() {
        return Arrays.asList("google_maps_geocoding", "google_maps_directions", "google_maps_places");
    }

    /**
     * 创建指定类型的工具
     * 
     * @param toolTypes 工具类型列表
     * @return 对应的工具实例列表
     */
    public List<BaseTool> createTools(List<String> toolTypes) {
        List<BaseTool> tools = new ArrayList<>();
        
        for (String toolType : toolTypes) {
            try {
                BaseTool tool = getToolByName(toolType);
                tools.add(tool);
            } catch (IllegalArgumentException e) {
                log.warn("Skipping unsupported tool type: {}", toolType);
            }
        }
        
        log.info("Created {} Google Maps tools from {} requested types", tools.size(), toolTypes.size());
        return tools;
    }
}
