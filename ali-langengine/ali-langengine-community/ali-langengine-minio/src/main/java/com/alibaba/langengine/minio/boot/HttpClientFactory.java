/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.minio.boot;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Factory for creating OkHttpClient instances for MinIO.
 */
final class HttpClientFactory {

    private HttpClientFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Builds an OkHttpClient with tuned settings for MinIO.
     *
     * @param cfg The HTTP configuration properties.
     * @return A configured OkHttpClient instance.
     */
    static OkHttpClient build(MinioProperties.Http cfg) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(cfg.getMaxRequests());
        dispatcher.setMaxRequestsPerHost(cfg.getMaxRequestsPerHost());

        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(cfg.getConnectionPoolMaxIdle(),
                        cfg.getConnectionPoolKeepAliveMinutes(), TimeUnit.MINUTES))
                .connectTimeout(cfg.getConnectTimeoutSec(), TimeUnit.SECONDS)
                .readTimeout(cfg.getReadTimeoutSec(), TimeUnit.SECONDS)
                .writeTimeout(cfg.getWriteTimeoutSec(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        if (cfg.getPingIntervalSec() != null && cfg.getPingIntervalSec() > 0) {
            b.pingInterval(cfg.getPingIntervalSec(), TimeUnit.SECONDS);
        }
        return b.build();
    }
}