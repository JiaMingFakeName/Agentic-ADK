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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

import static com.alibaba.langengine.googlemaps.sdk.GoogleMapsConstant.GOOGLE_MAPS_BASE_URL;

public class GoogleMapsConfiguration {

    /**
     * Google Maps API key, retrieved from work properties
     */
    public static String GOOGLE_MAPS_API_KEY = WorkPropertiesUtils.get("google_maps_api_key");

    /**
     * Google Maps API base URL, defaults to the constant BASE_URL if not configured
     */
    public static String GOOGLE_MAPS_API_URL = WorkPropertiesUtils.get("google_maps_api_url", GOOGLE_MAPS_BASE_URL);

    /**
     * Google Maps API region code (e.g., "us", "cn")
     */
    public static String GOOGLE_MAPS_REGION = WorkPropertiesUtils.get("google_maps_region", "us");

    /**
     * Google Maps API language code (e.g., "en", "zh-CN")
     */
    public static String GOOGLE_MAPS_LANGUAGE = WorkPropertiesUtils.get("google_maps_language", "en");
}
