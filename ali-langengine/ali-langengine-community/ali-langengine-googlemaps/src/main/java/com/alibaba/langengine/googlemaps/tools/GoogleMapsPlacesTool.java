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

package com.alibaba.langengine.googlemaps.tools;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.googlemaps.GoogleMapsConfiguration;
import com.alibaba.langengine.googlemaps.sdk.GoogleMapsClient;
import com.alibaba.langengine.googlemaps.sdk.GoogleMapsException;
import com.alibaba.langengine.googlemaps.sdk.request.PlacesSearchRequest;
import com.alibaba.langengine.googlemaps.sdk.response.PlacesSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Google Maps 地点搜索工具
 */
@Slf4j
public class GoogleMapsPlacesTool extends BaseTool {

    private final GoogleMapsClient client;

    public GoogleMapsPlacesTool() {
        this.client = new GoogleMapsClient();
    }

    public GoogleMapsPlacesTool(GoogleMapsConfiguration config) {
        this.client = new GoogleMapsClient(config.getApiKey(), config.getRegion(), config.getLanguage());
    }

    @Override
    public String getName() {
        return "google_maps_places";
    }

    @Override
    public String getDescription() {
        return "使用 Google Maps Places API 搜索地点。支持文本搜索和附近搜索。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("type", "string");
        queryParam.put("description", "搜索查询文本");
        properties.put("query", queryParam);
        
        Map<String, Object> locationParam = new HashMap<>();
        locationParam.put("type", "string");
        locationParam.put("description", "位置坐标（纬度,经度），用于附近搜索");
        properties.put("location", locationParam);
        
        Map<String, Object> radiusParam = new HashMap<>();
        radiusParam.put("type", "integer");
        radiusParam.put("description", "搜索半径（米），用于附近搜索");
        properties.put("radius", radiusParam);
        
        Map<String, Object> typeParam = new HashMap<>();
        typeParam.put("type", "string");
        typeParam.put("description", "地点类型（如 restaurant, hotel, gas_station 等）");
        properties.put("type", typeParam);
        
        Map<String, Object> keywordParam = new HashMap<>();
        keywordParam.put("type", "string");
        keywordParam.put("description", "关键词");
        properties.put("keyword", keywordParam);
        
        Map<String, Object> nameParam = new HashMap<>();
        nameParam.put("type", "string");
        nameParam.put("description", "地点名称");
        properties.put("name", nameParam);
        
        Map<String, Object> languageParam = new HashMap<>();
        languageParam.put("type", "string");
        languageParam.put("description", "语言代码（如 'en', 'zh-CN'）");
        properties.put("language", languageParam);
        
        Map<String, Object> regionParam = new HashMap<>();
        regionParam.put("type", "string");
        regionParam.put("description", "地区代码（如 'us', 'cn'）");
        properties.put("region", regionParam);
        
        Map<String, Object> minPriceParam = new HashMap<>();
        minPriceParam.put("type", "integer");
        minPriceParam.put("description", "最低价格等级（0-4）");
        minPriceParam.put("minimum", 0);
        minPriceParam.put("maximum", 4);
        properties.put("min_price", minPriceParam);
        
        Map<String, Object> maxPriceParam = new HashMap<>();
        maxPriceParam.put("type", "integer");
        maxPriceParam.put("description", "最高价格等级（0-4）");
        maxPriceParam.put("minimum", 0);
        maxPriceParam.put("maximum", 4);
        properties.put("max_price", maxPriceParam);
        
        Map<String, Object> openNowParam = new HashMap<>();
        openNowParam.put("type", "boolean");
        openNowParam.put("description", "是否只搜索当前营业的地点");
        properties.put("open_now", openNowParam);
        
        Map<String, Object> rankByParam = new HashMap<>();
        rankByParam.put("type", "string");
        rankByParam.put("description", "排序方式：prominence（相关性）、distance（距离）");
        rankByParam.put("enum", new String[]{"prominence", "distance"});
        properties.put("rank_by", rankByParam);
        
        parameters.put("properties", properties);
        
        return parameters;
    }

    @Override
    public Object run(Map<String, Object> parameters) {
        try {
            String query = (String) parameters.get("query");
            String location = (String) parameters.get("location");
            Integer radius = (Integer) parameters.get("radius");
            String type = (String) parameters.get("type");
            String keyword = (String) parameters.get("keyword");
            String name = (String) parameters.get("name");
            String language = (String) parameters.get("language");
            String region = (String) parameters.get("region");
            Integer minPrice = (Integer) parameters.get("min_price");
            Integer maxPrice = (Integer) parameters.get("max_price");
            Boolean openNow = (Boolean) parameters.get("open_now");
            String rankBy = (String) parameters.get("rank_by");

            PlacesSearchRequest request = new PlacesSearchRequest();
            
            if (query != null && !query.trim().isEmpty()) {
                // 文本搜索
                request.setQuery(query);
            } else if (location != null && radius != null) {
                // 附近搜索
                request.setLocation(location);
                request.setRadius(String.valueOf(radius));
            } else {
                throw new IllegalArgumentException("必须提供查询文本或位置+半径");
            }
            
            if (type != null) {
                request.setType(type);
            }
            if (keyword != null) {
                request.setKeyword(keyword);
            }
            if (name != null) {
                request.setName(name);
            }
            if (language != null) {
                request.setLanguage(language);
            }
            if (region != null) {
                request.setRegion(region);
            }
            if (minPrice != null) {
                request.setMinPrice(String.valueOf(minPrice));
            }
            if (maxPrice != null) {
                request.setMaxPrice(String.valueOf(maxPrice));
            }
            if (openNow != null) {
                request.setOpenNow(String.valueOf(openNow));
            }
            if (rankBy != null) {
                request.setRankBy(rankBy);
            }

            PlacesSearchResponse response;
            if (query != null && !query.trim().isEmpty()) {
                response = client.placesTextSearch(request);
            } else {
                response = client.placesNearbySearch(request);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatus());
            result.put("results", response.getResults());
            
            if (response.getErrorMessage() != null) {
                result.put("error_message", response.getErrorMessage());
            }
            if (response.getNextPageToken() != null) {
                result.put("next_page_token", response.getNextPageToken());
            }
            
            return result;
            
        } catch (GoogleMapsException e) {
            log.error("Google Maps places error: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("error_code", e.getErrorCode());
            return errorResult;
        } catch (Exception e) {
            log.error("Unexpected error in Google Maps places: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Unexpected error: " + e.getMessage());
            return errorResult;
        }
    }
}
