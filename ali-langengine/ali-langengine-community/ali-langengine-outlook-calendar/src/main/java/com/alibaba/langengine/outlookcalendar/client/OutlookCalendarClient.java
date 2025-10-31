/**
 * Copyright (C) 2024 AIDC-AI
 */
package com.alibaba.langengine.outlookcalendar.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.outlookcalendar.OutlookCalendarConfiguration;
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
public class OutlookCalendarClient {

    private final OutlookCalendarConfiguration config;
    private final HttpClient httpClient;
    private String accessToken;

    public OutlookCalendarClient(OutlookCalendarConfiguration config) {
        this.config = config;
        this.httpClient = HttpClients.createDefault();
    }

    private String getAccessToken() throws Exception {
        if (accessToken != null) return accessToken;
        String url = "https://login.microsoftonline.com/" + config.getTenantId() + "/oauth2/v2.0/token";
        String body = "client_id=" + config.getClientId() +
                "&client_secret=" + config.getClientSecret() +
                "&grant_type=client_credentials" +
                "&scope=https://graph.microsoft.com/.default";
        HttpPost req = new HttpPost(url);
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse resp = httpClient.execute(req);
        int sc = resp.getStatusLine().getStatusCode();
        if (sc == 200) {
            String respBody = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            JSONObject json = JSON.parseObject(respBody);
            accessToken = json.getString("access_token");
            return accessToken;
        }
        String err = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("MS Graph token failed: " + err);
    }

    private String userPath() {
        return (config.getUserId() == null || config.getUserId().trim().isEmpty()) ? "/me" : "/users/" + config.getUserId();
    }

    public String listEvents(String startDateTime, String endDateTime) throws Exception {
        StringBuilder url = new StringBuilder("https://graph.microsoft.com/v1.0").append(userPath())
                .append("/calendarView?");
        if (startDateTime != null) url.append("startDateTime=").append(java.net.URLEncoder.encode(startDateTime, "UTF-8")).append("&");
        if (endDateTime != null) url.append("endDateTime=").append(java.net.URLEncoder.encode(endDateTime, "UTF-8"));
        return doGet(url.toString());
    }

    public String createEvent(JSONObject event) throws Exception {
        String url = "https://graph.microsoft.com/v1.0" + userPath() + "/events";
        return doPost(url, event.toJSONString());
    }

    public String updateEvent(String eventId, JSONObject event) throws Exception {
        String url = "https://graph.microsoft.com/v1.0" + userPath() + "/events/" + eventId;
        return doPatch(url, event.toJSONString());
    }

    public void deleteEvent(String eventId) throws Exception {
        String url = "https://graph.microsoft.com/v1.0" + userPath() + "/events/" + eventId;
        doDelete(url);
    }

    private String doGet(String url) throws Exception {
        HttpGet r = new HttpGet(url);
        r.setHeader("Authorization", "Bearer " + getAccessToken());
        r.setHeader("Accept", "application/json");
        HttpResponse resp = httpClient.execute(r);
        int sc = resp.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        String err = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("Graph GET failed: " + err);
    }

    private String doPost(String url, String body) throws Exception {
        HttpPost r = new HttpPost(url);
        r.setHeader("Authorization", "Bearer " + getAccessToken());
        r.setHeader("Content-Type", "application/json");
        r.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse resp = httpClient.execute(r);
        int sc = resp.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        String err = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("Graph POST failed: " + err);
    }

    private String doPatch(String url, String body) throws Exception {
        HttpPatch r = new HttpPatch(url);
        r.setHeader("Authorization", "Bearer " + getAccessToken());
        r.setHeader("Content-Type", "application/json");
        r.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        HttpResponse resp = httpClient.execute(r);
        int sc = resp.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) return EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        String err = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
        throw new RuntimeException("Graph PATCH failed: " + err);
    }

    private void doDelete(String url) throws Exception {
        HttpDelete r = new HttpDelete(url);
        r.setHeader("Authorization", "Bearer " + getAccessToken());
        HttpResponse resp = httpClient.execute(r);
        int sc = resp.getStatusLine().getStatusCode();
        if (sc < 200 || sc >= 300) {
            String err = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            throw new RuntimeException("Graph DELETE failed: " + err);
        }
    }
}


