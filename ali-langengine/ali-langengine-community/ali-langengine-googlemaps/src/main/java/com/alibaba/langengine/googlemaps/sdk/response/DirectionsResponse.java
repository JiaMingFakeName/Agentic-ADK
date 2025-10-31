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

package com.alibaba.langengine.googlemaps.sdk.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from Google Maps Directions API
 */
public class DirectionsResponse {

    @JsonProperty("routes")
    private List<Route> routes;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("geocoded_waypoints")
    private List<GeocodedWaypoint> geocodedWaypoints;

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<GeocodedWaypoint> getGeocodedWaypoints() {
        return geocodedWaypoints;
    }

    public void setGeocodedWaypoints(List<GeocodedWaypoint> geocodedWaypoints) {
        this.geocodedWaypoints = geocodedWaypoints;
    }

    public static class Route {
        @JsonProperty("bounds")
        private Bounds bounds;

        @JsonProperty("copyrights")
        private String copyrights;

        @JsonProperty("legs")
        private List<Leg> legs;

        @JsonProperty("overview_polyline")
        private OverviewPolyline overviewPolyline;

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("warnings")
        private List<String> warnings;

        @JsonProperty("waypoint_order")
        private List<Integer> waypointOrder;

        public Bounds getBounds() {
            return bounds;
        }

        public void setBounds(Bounds bounds) {
            this.bounds = bounds;
        }

        public String getCopyrights() {
            return copyrights;
        }

        public void setCopyrights(String copyrights) {
            this.copyrights = copyrights;
        }

        public List<Leg> getLegs() {
            return legs;
        }

        public void setLegs(List<Leg> legs) {
            this.legs = legs;
        }

        public OverviewPolyline getOverviewPolyline() {
            return overviewPolyline;
        }

        public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
            this.overviewPolyline = overviewPolyline;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public List<Integer> getWaypointOrder() {
            return waypointOrder;
        }

        public void setWaypointOrder(List<Integer> waypointOrder) {
            this.waypointOrder = waypointOrder;
        }
    }

    public static class Leg {
        @JsonProperty("distance")
        private Distance distance;

        @JsonProperty("duration")
        private Duration duration;

        @JsonProperty("duration_in_traffic")
        private Duration durationInTraffic;

        @JsonProperty("end_address")
        private String endAddress;

        @JsonProperty("end_location")
        private Location endLocation;

        @JsonProperty("start_address")
        private String startAddress;

        @JsonProperty("start_location")
        private Location startLocation;

        @JsonProperty("steps")
        private List<Step> steps;

        @JsonProperty("traffic_speed_entry")
        private List<TrafficSpeedEntry> trafficSpeedEntry;

        @JsonProperty("via_waypoint")
        private List<ViaWaypoint> viaWaypoint;

        public Distance getDistance() {
            return distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public Duration getDurationInTraffic() {
            return durationInTraffic;
        }

        public void setDurationInTraffic(Duration durationInTraffic) {
            this.durationInTraffic = durationInTraffic;
        }

        public String getEndAddress() {
            return endAddress;
        }

        public void setEndAddress(String endAddress) {
            this.endAddress = endAddress;
        }

        public Location getEndLocation() {
            return endLocation;
        }

        public void setEndLocation(Location endLocation) {
            this.endLocation = endLocation;
        }

        public String getStartAddress() {
            return startAddress;
        }

        public void setStartAddress(String startAddress) {
            this.startAddress = startAddress;
        }

        public Location getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(Location startLocation) {
            this.startLocation = startLocation;
        }

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }

        public List<TrafficSpeedEntry> getTrafficSpeedEntry() {
            return trafficSpeedEntry;
        }

        public void setTrafficSpeedEntry(List<TrafficSpeedEntry> trafficSpeedEntry) {
            this.trafficSpeedEntry = trafficSpeedEntry;
        }

        public List<ViaWaypoint> getViaWaypoint() {
            return viaWaypoint;
        }

        public void setViaWaypoint(List<ViaWaypoint> viaWaypoint) {
            this.viaWaypoint = viaWaypoint;
        }
    }

    public static class Step {
        @JsonProperty("distance")
        private Distance distance;

        @JsonProperty("duration")
        private Duration duration;

        @JsonProperty("end_location")
        private Location endLocation;

        @JsonProperty("html_instructions")
        private String htmlInstructions;

        @JsonProperty("polyline")
        private OverviewPolyline polyline;

        @JsonProperty("start_location")
        private Location startLocation;

        @JsonProperty("travel_mode")
        private String travelMode;

        @JsonProperty("maneuver")
        private String maneuver;

        public Distance getDistance() {
            return distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public Location getEndLocation() {
            return endLocation;
        }

        public void setEndLocation(Location endLocation) {
            this.endLocation = endLocation;
        }

        public String getHtmlInstructions() {
            return htmlInstructions;
        }

        public void setHtmlInstructions(String htmlInstructions) {
            this.htmlInstructions = htmlInstructions;
        }

        public OverviewPolyline getPolyline() {
            return polyline;
        }

        public void setPolyline(OverviewPolyline polyline) {
            this.polyline = polyline;
        }

        public Location getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(Location startLocation) {
            this.startLocation = startLocation;
        }

        public String getTravelMode() {
            return travelMode;
        }

        public void setTravelMode(String travelMode) {
            this.travelMode = travelMode;
        }

        public String getManeuver() {
            return maneuver;
        }

        public void setManeuver(String maneuver) {
            this.maneuver = maneuver;
        }
    }

    public static class Distance {
        @JsonProperty("text")
        private String text;

        @JsonProperty("value")
        private long value;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class Duration {
        @JsonProperty("text")
        private String text;

        @JsonProperty("value")
        private long value;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class Location {
        @JsonProperty("lat")
        private double lat;

        @JsonProperty("lng")
        private double lng;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }

    public static class Bounds {
        @JsonProperty("northeast")
        private Location northeast;

        @JsonProperty("southwest")
        private Location southwest;

        public Location getNortheast() {
            return northeast;
        }

        public void setNortheast(Location northeast) {
            this.northeast = northeast;
        }

        public Location getSouthwest() {
            return southwest;
        }

        public void setSouthwest(Location southwest) {
            this.southwest = southwest;
        }
    }

    public static class OverviewPolyline {
        @JsonProperty("points")
        private String points;

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
    }

    public static class GeocodedWaypoint {
        @JsonProperty("geocoder_status")
        private String geocoderStatus;

        @JsonProperty("place_id")
        private String placeId;

        @JsonProperty("types")
        private List<String> types;

        public String getGeocoderStatus() {
            return geocoderStatus;
        }

        public void setGeocoderStatus(String geocoderStatus) {
            this.geocoderStatus = geocoderStatus;
        }

        public String getPlaceId() {
            return placeId;
        }

        public void setPlaceId(String placeId) {
            this.placeId = placeId;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }
    }

    public static class TrafficSpeedEntry {
        @JsonProperty("offset_meters")
        private int offsetMeters;

        @JsonProperty("speed")
        private int speed;

        public int getOffsetMeters() {
            return offsetMeters;
        }

        public void setOffsetMeters(int offsetMeters) {
            this.offsetMeters = offsetMeters;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }
    }

    public static class ViaWaypoint {
        @JsonProperty("location")
        private Location location;

        @JsonProperty("step_index")
        private int stepIndex;

        @JsonProperty("step_interpolation")
        private double stepInterpolation;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public int getStepIndex() {
            return stepIndex;
        }

        public void setStepIndex(int stepIndex) {
            this.stepIndex = stepIndex;
        }

        public double getStepInterpolation() {
            return stepInterpolation;
        }

        public void setStepInterpolation(double stepInterpolation) {
            this.stepInterpolation = stepInterpolation;
        }
    }
}
