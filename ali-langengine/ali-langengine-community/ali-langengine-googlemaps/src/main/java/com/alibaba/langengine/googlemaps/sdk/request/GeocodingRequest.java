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

package com.alibaba.langengine.googlemaps.sdk.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for Google Maps Geocoding API
 */
public class GeocodingRequest {

    @JsonProperty("address")
    private String address;

    @JsonProperty("latlng")
    private String latlng;

    @JsonProperty("components")
    private String components;

    @JsonProperty("bounds")
    private String bounds;

    @JsonProperty("region")
    private String region;

    @JsonProperty("language")
    private String language;

    @JsonProperty("result_type")
    private String resultType;

    @JsonProperty("location_type")
    private String locationType;

    public GeocodingRequest() {
    }

    public GeocodingRequest(String address) {
        this.address = address;
    }

    public GeocodingRequest(String lat, String lng) {
        this.latlng = lat + "," + lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getBounds() {
        return bounds;
    }

    public void setBounds(String bounds) {
        this.bounds = bounds;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }
}
