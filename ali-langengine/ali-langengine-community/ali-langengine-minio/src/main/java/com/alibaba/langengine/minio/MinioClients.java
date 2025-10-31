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

package com.alibaba.langengine.minio;

import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * A utility class for creating and configuring MinioClient instances.
 * <p>
 * This class provides convenient factory methods for instantiating {@link MinioClient}
 * with pre-configured settings. It includes a method to create a finely-tuned
 * {@link OkHttpClient} optimized for high-throughput S3 operations, and another
 * method to construct a {@code MinioClient} directly from environment variables,
 * simplifying configuration in containerized or cloud environments.
 * </p>
 */
public final class MinioClients {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MinioClients() {}

    /**
     * Creates a new {@link OkHttpClient} with settings optimized for high performance
     * communication with a MinIO or S3-compatible service.
     * <p>
     * The returned client is configured with a specific dispatcher and connection pool
     * settings, including max requests, timeouts, and connection reuse policies,
     * to ensure efficient resource utilization and reliable performance under load.
     * </p>
     *
     * @return A pre-configured {@code OkHttpClient} instance.
     */
    public static OkHttpClient tunedHttpClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(256);
        dispatcher.setMaxRequestsPerHost(64);

        return new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(64, 5, TimeUnit.MINUTES))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Creates a {@link MinioClient} instance configured from environment variables.
     * <p>
     * This method reads connection details such as endpoint, credentials, and region
     * from standard environment variables (e.g., {@code MINIO_ENDPOINT},
     * {@code MINIO_ACCESS_KEY}). It provides sensible defaults for common settings.
     * This is the preferred way to initialize the client in automated or
     * containerized environments. It internally uses the {@link #tunedHttpClient()}
     * for optimal performance.
     * </p>
     *
     * @return A new {@code MinioClient} instance.
     */
    public static MinioClient fromEnv() {
        String endpoint = getenv("MINIO_ENDPOINT", "play.min.io");
        String accessKey = getenv("MINIO_ACCESS_KEY", null);
        String secretKey = getenv("MINIO_SECRET_KEY", null);
        boolean secure = Boolean.parseBoolean(getenv("MINIO_SECURE", "true"));
        String region = getenv("MINIO_REGION", null);

        MinioClient.Builder b = MinioClient.builder()
                .endpoint((secure ? "https://" : "http://") + endpoint)
                .httpClient(tunedHttpClient());

        if (accessKey != null && secretKey != null) {
            b.credentials(accessKey, secretKey);
        }
        if (region != null) {
            b.region(region);
        }
        return b.build();
    }

    /**
     * A helper method to retrieve an environment variable with a default value.
     *
     * @param key The name of the environment variable.
     * @param def The default value to return if the variable is not set.
     * @return The value of the environment variable or the default value.
     */
    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return v == null ? def : v;
    }
}