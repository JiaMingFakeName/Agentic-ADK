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

package com.alibaba.langengine.openstreetmap.tools;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openstreetmap.OpenStreetMapConfiguration;
import com.alibaba.langengine.openstreetmap.sdk.OpenStreetMapClient;
import com.alibaba.langengine.openstreetmap.sdk.OpenStreetMapException;
import com.alibaba.langengine.openstreetmap.sdk.request.NominatimRequest;
import com.alibaba.langengine.openstreetmap.sdk.request.ReverseGeocodingRequest;
import com.alibaba.langengine.openstreetmap.sdk.response.NominatimResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenStreetMap 地理编码工具
 */
@Slf4j
public class OpenStreetMapGeocodingTool extends BaseTool {

    private final OpenStreetMapClient client;

    public OpenStreetMapGeocodingTool() {
        this.client = new OpenStreetMapClient();
    }

    public OpenStreetMapGeocodingTool(OpenStreetMapConfiguration config) {
        this.client = new OpenStreetMapClient(config.getApiUrl(), config.getEmail(), 
                config.getLanguage(), config.getCountryCodes(), config.getUserAgent());
    }

    @Override
    public String getName() {
        return "openstreetmap_geocoding";
    }

    @Override
    public String getDescription() {
        return "使用 OpenStreetMap Nominatim API 进行地理编码和反向地理编码。免费开源的地理编码服务。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("type", "string");
        queryParam.put("description", "要查询的地址或地点名称");
        properties.put("query", queryParam);
        
        Map<String, Object> latParam = new HashMap<>();
        latParam.put("type", "number");
        latParam.put("description", "纬度（用于反向地理编码）");
        properties.put("lat", latParam);
        
        Map<String, Object> lonParam = new HashMap<>();
        lonParam.put("type", "number");
        lonParam.put("description", "经度（用于反向地理编码）");
        properties.put("lon", lonParam);
        
        Map<String, Object> limitParam = new HashMap<>();
        limitParam.put("type", "integer");
        limitParam.put("description", "返回结果数量限制（1-50）");
        limitParam.put("minimum", 1);
        limitParam.put("maximum", 50);
        properties.put("limit", limitParam);
        
        Map<String, Object> countryCodesParam = new HashMap<>();
        countryCodesParam.put("type", "string");
        countryCodesParam.put("description", "国家代码过滤（如 'us', 'cn'）");
        properties.put("country_codes", countryCodesParam);
        
        Map<String, Object> languageParam = new HashMap<>();
        languageParam.put("type", "string");
        languageParam.put("description", "语言代码（如 'en', 'zh-CN'）");
        properties.put("language", languageParam);
        
        Map<String, Object> addressDetailsParam = new HashMap<>();
        addressDetailsParam.put("type", "boolean");
        addressDetailsParam.put("description", "是否返回详细地址信息");
        properties.put("address_details", addressDetailsParam);
        
        Map<String, Object> extraTagsParam = new HashMap<>();
        extraTagsParam.put("type", "boolean");
        extraTagsParam.put("description", "是否返回额外标签信息");
        properties.put("extra_tags", extraTagsParam);
        
        Map<String, Object> nameDetailsParam = new HashMap<>();
        nameDetailsParam.put("type", "boolean");
        nameDetailsParam.put("description", "是否返回名称详情");
        properties.put("name_details", nameDetailsParam);
        
        Map<String, Object> zoomParam = new HashMap<>();
        zoomParam.put("type", "integer");
        zoomParam.put("description", "缩放级别（1-18），用于反向地理编码");
        zoomParam.put("minimum", 1);
        zoomParam.put("maximum", 18);
        properties.put("zoom", zoomParam);
        
        parameters.put("properties", properties);
        
        return parameters;
    }

    @Override
    public Object run(Map<String, Object> parameters) {
        try {
            String query = (String) parameters.get("query");
            Double lat = (Double) parameters.get("lat");
            Double lon = (Double) parameters.get("lon");
            Integer limit = (Integer) parameters.get("limit");
            String countryCodes = (String) parameters.get("country_codes");
            String language = (String) parameters.get("language");
            Boolean addressDetails = (Boolean) parameters.get("address_details");
            Boolean extraTags = (Boolean) parameters.get("extra_tags");
            Boolean nameDetails = (Boolean) parameters.get("name_details");
            Integer zoom = (Integer) parameters.get("zoom");

            Map<String, Object> result = new HashMap<>();
            
            if (query != null && !query.trim().isEmpty()) {
                // 正向地理编码
                NominatimRequest request = new NominatimRequest(query);
                
                if (limit != null) {
                    request.setLimit(String.valueOf(limit));
                }
                if (countryCodes != null) {
                    request.setCountryCodes(countryCodes);
                }
                if (language != null) {
                    request.setAcceptLanguage(language);
                }
                if (addressDetails != null) {
                    request.setAddressDetails(addressDetails ? "1" : "0");
                }
                if (extraTags != null) {
                    request.setExtraTags(extraTags ? "1" : "0");
                }
                if (nameDetails != null) {
                    request.setNameDetails(nameDetails ? "1" : "0");
                }

                List<NominatimResponse> responses = client.geocoding(request);
                result.put("results", responses);
                result.put("count", responses.size());
                
            } else if (lat != null && lon != null) {
                // 反向地理编码
                ReverseGeocodingRequest request = new ReverseGeocodingRequest(lat, lon);
                
                if (language != null) {
                    request.setAcceptLanguage(language);
                }
                if (addressDetails != null) {
                    request.setAddressDetails(addressDetails ? "1" : "0");
                }
                if (extraTags != null) {
                    request.setExtraTags(extraTags ? "1" : "0");
                }
                if (nameDetails != null) {
                    request.setNameDetails(nameDetails ? "1" : "0");
                }
                if (zoom != null) {
                    request.setZoom(String.valueOf(zoom));
                }

                NominatimResponse response = client.reverseGeocoding(request);
                result.put("result", response);
                
            } else {
                throw new IllegalArgumentException("必须提供查询文本或坐标（经纬度）");
            }
            
            return result;
            
        } catch (OpenStreetMapException e) {
            log.error("OpenStreetMap geocoding error: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("error_code", e.getErrorCode());
            return errorResult;
        } catch (Exception e) {
            log.error("Unexpected error in OpenStreetMap geocoding: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Unexpected error: " + e.getMessage());
            return errorResult;
        }
    }
}
