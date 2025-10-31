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
package com.alibaba.langengine.tencentvdb.config;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;



public class DefaultTencentVdbConfig implements TencentVdbConfig {

    private final String serverUrl;
    private final String username;
    private final String password;
    private final String databaseName;
    private final int connectionTimeoutMs;
    private final int requestTimeoutMs;

    /**
     * 从配置文件加载配置
     */
    public DefaultTencentVdbConfig() {
        this.serverUrl = WorkPropertiesUtils.get("tencent_vdb_server_url");
        this.username = WorkPropertiesUtils.get("tencent_vdb_username");
        this.password = WorkPropertiesUtils.get("tencent_vdb_password");
        this.databaseName = WorkPropertiesUtils.get("tencent_vdb_database");
        this.connectionTimeoutMs = 30000;
        this.requestTimeoutMs = 60000;
    }

    /**
     * 直接指定配置参数
     *
     * @param serverUrl 服务器URL
     * @param username 用户名
     * @param password 密码
     * @param databaseName 数据库名称
     */
    public DefaultTencentVdbConfig(String serverUrl, String username, String password, String databaseName) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.connectionTimeoutMs = 30000;
        this.requestTimeoutMs = 60000;
    }

    /**
     * 完整参数构造函数
     *
     * @param serverUrl 服务器URL
     * @param username 用户名
     * @param password 密码
     * @param databaseName 数据库名称
     * @param connectionTimeoutMs 连接超时时间
     * @param requestTimeoutMs 请求超时时间
     */
    public DefaultTencentVdbConfig(String serverUrl, String username, String password,
                                   String databaseName, int connectionTimeoutMs, int requestTimeoutMs) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.requestTimeoutMs = requestTimeoutMs;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    @Override
    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

}
