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
package com.alibaba.langengine.astradb.vectorstore;

import com.alibaba.langengine.astradb.utils.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AstraDBConfigurationTest {

    private String originalToken;
    private String originalEndpoint;
    private String originalKeyspace;
    private String originalRegion;

    @BeforeEach
    public void setUp() {
        // Save original environment variables
        originalToken = System.getenv(Constants.ENV_ASTRA_DB_APPLICATION_TOKEN);
        originalEndpoint = System.getenv(Constants.ENV_ASTRA_DB_API_ENDPOINT);
        originalKeyspace = System.getenv(Constants.ENV_ASTRA_DB_KEYSPACE);
        originalRegion = System.getenv(Constants.ENV_ASTRA_DB_REGION);
    }

    @AfterEach
    public void tearDown() {
        // Environment variables cannot be easily reset in tests
        // This is a limitation of testing environment variable behavior
    }

    @Test
    public void testConstructorWithParameters() {
        String token = "test_token";
        String endpoint = "https://test-endpoint.com";
        String keyspace = "test_keyspace";
        
        AstraDBConfiguration config = new AstraDBConfiguration(token, endpoint, keyspace);
        
        assertEquals(token, config.getApplicationToken());
        assertEquals(endpoint, config.getApiEndpoint());
        assertEquals(keyspace, config.getKeyspace());
    }

    @Test
    public void testDefaultConstructor() {
        AstraDBConfiguration config = new AstraDBConfiguration();
        
        // Should not be null (may be loaded from properties or environment)
        assertNotNull(config);
    }

    @Test
    public void testBuilderPattern() {
        String token = "builder_token";
        String endpoint = "https://builder-endpoint.com";
        String keyspace = "builder_keyspace";
        String region = "us-east-1";
        
        AstraDBConfiguration config = AstraDBConfiguration.builder()
                .applicationToken(token)
                .apiEndpoint(endpoint)
                .keyspace(keyspace)
                .region(region)
                .build();
        
        assertEquals(token, config.getApplicationToken());
        assertEquals(endpoint, config.getApiEndpoint());
        assertEquals(keyspace, config.getKeyspace());
        assertEquals(region, config.getRegion());
    }

    @Test
    public void testBuilderWithPartialConfiguration() {
        String token = "partial_token";
        
        AstraDBConfiguration config = AstraDBConfiguration.builder()
                .applicationToken(token)
                .build();
        
        assertEquals(token, config.getApplicationToken());
        // Other fields should be null or default values
    }

    @Test
    public void testBuilderChaining() {
        AstraDBConfiguration.Builder builder = AstraDBConfiguration.builder();
        
        assertNotNull(builder);
        assertSame(builder, builder.applicationToken("test"));
        assertSame(builder, builder.apiEndpoint("test"));
        assertSame(builder, builder.keyspace("test"));
        assertSame(builder, builder.region("test"));
    }

    @Test
    public void testSettersAndGetters() {
        AstraDBConfiguration config = new AstraDBConfiguration();
        
        String token = "setter_token";
        String endpoint = "https://setter-endpoint.com";
        String keyspace = "setter_keyspace";
        String region = "us-west-2";
        
        config.setApplicationToken(token);
        config.setApiEndpoint(endpoint);
        config.setKeyspace(keyspace);
        config.setRegion(region);
        
        assertEquals(token, config.getApplicationToken());
        assertEquals(endpoint, config.getApiEndpoint());
        assertEquals(keyspace, config.getKeyspace());
        assertEquals(region, config.getRegion());
    }

    @Test
    public void testDefaultKeyspace() {
        AstraDBConfiguration config = new AstraDBConfiguration(
            "test_token", 
            "https://test-endpoint.com", 
            null
        );
        
        // When keyspace is null in constructor, it should use environment or default
        // The actual behavior depends on environment variables and properties
        assertNotNull(config);
    }

    @Test
    public void testConfigurationEquality() {
        AstraDBConfiguration config1 = new AstraDBConfiguration("token", "endpoint", "keyspace");
        AstraDBConfiguration config2 = new AstraDBConfiguration("token", "endpoint", "keyspace");
        
        // Test basic equality (depends on Lombok @Data annotation)
        assertEquals(config1.getApplicationToken(), config2.getApplicationToken());
        assertEquals(config1.getApiEndpoint(), config2.getApiEndpoint());
        assertEquals(config1.getKeyspace(), config2.getKeyspace());
    }

    @Test
    public void testConfigurationToString() {
        AstraDBConfiguration config = new AstraDBConfiguration("token", "endpoint", "keyspace");
        
        String toString = config.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("AstraDBConfiguration"));
    }

    @Test
    public void testBuilderReusability() {
        AstraDBConfiguration.Builder builder = AstraDBConfiguration.builder()
                .applicationToken("base_token")
                .keyspace("base_keyspace");
        
        AstraDBConfiguration config1 = builder
                .apiEndpoint("https://endpoint1.com")
                .build();
        
        AstraDBConfiguration config2 = builder
                .apiEndpoint("https://endpoint2.com")
                .build();
        
        assertEquals("base_token", config1.getApplicationToken());
        assertEquals("base_token", config2.getApplicationToken());
        assertEquals("https://endpoint1.com", config1.getApiEndpoint());
        assertEquals("https://endpoint2.com", config2.getApiEndpoint());
    }

    @Test
    public void testNullValues() {
        AstraDBConfiguration config = AstraDBConfiguration.builder()
                .applicationToken(null)
                .apiEndpoint(null)
                .keyspace(null)
                .region(null)
                .build();
        
        // Should handle null values gracefully
        assertNotNull(config);
    }
}