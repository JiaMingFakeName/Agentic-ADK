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
 * Base request for OpenStreetMap Nominatim API
 */
public class NominatimRequest {

    @JsonProperty("q")
    private String query;

    @JsonProperty("format")
    private String format = "json";

    @JsonProperty("addressdetails")
    private String addressDetails = "1";

    @JsonProperty("limit")
    private String limit;

    @JsonProperty("countrycodes")
    private String countryCodes;

    @JsonProperty("accept-language")
    private String acceptLanguage;

    @JsonProperty("email")
    private String email;

    @JsonProperty("extratags")
    private String extraTags;

    @JsonProperty("namedetails")
    private String nameDetails;

    @JsonProperty("dedupe")
    private String dedupe;

    @JsonProperty("bounded")
    private String bounded;

    @JsonProperty("viewbox")
    private String viewBox;

    @JsonProperty("polygon_geojson")
    private String polygonGeojson;

    @JsonProperty("polygon_kml")
    private String polygonKml;

    @JsonProperty("polygon_svg")
    private String polygonSvg;

    @JsonProperty("polygon_text")
    private String polygonText;

    public NominatimRequest() {
    }

    public NominatimRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(String countryCodes) {
        this.countryCodes = countryCodes;
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

    public String getDedupe() {
        return dedupe;
    }

    public void setDedupe(String dedupe) {
        this.dedupe = dedupe;
    }

    public String getBounded() {
        return bounded;
    }

    public void setBounded(String bounded) {
        this.bounded = bounded;
    }

    public String getViewBox() {
        return viewBox;
    }

    public void setViewBox(String viewBox) {
        this.viewBox = viewBox;
    }

    public String getPolygonGeojson() {
        return polygonGeojson;
    }

    public void setPolygonGeojson(String polygonGeojson) {
        this.polygonGeojson = polygonGeojson;
    }

    public String getPolygonKml() {
        return polygonKml;
    }

    public void setPolygonKml(String polygonKml) {
        this.polygonKml = polygonKml;
    }

    public String getPolygonSvg() {
        return polygonSvg;
    }

    public void setPolygonSvg(String polygonSvg) {
        this.polygonSvg = polygonSvg;
    }

    public String getPolygonText() {
        return polygonText;
    }

    public void setPolygonText(String polygonText) {
        this.polygonText = polygonText;
    }
}
