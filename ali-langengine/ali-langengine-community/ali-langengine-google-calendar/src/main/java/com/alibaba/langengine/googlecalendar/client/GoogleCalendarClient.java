/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.googlecalendar.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.googlecalendar.GoogleCalendarConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class GoogleCalendarClient {

    private final GoogleCalendarConfiguration config;
    private final HttpClient httpClient;

    public GoogleCalendarClient(GoogleCalendarConfiguration config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
    }

    public String listEvents(String calendarId, String timeMin, String timeMax, int maxResults) throws Exception {
        StringBuilder url = new StringBuilder("https://www.googleapis.com/calendar/v3/calendars/")
                .append(calendarId).append("/events?");
        if (timeMin != null) url.append("timeMin=").append(java.net.URLEncoder.encode(timeMin, "UTF-8")).append("&");
        if (timeMax != null) url.append("timeMax=").append(java.net.URLEncoder.encode(timeMax, "UTF-8")).append("&");
        if (maxResults > 0) url.append("maxResults=").append(maxResults).append("&");
        url.append("singleEvents=true&orderBy=startTime");

        return doGet(url.toString());
    }

    public String createEvent(String calendarId, JSONObject event) throws Exception {
        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events";
        return doPost(url, event.toJSONString());
    }

    public String updateEvent(String calendarId, String eventId, JSONObject event) throws Exception {
        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events/" + eventId;
        return doPatch(url, event.toJSONString());
    }

    public void deleteEvent(String calendarId, String eventId) throws Exception {
        String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events/" + eventId;
        doDelete(url);
    }

    private String doGet(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "Bearer " + config.getAccessToken());
        request.setHeader("Accept", "application/json");
        HttpResponse response = httpClient.execute(request);
        int sc = response.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
        String err = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("Google Calendar GET failed: " + err);
    }

    private String doPost(String url, String body) throws Exception {
        HttpPost request = new HttpPost(url);
        request.setHeader("Authorization", "Bearer " + config.getAccessToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse response = httpClient.execute(request);
        int sc = response.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
        String err = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("Google Calendar POST failed: " + err);
    }

    private String doPatch(String url, String body) throws Exception {
        HttpPatch request = new HttpPatch(url);
        request.setHeader("Authorization", "Bearer " + config.getAccessToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse response = httpClient.execute(request);
        int sc = response.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
        String err = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("Google Calendar PATCH failed: " + err);
    }

    private void doDelete(String url) throws Exception {
        HttpDelete request = new HttpDelete(url);
        request.setHeader("Authorization", "Bearer " + config.getAccessToken());
        HttpResponse response = httpClient.execute(request);
        int sc = response.getStatusLine().getStatusCode();
        if (sc < 200 || sc >= 300) {
            String err = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            throw new RuntimeException("Google Calendar DELETE failed: " + err);
        }
    }
}


