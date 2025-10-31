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
package com.alibaba.langengine.proxima.vectorstore.service;

import com.alibaba.langengine.proxima.vectorstore.ProximaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.time.Duration;


@Slf4j
public class ProximaClient {

    private final OkHttpClient httpClient;
    private final String serverUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public ProximaClient(String serverUrl, String apiKey, Duration timeout) {
        this.serverUrl = serverUrl;
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .build();
    }

    public void insertDocuments(ProximaInsertRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(serverUrl + "/api/v1/documents")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new ProximaException("PROXIMA_INSERT_ERROR", 
                            "Failed to insert documents: " + response.message());
                }
            }
        } catch (IOException e) {
            throw new ProximaException("PROXIMA_IO_ERROR", "IO error during insert", e);
        }
    }

    public ProximaQueryResponse queryDocuments(ProximaQueryRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(serverUrl + "/api/v1/query")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new ProximaException("PROXIMA_QUERY_ERROR", 
                            "Failed to query documents: " + response.message());
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new ProximaException("PROXIMA_QUERY_ERROR", "Response body is null");
                }
                
                String responseBodyStr = responseBody.string();
                return objectMapper.readValue(responseBodyStr, ProximaQueryResponse.class);
            }
        } catch (IOException e) {
            throw new ProximaException("PROXIMA_IO_ERROR", "IO error during query", e);
        }
    }

    public void close() {
        if (httpClient != null) {
            try {
                httpClient.dispatcher().executorService().shutdown();
                if (!httpClient.dispatcher().executorService().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    httpClient.dispatcher().executorService().shutdownNow();
                }
            } catch (InterruptedException e) {
                httpClient.dispatcher().executorService().shutdownNow();
                Thread.currentThread().interrupt();
            }
            httpClient.connectionPool().evictAll();
        }
    }
}