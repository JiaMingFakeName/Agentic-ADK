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
import com.alibaba.langengine.googlemaps.sdk.request.DirectionsRequest;
import com.alibaba.langengine.googlemaps.sdk.response.DirectionsResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Google Maps 路径规划工具
 */
@Slf4j
public class GoogleMapsDirectionsTool extends BaseTool {

    private final GoogleMapsClient client;

    public GoogleMapsDirectionsTool() {
        this.client = new GoogleMapsClient();
    }

    public GoogleMapsDirectionsTool(GoogleMapsConfiguration config) {
        this.client = new GoogleMapsClient(config.getApiKey(), config.getRegion(), config.getLanguage());
    }

    @Override
    public String getName() {
        return "google_maps_directions";
    }

    @Override
    public String getDescription() {
        return "使用 Google Maps API 获取两点之间的路径规划。支持多种交通方式（驾车、步行、公交等）。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> originParam = new HashMap<>();
        originParam.put("type", "string");
        originParam.put("description", "起点地址或坐标");
        properties.put("origin", originParam);
        
        Map<String, Object> destinationParam = new HashMap<>();
        destinationParam.put("type", "string");
        destinationParam.put("description", "终点地址或坐标");
        properties.put("destination", destinationParam);
        
        Map<String, Object> modeParam = new HashMap<>();
        modeParam.put("type", "string");
        modeParam.put("description", "交通方式：driving（驾车）、walking（步行）、bicycling（骑行）、transit（公交）");
        modeParam.put("enum", new String[]{"driving", "walking", "bicycling", "transit"});
        properties.put("mode", modeParam);
        
        Map<String, Object> waypointsParam = new HashMap<>();
        waypointsParam.put("type", "string");
        waypointsParam.put("description", "途经点，多个点用 | 分隔");
        properties.put("waypoints", waypointsParam);
        
        Map<String, Object> avoidParam = new HashMap<>();
        avoidParam.put("type", "string");
        avoidParam.put("description", "避开：tolls（收费站）、highways（高速公路）、ferries（渡轮）、indoor（室内）");
        properties.put("avoid", avoidParam);
        
        Map<String, Object> unitsParam = new HashMap<>();
        unitsParam.put("type", "string");
        unitsParam.put("description", "单位：metric（公制）、imperial（英制）");
        unitsParam.put("enum", new String[]{"metric", "imperial"});
        properties.put("units", unitsParam);
        
        Map<String, Object> languageParam = new HashMap<>();
        languageParam.put("type", "string");
        languageParam.put("description", "语言代码（如 'en', 'zh-CN'）");
        properties.put("language", languageParam);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"origin", "destination"});
        
        return parameters;
    }

    @Override
    public Object run(Map<String, Object> parameters) {
        try {
            String origin = (String) parameters.get("origin");
            String destination = (String) parameters.get("destination");
            String mode = (String) parameters.get("mode");
            String waypoints = (String) parameters.get("waypoints");
            String avoid = (String) parameters.get("avoid");
            String units = (String) parameters.get("units");
            String language = (String) parameters.get("language");

            if (origin == null || origin.trim().isEmpty()) {
                throw new IllegalArgumentException("起点不能为空");
            }
            if (destination == null || destination.trim().isEmpty()) {
                throw new IllegalArgumentException("终点不能为空");
            }

            DirectionsRequest request = new DirectionsRequest(origin, destination);
            
            if (mode != null) {
                request.setMode(mode);
            }
            if (waypoints != null) {
                request.setWaypoints(waypoints);
            }
            if (avoid != null) {
                request.setAvoid(avoid);
            }
            if (units != null) {
                request.setUnits(units);
            }
            if (language != null) {
                request.setLanguage(language);
            }

            DirectionsResponse response = client.directions(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatus());
            result.put("routes", response.getRoutes());
            
            if (response.getErrorMessage() != null) {
                result.put("error_message", response.getErrorMessage());
            }
            
            return result;
            
        } catch (GoogleMapsException e) {
            log.error("Google Maps directions error: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("error_code", e.getErrorCode());
            return errorResult;
        } catch (Exception e) {
            log.error("Unexpected error in Google Maps directions: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Unexpected error: " + e.getMessage());
            return errorResult;
        }
    }
}
