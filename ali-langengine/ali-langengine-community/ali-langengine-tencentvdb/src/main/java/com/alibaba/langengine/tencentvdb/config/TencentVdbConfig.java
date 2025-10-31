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



public interface TencentVdbConfig {

    /**
     * 获取服务器URL
     * @return 服务器URL
     */
    String getServerUrl();

    /**
     * 获取用户名
     * @return 用户名
     */
    String getUsername();

    /**
     * 获取密码
     * @return 密码
     */
    String getPassword();

    /**
     * 获取数据库名称
     * @return 数据库名称
     */
    String getDatabaseName();

    /**
     * 获取连接超时时间（毫秒）
     * @return 连接超时时间，默认30秒
     */
    default int getConnectionTimeoutMs() {
        return 30000;
    }

    /**
     * 获取请求超时时间（毫秒）
     * @return 请求超时时间，默认60秒
     */
    default int getRequestTimeoutMs() {
        return 60000;
    }

}
