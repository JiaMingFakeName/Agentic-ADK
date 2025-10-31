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
package com.alibaba.langengine.momento;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class MomentoConfiguration {

    /**
     * Momento auth token
     */
    public static String MOMENTO_AUTH_TOKEN = WorkPropertiesUtils.get("momento_auth_token");

    /**
     * Momento cache name
     */
    public static String MOMENTO_CACHE_NAME = WorkPropertiesUtils.get("momento_cache_name");

    /**
     * Momento index name
     */
    public static String MOMENTO_INDEX_NAME = WorkPropertiesUtils.get("momento_index_name");

}
