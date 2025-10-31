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

package com.alibaba.langengine.googlemaps.sdk;

import java.util.concurrent.TimeUnit;

public class GoogleMapsConstant {

    /**
     * Google Maps API base URL
     */
    public static final String GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api";

    /**
     * Default timeout for HTTP requests
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Default timeout time unit
     */
    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * Geocoding API endpoint
     */
    public static final String GEOCODING_API_ENDPOINT = "/geocode/json";

    /**
     * Places API endpoint for text search
     */
    public static final String PLACES_TEXT_SEARCH_ENDPOINT = "/place/textsearch/json";

    /**
     * Places API endpoint for nearby search
     */
    public static final String PLACES_NEARBY_SEARCH_ENDPOINT = "/place/nearbysearch/json";

    /**
     * Directions API endpoint
     */
    public static final String DIRECTIONS_API_ENDPOINT = "/directions/json";

    /**
     * Distance Matrix API endpoint
     */
    public static final String DISTANCE_MATRIX_API_ENDPOINT = "/distancematrix/json";

    /**
     * Elevation API endpoint
     */
    public static final String ELEVATION_API_ENDPOINT = "/elevation/json";

    /**
     * Time Zone API endpoint
     */
    public static final String TIME_ZONE_API_ENDPOINT = "/timezone/json";

    /**
     * Roads API endpoint for snap to roads
     */
    public static final String ROADS_SNAP_TO_ROADS_ENDPOINT = "/snapToRoads";

    /**
     * Roads API endpoint for speed limits
     */
    public static final String ROADS_SPEED_LIMITS_ENDPOINT = "/speedLimits";

    /**
     * Roads API endpoint for nearest roads
     */
    public static final String ROADS_NEAREST_ROADS_ENDPOINT = "/nearestRoads";

    /**
     * Static Maps API endpoint
     */
    public static final String STATIC_MAPS_API_ENDPOINT = "/staticmap";

    /**
     * Street View Static API endpoint
     */
    public static final String STREET_VIEW_STATIC_API_ENDPOINT = "/streetview";

    /**
     * Places API endpoint for place details
     */
    public static final String PLACES_DETAILS_ENDPOINT = "/place/details/json";

    /**
     * Places API endpoint for place photos
     */
    public static final String PLACES_PHOTOS_ENDPOINT = "/place/photo";

    /**
     * Places API endpoint for autocomplete
     */
    public static final String PLACES_AUTOCOMPLETE_ENDPOINT = "/place/autocomplete/json";

    /**
     * Places API endpoint for query autocomplete
     */
    public static final String PLACES_QUERY_AUTOCOMPLETE_ENDPOINT = "/place/queryautocomplete/json";

    /**
     * Maximum number of waypoints for Directions API
     */
    public static final int MAX_WAYPOINTS = 25;

    /**
     * Maximum number of origins/destinations for Distance Matrix API
     */
    public static final int MAX_DISTANCE_MATRIX_ELEMENTS = 100;

    /**
     * Maximum number of coordinates for Roads API
     */
    public static final int MAX_ROADS_COORDINATES = 100;

    /**
     * Maximum number of coordinates for Elevation API
     */
    public static final int MAX_ELEVATION_COORDINATES = 512;

    /**
     * Maximum number of coordinates for Time Zone API
     */
    public static final int MAX_TIME_ZONE_COORDINATES = 100;
}
