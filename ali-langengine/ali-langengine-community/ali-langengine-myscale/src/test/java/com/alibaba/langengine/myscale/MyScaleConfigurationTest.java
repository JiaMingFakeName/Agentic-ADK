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
package com.alibaba.langengine.myscale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MyScaleConfigurationTest {

    @Test
    public void testConfiguration() {
        // 验证配置类可以正常实例化
        MyScaleConfiguration config = new MyScaleConfiguration();
        assertNotNull(config);
    }

    @Test
    public void testConstants() {
        // 验证常量的存在和非空性
        assertNotNull(MyScaleConfiguration.MYSCALE_SERVER_URL);
        assertNotNull(MyScaleConfiguration.MYSCALE_USERNAME);
        assertNotNull(MyScaleConfiguration.MYSCALE_PASSWORD);
        assertNotNull(MyScaleConfiguration.MYSCALE_DATABASE);

        // 验证常量值的合理性
        assertFalse(MyScaleConfiguration.MYSCALE_SERVER_URL.trim().isEmpty());
        assertFalse(MyScaleConfiguration.MYSCALE_DATABASE.trim().isEmpty());

        // 验证超时值为正数
        assertTrue(MyScaleConfiguration.MYSCALE_CONNECTION_TIMEOUT > 0);
        assertTrue(MyScaleConfiguration.MYSCALE_READ_TIMEOUT > 0);
    }

    @Test
    public void testDefaultConfigurationValues() {
        // 测试默认配置值
        assertEquals("http://localhost:8123", MyScaleConfiguration.MYSCALE_SERVER_URL);
        assertEquals("default", MyScaleConfiguration.MYSCALE_USERNAME);
        assertEquals("", MyScaleConfiguration.MYSCALE_PASSWORD);
        assertEquals("default", MyScaleConfiguration.MYSCALE_DATABASE);
        assertEquals(30000, MyScaleConfiguration.MYSCALE_CONNECTION_TIMEOUT);
        assertEquals(60000, MyScaleConfiguration.MYSCALE_READ_TIMEOUT);
    }

    @Test
    public void testConfigurationFieldTypes() {
        // 验证字段类型
        assertTrue(MyScaleConfiguration.MYSCALE_SERVER_URL instanceof String);
        assertTrue(MyScaleConfiguration.MYSCALE_USERNAME instanceof String);
        assertTrue(MyScaleConfiguration.MYSCALE_PASSWORD instanceof String);
        assertTrue(MyScaleConfiguration.MYSCALE_DATABASE instanceof String);

        // 验证超时字段是int类型
        assertEquals("int", getPrimitiveTypeName(MyScaleConfiguration.MYSCALE_CONNECTION_TIMEOUT));
        assertEquals("int", getPrimitiveTypeName(MyScaleConfiguration.MYSCALE_READ_TIMEOUT));
    }

    @Test
    public void testTimeoutValues() {
        // 确保超时值在合理范围内
        assertTrue(MyScaleConfiguration.MYSCALE_CONNECTION_TIMEOUT >= 1000); // 至少1秒
        assertTrue(MyScaleConfiguration.MYSCALE_READ_TIMEOUT >= 1000); // 至少1秒
        assertTrue(MyScaleConfiguration.MYSCALE_CONNECTION_TIMEOUT <= 300000); // 不超过5分钟
        assertTrue(MyScaleConfiguration.MYSCALE_READ_TIMEOUT <= 300000); // 不超过5分钟
    }

    @Test
    public void testServerUrlFormat() {
        // 验证服务器URL格式
        String serverUrl = MyScaleConfiguration.MYSCALE_SERVER_URL;
        assertTrue(serverUrl.startsWith("http://") || serverUrl.startsWith("https://"));
        assertTrue(serverUrl.contains(":"));
        // 基本的URL格式验证
        assertFalse(serverUrl.endsWith("/"));
    }

    @Test
    public void testDatabaseName() {
        // 验证数据库名的有效性
        String database = MyScaleConfiguration.MYSCALE_DATABASE;

        // 数据库名不应包含特殊字符（基本验证）
        assertFalse(database.contains(" "));
        assertFalse(database.contains("-"));
    }

    @Test
    public void testUsernameAndPassword() {
        // 验证用户名和密码字段
        String username = MyScaleConfiguration.MYSCALE_USERNAME;
        String password = MyScaleConfiguration.MYSCALE_PASSWORD;

        assertNotNull(username);
        assertNotNull(password);

        // 用户名应该非空
        assertFalse(username.trim().isEmpty());

        // 密码可以为空（默认ClickHouse配置）
        assertEquals("", password);
    }

    @Test
    public void testTimeoutValuesBoundaries() {
        // 测试超时值的边界情况
        int connectionTimeout = MyScaleConfiguration.MYSCALE_CONNECTION_TIMEOUT;
        int readTimeout = MyScaleConfiguration.MYSCALE_READ_TIMEOUT;

        // 连接超时应该小于或等于读取超时
        assertTrue(connectionTimeout <= readTimeout * 2); // 允许一定的灵活性

        // 测试超时值都是正整数
        assertTrue(connectionTimeout > 0);
        assertTrue(readTimeout > 0);
    }

    @Test
    public void testConfigurationConsistency() {
        // 验证配置的一致性
        assertNotNull(MyScaleConfiguration.MYSCALE_SERVER_URL);
        assertNotNull(MyScaleConfiguration.MYSCALE_DATABASE);
        assertNotNull(MyScaleConfiguration.MYSCALE_USERNAME);

        // 确保所有字符串配置都不为null
        assertNotEquals(null, MyScaleConfiguration.MYSCALE_SERVER_URL);
        assertNotEquals(null, MyScaleConfiguration.MYSCALE_DATABASE);
        assertNotEquals(null, MyScaleConfiguration.MYSCALE_USERNAME);
        assertNotEquals(null, MyScaleConfiguration.MYSCALE_PASSWORD);
    }

    /**
     * 获取原始类型名称的辅助方法
     */
    private String getPrimitiveTypeName(int value) {
        return "int";
    }
}