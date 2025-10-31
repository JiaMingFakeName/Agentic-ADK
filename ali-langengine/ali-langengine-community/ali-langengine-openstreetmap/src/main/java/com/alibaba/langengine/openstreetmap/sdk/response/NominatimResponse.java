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

package com.alibaba.langengine.openstreetmap.sdk.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response from OpenStreetMap Nominatim API
 */
public class NominatimResponse {

    @JsonProperty("place_id")
    private Long placeId;

    @JsonProperty("licence")
    private String licence;

    @JsonProperty("osm_type")
    private String osmType;

    @JsonProperty("osm_id")
    private Long osmId;

    @JsonProperty("boundingbox")
    private List<String> boundingBox;

    @JsonProperty("lat")
    private String latitude;

    @JsonProperty("lon")
    private String longitude;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("class")
    private String placeClass;

    @JsonProperty("type")
    private String type;

    @JsonProperty("importance")
    private Double importance;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("extratags")
    private Map<String, String> extraTags;

    @JsonProperty("namedetails")
    private Map<String, String> nameDetails;

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    public String getOsmType() {
        return osmType;
    }

    public void setOsmType(String osmType) {
        this.osmType = osmType;
    }

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public List<String> getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(List<String> boundingBox) {
        this.boundingBox = boundingBox;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPlaceClass() {
        return placeClass;
    }

    public void setPlaceClass(String placeClass) {
        this.placeClass = placeClass;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getImportance() {
        return importance;
    }

    public void setImportance(Double importance) {
        this.importance = importance;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Map<String, String> getExtraTags() {
        return extraTags;
    }

    public void setExtraTags(Map<String, String> extraTags) {
        this.extraTags = extraTags;
    }

    public Map<String, String> getNameDetails() {
        return nameDetails;
    }

    public void setNameDetails(Map<String, String> nameDetails) {
        this.nameDetails = nameDetails;
    }

    public static class Address {
        @JsonProperty("house_number")
        private String houseNumber;

        @JsonProperty("road")
        private String road;

        @JsonProperty("neighbourhood")
        private String neighbourhood;

        @JsonProperty("suburb")
        private String suburb;

        @JsonProperty("city")
        private String city;

        @JsonProperty("county")
        private String county;

        @JsonProperty("state")
        private String state;

        @JsonProperty("postcode")
        private String postcode;

        @JsonProperty("country")
        private String country;

        @JsonProperty("country_code")
        private String countryCode;

        public String getHouseNumber() {
            return houseNumber;
        }

        public void setHouseNumber(String houseNumber) {
            this.houseNumber = houseNumber;
        }

        public String getRoad() {
            return road;
        }

        public void setRoad(String road) {
            this.road = road;
        }

        public String getNeighbourhood() {
            return neighbourhood;
        }

        public void setNeighbourhood(String neighbourhood) {
            this.neighbourhood = neighbourhood;
        }

        public String getSuburb() {
            return suburb;
        }

        public void setSuburb(String suburb) {
            this.suburb = suburb;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostcode() {
            return postcode;
        }

        public void setPostcode(String postcode) {
            this.postcode = postcode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }
    }
}
