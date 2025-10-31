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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

import static com.alibaba.langengine.openstreetmap.sdk.OpenStreetMapConstant.NOMINATIM_BASE_URL;

public class OpenStreetMapConfiguration {

    /**
     * OpenStreetMap Nominatim API base URL
     */
    public static String NOMINATIM_API_URL = WorkPropertiesUtils.get("nominatim_api_url", NOMINATIM_BASE_URL);

    /**
     * OpenStreetMap Nominatim API email for identification
     */
    public static String NOMINATIM_EMAIL = WorkPropertiesUtils.get("nominatim_email");

    /**
     * OpenStreetMap Nominatim API language preference
     */
    public static String NOMINATIM_LANGUAGE = WorkPropertiesUtils.get("nominatim_language", "en");

    /**
     * OpenStreetMap Nominatim API country codes filter
     */
    public static String NOMINATIM_COUNTRY_CODES = WorkPropertiesUtils.get("nominatim_country_codes");

    /**
     * OpenStreetMap Nominatim API user agent
     */
    public static String NOMINATIM_USER_AGENT = WorkPropertiesUtils.get("nominatim_user_agent", "AliLangEngine/1.0");
}
