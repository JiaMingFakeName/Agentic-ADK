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
import com.alibaba.langengine.googlemaps.sdk.request.GeocodingRequest;
import com.alibaba.langengine.googlemaps.sdk.response.GeocodingResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Google Maps 地理编码工具
 */
@Slf4j
public class GoogleMapsGeocodingTool extends BaseTool {

    private final GoogleMapsClient client;

    public GoogleMapsGeocodingTool() {
        this.client = new GoogleMapsClient();
    }

    public GoogleMapsGeocodingTool(GoogleMapsConfiguration config) {
        this.client = new GoogleMapsClient(config.getApiKey(), config.getRegion(), config.getLanguage());
    }

    @Override
    public String getName() {
        return "google_maps_geocoding";
    }

    @Override
    public String getDescription() {
        return "使用 Google Maps API 进行地理编码和反向地理编码。可以将地址转换为坐标，或将坐标转换为地址。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> addressParam = new HashMap<>();
        addressParam.put("type", "string");
        addressParam.put("description", "要查询的地址");
        properties.put("address", addressParam);
        
        Map<String, Object> latParam = new HashMap<>();
        latParam.put("type", "number");
        latParam.put("description", "纬度（用于反向地理编码）");
        properties.put("lat", latParam);
        
        Map<String, Object> lngParam = new HashMap<>();
        lngParam.put("type", "number");
        lngParam.put("description", "经度（用于反向地理编码）");
        properties.put("lng", lngParam);
        
        Map<String, Object> regionParam = new HashMap<>();
        regionParam.put("type", "string");
        regionParam.put("description", "地区代码（如 'us', 'cn'）");
        properties.put("region", regionParam);
        
        Map<String, Object> languageParam = new HashMap<>();
        languageParam.put("type", "string");
        languageParam.put("description", "语言代码（如 'en', 'zh-CN'）");
        properties.put("language", languageParam);
        
        parameters.put("properties", properties);
        
        return parameters;
    }

    @Override
    public Object run(Map<String, Object> parameters) {
        try {
            String address = (String) parameters.get("address");
            Double lat = (Double) parameters.get("lat");
            Double lng = (Double) parameters.get("lng");
            String region = (String) parameters.get("region");
            String language = (String) parameters.get("language");

            GeocodingRequest request = new GeocodingRequest();
            
            if (address != null && !address.trim().isEmpty()) {
                // 正向地理编码
                request.setAddress(address);
            } else if (lat != null && lng != null) {
                // 反向地理编码
                request.setLatlng(lat + "," + lng);
            } else {
                throw new IllegalArgumentException("必须提供地址或坐标（经纬度）");
            }
            
            if (region != null) {
                request.setRegion(region);
            }
            if (language != null) {
                request.setLanguage(language);
            }

            GeocodingResponse response = client.geocoding(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatus());
            result.put("results", response.getResults());
            
            if (response.getErrorMessage() != null) {
                result.put("error_message", response.getErrorMessage());
            }
            
            return result;
            
        } catch (GoogleMapsException e) {
            log.error("Google Maps geocoding error: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("error_code", e.getErrorCode());
            return errorResult;
        } catch (Exception e) {
            log.error("Unexpected error in Google Maps geocoding: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Unexpected error: " + e.getMessage());
            return errorResult;
        }
    }
}
