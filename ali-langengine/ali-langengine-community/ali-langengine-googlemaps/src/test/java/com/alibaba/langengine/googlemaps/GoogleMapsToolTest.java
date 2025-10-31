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

import com.alibaba.langengine.googlemaps.tools.GoogleMapsDirectionsTool;
import com.alibaba.langengine.googlemaps.tools.GoogleMapsGeocodingTool;
import com.alibaba.langengine.googlemaps.tools.GoogleMapsPlacesTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Google Maps 工具测试
 */
@EnabledIfEnvironmentVariable(named = "GOOGLE_MAPS_API_KEY", matches = ".*")
public class GoogleMapsToolTest {

    private GoogleMapsGeocodingTool geocodingTool;
    private GoogleMapsDirectionsTool directionsTool;
    private GoogleMapsPlacesTool placesTool;

    @BeforeEach
    void setUp() {
        geocodingTool = new GoogleMapsGeocodingTool();
        directionsTool = new GoogleMapsDirectionsTool();
        placesTool = new GoogleMapsPlacesTool();
    }

    @Test
    void testGeocodingToolName() {
        assertEquals("google_maps_geocoding", geocodingTool.getName());
    }

    @Test
    void testGeocodingToolDescription() {
        assertNotNull(geocodingTool.getDescription());
        assertTrue(geocodingTool.getDescription().contains("Google Maps"));
    }

    @Test
    void testGeocodingToolParameters() {
        Map<String, Object> parameters = geocodingTool.getParameters();
        assertNotNull(parameters);
        assertEquals("object", parameters.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parameters.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("address"));
        assertTrue(properties.containsKey("lat"));
        assertTrue(properties.containsKey("lng"));
    }

    @Test
    void testGeocodingWithAddress() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", "北京市天安门广场");
        
        Object result = geocodingTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // API key 未配置或请求失败
            assertTrue(resultMap.get("error").toString().contains("API"));
        } else {
            assertTrue(resultMap.containsKey("status"));
            assertTrue(resultMap.containsKey("results"));
        }
    }

    @Test
    void testGeocodingWithCoordinates() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("lat", 39.9042);
        parameters.put("lng", 116.4074);
        
        Object result = geocodingTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // API key 未配置或请求失败
            assertTrue(resultMap.get("error").toString().contains("API"));
        } else {
            assertTrue(resultMap.containsKey("status"));
            assertTrue(resultMap.containsKey("results"));
        }
    }

    @Test
    void testGeocodingWithInvalidInput() {
        Map<String, Object> parameters = new HashMap<>();
        // 不提供任何参数
        
        Object result = geocodingTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("error"));
    }

    @Test
    void testDirectionsToolName() {
        assertEquals("google_maps_directions", directionsTool.getName());
    }

    @Test
    void testDirectionsToolParameters() {
        Map<String, Object> parameters = directionsTool.getParameters();
        assertNotNull(parameters);
        assertEquals("object", parameters.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parameters.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("origin"));
        assertTrue(properties.containsKey("destination"));
        assertTrue(properties.containsKey("mode"));
    }

    @Test
    void testDirectionsWithValidRoute() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("origin", "北京市天安门广场");
        parameters.put("destination", "北京市首都国际机场");
        parameters.put("mode", "driving");
        
        Object result = directionsTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // API key 未配置或请求失败
            assertTrue(resultMap.get("error").toString().contains("API"));
        } else {
            assertTrue(resultMap.containsKey("status"));
            assertTrue(resultMap.containsKey("routes"));
        }
    }

    @Test
    void testDirectionsWithInvalidInput() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("origin", "北京市天安门广场");
        // 不提供终点
        
        Object result = directionsTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("error"));
    }

    @Test
    void testPlacesToolName() {
        assertEquals("google_maps_places", placesTool.getName());
    }

    @Test
    void testPlacesToolParameters() {
        Map<String, Object> parameters = placesTool.getParameters();
        assertNotNull(parameters);
        assertEquals("object", parameters.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parameters.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("query"));
        assertTrue(properties.containsKey("location"));
        assertTrue(properties.containsKey("radius"));
    }

    @Test
    void testPlacesTextSearch() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "北京餐厅");
        
        Object result = placesTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // API key 未配置或请求失败
            assertTrue(resultMap.get("error").toString().contains("API"));
        } else {
            assertTrue(resultMap.containsKey("status"));
            assertTrue(resultMap.containsKey("results"));
        }
    }

    @Test
    void testPlacesNearbySearch() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("location", "39.9042,116.4074");
        parameters.put("radius", 1000);
        parameters.put("type", "restaurant");
        
        Object result = placesTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // API key 未配置或请求失败
            assertTrue(resultMap.get("error").toString().contains("API"));
        } else {
            assertTrue(resultMap.containsKey("status"));
            assertTrue(resultMap.containsKey("results"));
        }
    }

    @Test
    void testPlacesWithInvalidInput() {
        Map<String, Object> parameters = new HashMap<>();
        // 不提供任何搜索条件
        
        Object result = placesTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("error"));
    }
}
