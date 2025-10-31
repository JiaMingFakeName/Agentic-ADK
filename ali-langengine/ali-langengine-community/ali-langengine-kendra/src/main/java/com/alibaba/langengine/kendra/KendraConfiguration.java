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
package com.alibaba.langengine.kendra;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class KendraConfiguration {

    /**
     * AWS access key for Kendra service
     */
    public static String KENDRA_ACCESS_KEY = WorkPropertiesUtils.get("kendra_access_key");

    /**
     * AWS secret key for Kendra service
     */
    public static String KENDRA_SECRET_KEY = WorkPropertiesUtils.get("kendra_secret_key");

    /**
     * AWS region for Kendra service
     */
    public static String KENDRA_REGION = WorkPropertiesUtils.get("kendra_region", "us-east-1");

    /**
     * Kendra default index ID
     */
    public static String KENDRA_DEFAULT_INDEX = WorkPropertiesUtils.get("kendra_default_index");

    /**
     * Connection timeout in milliseconds
     */
    public static String KENDRA_CONNECTION_TIMEOUT = WorkPropertiesUtils.get("kendra_connection_timeout", "30000");

    /**
     * Read timeout in milliseconds
     */
    public static String KENDRA_READ_TIMEOUT = WorkPropertiesUtils.get("kendra_read_timeout", "60000");

}