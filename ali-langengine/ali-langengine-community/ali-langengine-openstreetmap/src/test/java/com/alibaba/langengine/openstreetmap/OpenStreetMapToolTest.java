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

package com.alibaba.langengine.openstreetmap;

import com.alibaba.langengine.openstreetmap.tools.OpenStreetMapGeocodingTool;
import com.alibaba.langengine.openstreetmap.tools.OpenStreetMapLookupTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenStreetMap 工具测试
 */
public class OpenStreetMapToolTest {

    private OpenStreetMapGeocodingTool geocodingTool;
    private OpenStreetMapLookupTool lookupTool;

    @BeforeEach
    void setUp() {
        geocodingTool = new OpenStreetMapGeocodingTool();
        lookupTool = new OpenStreetMapLookupTool();
    }

    @Test
    void testGeocodingToolName() {
        assertEquals("openstreetmap_geocoding", geocodingTool.getName());
    }

    @Test
    void testGeocodingToolDescription() {
        assertNotNull(geocodingTool.getDescription());
        assertTrue(geocodingTool.getDescription().contains("OpenStreetMap"));
    }

    @Test
    void testGeocodingToolParameters() {
        Map<String, Object> parameters = geocodingTool.getParameters();
        assertNotNull(parameters);
        assertEquals("object", parameters.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parameters.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("query"));
        assertTrue(properties.containsKey("lat"));
        assertTrue(properties.containsKey("lon"));
    }

    @Test
    void testGeocodingWithAddress() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("query", "北京市天安门广场");
        parameters.put("limit", 5);
        
        Object result = geocodingTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // 网络错误或其他异常
            assertTrue(resultMap.get("error").toString().contains("error"));
        } else {
            assertTrue(resultMap.containsKey("results"));
            assertTrue(resultMap.containsKey("count"));
        }
    }

    @Test
    void testGeocodingWithCoordinates() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("lat", 39.9042);
        parameters.put("lon", 116.4074);
        parameters.put("zoom", 18);
        
        Object result = geocodingTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // 网络错误或其他异常
            assertTrue(resultMap.get("error").toString().contains("error"));
        } else {
            assertTrue(resultMap.containsKey("result"));
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
    void testLookupToolName() {
        assertEquals("openstreetmap_lookup", lookupTool.getName());
    }

    @Test
    void testLookupToolDescription() {
        assertNotNull(lookupTool.getDescription());
        assertTrue(lookupTool.getDescription().contains("OpenStreetMap"));
    }

    @Test
    void testLookupToolParameters() {
        Map<String, Object> parameters = lookupTool.getParameters();
        assertNotNull(parameters);
        assertEquals("object", parameters.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parameters.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("osm_ids"));
    }

    @Test
    void testLookupWithValidOsmId() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("osm_ids", "N123456");
        
        Object result = lookupTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        if (resultMap.containsKey("error")) {
            // 网络错误或其他异常
            assertTrue(resultMap.get("error").toString().contains("error"));
        } else {
            assertTrue(resultMap.containsKey("results"));
            assertTrue(resultMap.containsKey("count"));
        }
    }

    @Test
    void testLookupWithInvalidInput() {
        Map<String, Object> parameters = new HashMap<>();
        // 不提供 OSM ID
        
        Object result = lookupTool.run(parameters);
        assertNotNull(result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("error"));
    }

    @Test
    void testToolFactory() {
        OpenStreetMapToolFactory factory = new OpenStreetMapToolFactory();
        
        assertNotNull(factory.getGeocodingTool());
        assertNotNull(factory.getLookupTool());
        
        assertEquals(2, factory.getAllTools().size());
        assertEquals(2, factory.getSupportedToolTypes().size());
        
        assertTrue(factory.getSupportedToolTypes().contains("openstreetmap_geocoding"));
        assertTrue(factory.getSupportedToolTypes().contains("openstreetmap_lookup"));
    }

    @Test
    void testToolFactoryByName() {
        OpenStreetMapToolFactory factory = new OpenStreetMapToolFactory();
        
        assertNotNull(factory.getToolByName("openstreetmap_geocoding"));
        assertNotNull(factory.getToolByName("openstreetmap_lookup"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("invalid_tool");
        });
    }
}
