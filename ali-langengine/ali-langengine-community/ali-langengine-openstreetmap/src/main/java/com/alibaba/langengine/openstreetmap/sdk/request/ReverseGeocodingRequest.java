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

package com.alibaba.langengine.openstreetmap.sdk.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request for OpenStreetMap Reverse Geocoding API
 */
public class ReverseGeocodingRequest {

    @JsonProperty("lat")
    private String latitude;

    @JsonProperty("lon")
    private String longitude;

    @JsonProperty("format")
    private String format = "json";

    @JsonProperty("addressdetails")
    private String addressDetails = "1";

    @JsonProperty("accept-language")
    private String acceptLanguage;

    @JsonProperty("email")
    private String email;

    @JsonProperty("extratags")
    private String extraTags;

    @JsonProperty("namedetails")
    private String nameDetails;

    @JsonProperty("zoom")
    private String zoom;

    public ReverseGeocodingRequest() {
    }

    public ReverseGeocodingRequest(double lat, double lon) {
        this.latitude = String.valueOf(lat);
        this.longitude = String.valueOf(lon);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExtraTags() {
        return extraTags;
    }

    public void setExtraTags(String extraTags) {
        this.extraTags = extraTags;
    }

    public String getNameDetails() {
        return nameDetails;
    }

    public void setNameDetails(String nameDetails) {
        this.nameDetails = nameDetails;
    }

    public String getZoom() {
        return zoom;
    }

    public void setZoom(String zoom) {
        this.zoom = zoom;
    }
}
