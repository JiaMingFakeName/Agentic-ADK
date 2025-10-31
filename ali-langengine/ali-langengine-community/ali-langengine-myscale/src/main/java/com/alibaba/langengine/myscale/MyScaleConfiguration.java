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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class MyScaleConfiguration {

    /**
     * MyScale服务器URL
     */
    public static String MYSCALE_SERVER_URL = WorkPropertiesUtils.get("myscale_server_url", "http://localhost:8123");

    /**
     * MyScale用户名
     */
    public static String MYSCALE_USERNAME = WorkPropertiesUtils.get("myscale_username", "default");

    /**
     * MyScale密码
     */
    public static String MYSCALE_PASSWORD = WorkPropertiesUtils.get("myscale_password", "");

    /**
     * MyScale数据库名
     */
    public static String MYSCALE_DATABASE = WorkPropertiesUtils.get("myscale_database", "default");

    /**
     * 连接超时时间（毫秒）
     */
    public static int MYSCALE_CONNECTION_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("myscale_connection_timeout", "30000"));

    /**
     * 读取超时时间（毫秒）
     */
    public static int MYSCALE_READ_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("myscale_read_timeout", "60000"));

}