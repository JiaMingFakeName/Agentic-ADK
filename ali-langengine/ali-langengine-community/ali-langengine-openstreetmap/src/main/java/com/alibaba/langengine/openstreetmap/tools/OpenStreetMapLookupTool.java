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
import com.alibaba.langengine.openstreetmap.sdk.response.NominatimResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenStreetMap 查找工具
 */
@Slf4j
public class OpenStreetMapLookupTool extends BaseTool {

    private final OpenStreetMapClient client;

    public OpenStreetMapLookupTool() {
        this.client = new OpenStreetMapClient();
    }

    public OpenStreetMapLookupTool(OpenStreetMapConfiguration config) {
        this.client = new OpenStreetMapClient(config.getApiUrl(), config.getEmail(), 
                config.getLanguage(), config.getCountryCodes(), config.getUserAgent());
    }

    @Override
    public String getName() {
        return "openstreetmap_lookup";
    }

    @Override
    public String getDescription() {
        return "使用 OpenStreetMap Nominatim API 通过 OSM ID 查找地点详情。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> osmIdsParam = new HashMap<>();
        osmIdsParam.put("type", "string");
        osmIdsParam.put("description", "OSM ID，格式：type_id（如 N123456, W789012, R345678）");
        properties.put("osm_ids", osmIdsParam);
        
        Map<String, Object> languageParam = new HashMap<>();
        languageParam.put("type", "string");
        languageParam.put("description", "语言代码（如 'en', 'zh-CN'）");
        properties.put("language", languageParam);
        
        parameters.put("properties", properties);
        parameters.put("required", new String[]{"osm_ids"});
        
        return parameters;
    }

    @Override
    public Object run(Map<String, Object> parameters) {
        try {
            String osmIds = (String) parameters.get("osm_ids");
            String language = (String) parameters.get("language");

            if (osmIds == null || osmIds.trim().isEmpty()) {
                throw new IllegalArgumentException("OSM ID 不能为空");
            }

            List<NominatimResponse> responses = client.lookup(osmIds);
            
            Map<String, Object> result = new HashMap<>();
            result.put("results", responses);
            result.put("count", responses.size());
            
            return result;
            
        } catch (OpenStreetMapException e) {
            log.error("OpenStreetMap lookup error: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("error_code", e.getErrorCode());
            return errorResult;
        } catch (Exception e) {
            log.error("Unexpected error in OpenStreetMap lookup: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Unexpected error: " + e.getMessage());
            return errorResult;
        }
    }
}
