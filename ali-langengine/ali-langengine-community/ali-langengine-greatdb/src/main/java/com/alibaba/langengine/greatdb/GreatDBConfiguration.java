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
package com.alibaba.langengine.greatdb;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class GreatDBConfiguration {

    /**
     * GreatDB server url
     */
    public static String GREATDB_SERVER_URL = WorkPropertiesUtils.get("greatdb_server_url", "jdbc:mysql://localhost:3306/vector_db");

    /**
     * GreatDB username
     */
    public static String GREATDB_USERNAME = WorkPropertiesUtils.get("greatdb_username", "root");

    /**
     * GreatDB password
     */
    public static String GREATDB_PASSWORD = WorkPropertiesUtils.get("greatdb_password", "");

    /**
     * GreatDB connection pool size
     */
    public static int GREATDB_POOL_SIZE = Integer.parseInt(WorkPropertiesUtils.get("greatdb_pool_size", "10"));

}