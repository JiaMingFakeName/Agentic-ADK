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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Google Maps 工具工厂测试
 */
public class GoogleMapsToolFactoryTest {

    @Test
    void testToolFactoryCreation() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        assertNotNull(factory.getGeocodingTool());
        assertNotNull(factory.getDirectionsTool());
        assertNotNull(factory.getPlacesTool());
    }

    @Test
    void testGetAllTools() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        List<BaseTool> tools = factory.getAllTools();
        
        assertEquals(3, tools.size());
        
        List<String> toolNames = tools.stream()
                .map(BaseTool::getName)
                .toList();
        
        assertTrue(toolNames.contains("google_maps_geocoding"));
        assertTrue(toolNames.contains("google_maps_directions"));
        assertTrue(toolNames.contains("google_maps_places"));
    }

    @Test
    void testGetSupportedToolTypes() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        List<String> supportedTypes = factory.getSupportedToolTypes();
        
        assertEquals(3, supportedTypes.size());
        assertTrue(supportedTypes.contains("google_maps_geocoding"));
        assertTrue(supportedTypes.contains("google_maps_directions"));
        assertTrue(supportedTypes.contains("google_maps_places"));
    }

    @Test
    void testGetToolByName() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        BaseTool geocodingTool = factory.getToolByName("google_maps_geocoding");
        assertNotNull(geocodingTool);
        assertEquals("google_maps_geocoding", geocodingTool.getName());
        
        BaseTool directionsTool = factory.getToolByName("google_maps_directions");
        assertNotNull(directionsTool);
        assertEquals("google_maps_directions", directionsTool.getName());
        
        BaseTool placesTool = factory.getToolByName("google_maps_places");
        assertNotNull(placesTool);
        assertEquals("google_maps_places", placesTool.getName());
    }

    @Test
    void testGetToolByNameCaseInsensitive() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        BaseTool tool1 = factory.getToolByName("GOOGLE_MAPS_GEOCODING");
        assertNotNull(tool1);
        assertEquals("google_maps_geocoding", tool1.getName());
        
        BaseTool tool2 = factory.getToolByName("  google_maps_directions  ");
        assertNotNull(tool2);
        assertEquals("google_maps_directions", tool2.getName());
    }

    @Test
    void testGetToolByNameWithInvalidName() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("invalid_tool");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("   ");
        });
    }

    @Test
    void testCreateTools() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        List<String> toolTypes = Arrays.asList(
                "google_maps_geocoding",
                "google_maps_directions",
                "google_maps_places"
        );
        
        List<BaseTool> tools = factory.createTools(toolTypes);
        assertEquals(3, tools.size());
        
        List<String> toolNames = tools.stream()
                .map(BaseTool::getName)
                .toList();
        
        assertTrue(toolNames.contains("google_maps_geocoding"));
        assertTrue(toolNames.contains("google_maps_directions"));
        assertTrue(toolNames.contains("google_maps_places"));
    }

    @Test
    void testCreateToolsWithInvalidTypes() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        List<String> toolTypes = Arrays.asList(
                "google_maps_geocoding",
                "invalid_tool",
                "google_maps_directions"
        );
        
        List<BaseTool> tools = factory.createTools(toolTypes);
        assertEquals(2, tools.size()); // 只有两个有效工具
        
        List<String> toolNames = tools.stream()
                .map(BaseTool::getName)
                .toList();
        
        assertTrue(toolNames.contains("google_maps_geocoding"));
        assertTrue(toolNames.contains("google_maps_directions"));
        assertFalse(toolNames.contains("invalid_tool"));
    }

    @Test
    void testCreateToolsWithEmptyList() {
        GoogleMapsToolFactory factory = new GoogleMapsToolFactory();
        
        List<BaseTool> tools = factory.createTools(Arrays.asList());
        assertEquals(0, tools.size());
    }
}
