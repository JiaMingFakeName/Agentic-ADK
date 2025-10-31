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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the MinIO client, mapped from application properties
 * files (e.g., {@code application.yml}) with the prefix {@code com.alibaba.langengine.minio}.
 * <p>
 * This class encapsulates all the settings required to configure the connection
 * and behavior of the MinIO client, including endpoint details, credentials,
 * and HTTP client tuning parameters.
 * </p>
 */
@ConfigurationProperties(prefix = "com.alibaba.langengine.minio")
public class MinioProperties {

    /**
     * The network endpoint of the MinIO or S3-compatible service.
     * It can be a domain name or an IP address, with an optional port.
     * Example: "play.min.io" or "127.0.0.1:9000". Do not include the protocol (http/https).
     */
    private String endpoint = "play.min.io";

    /**
     * The access key (equivalent to a username) for authenticating with the MinIO service.
     */
    private String accessKey;

    /**
     * The secret key (equivalent to a password) for authenticating with the MinIO service.
     */
    private String secretKey;

    /**
     * The session token, a temporary credential typically used with AWS STS. Optional.
     */
    private String sessionToken;

    /**
     * A flag to determine whether to use a secure (HTTPS) connection. Defaults to {@code true}.
     */
    private Boolean secure = true;

    /**
     * The specific S3 region to connect to. Optional.
     */
    private String region;

    /**
     * The default part size in bytes for multipart uploads. Defaults to 8 MiB.
     * If a value less than 5 MiB is provided, it will be automatically adjusted to 5 MiB
     * to comply with S3 protocol requirements.
     */
    private long partSize = 8L * 1024 * 1024;

    /**
     * Encapsulates advanced tuning settings for the underlying OkHttpClient.
     */
    private Http http = new Http();

    /**
     * A nested static class to hold HTTP client-specific configuration properties.
     */
    public static class Http {

        private int maxRequests = 256;
        private int maxRequestsPerHost = 64;
        private int connectTimeoutSec = 5;
        private int readTimeoutSec = 120;
        private int writeTimeoutSec = 120;
        private int connectionPoolMaxIdle = 64;
        private int connectionPoolKeepAliveMinutes = 5;
        private Integer pingIntervalSec;

        /**
         * @return The maximum number of concurrent requests across all hosts.
         */
        public int getMaxRequests() {
            return maxRequests;
        }

        /**
         * @param v The maximum number of concurrent requests across all hosts.
         */
        public void setMaxRequests(int v) {
            maxRequests = v;
        }

        /**
         * @return The maximum number of concurrent requests to any single host.
         */
        public int getMaxRequestsPerHost() {
            return maxRequestsPerHost;
        }

        /**
         * @param v The maximum number of concurrent requests to any single host.
         */
        public void setMaxRequestsPerHost(int v) {
            maxRequestsPerHost = v;
        }

        /**
         * @return The timeout in seconds for establishing a new connection.
         */
        public int getConnectTimeoutSec() {
            return connectTimeoutSec;
        }

        /**
         * @param v The timeout in seconds for establishing a new connection.
         */
        public void setConnectTimeoutSec(int v) {
            connectTimeoutSec = v;
        }

        /**
         * @return The timeout in seconds for reading data from a connection.
         */
        public int getReadTimeoutSec() {
            return readTimeoutSec;
        }

        /**
         * @param v The timeout in seconds for reading data from a connection.
         */
        public void setReadTimeoutSec(int v) {
            readTimeoutSec = v;
        }

        /**
         * @return The timeout in seconds for writing data to a connection.
         */
        public int getWriteTimeoutSec() {
            return writeTimeoutSec;
        }

        /**
         * @param v The timeout in seconds for writing data to a connection.
         */
        public void setWriteTimeoutSec(int v) {
            writeTimeoutSec = v;
        }

        /**
         * @return The maximum number of idle connections to keep in the connection pool.
         */
        public int getConnectionPoolMaxIdle() {
            return connectionPoolMaxIdle;
        }

        /**
         * @param v The maximum number of idle connections to keep in the connection pool.
         */
        public void setConnectionPoolMaxIdle(int v) {
            connectionPoolMaxIdle = v;
        }

        /**
         * @return The keep-alive duration in minutes for idle connections in the pool.
         */
        public int getConnectionPoolKeepAliveMinutes() {
            return connectionPoolKeepAliveMinutes;
        }

        /**
         * @param v The keep-alive duration in minutes for idle connections in the pool.
         */
        public void setConnectionPoolKeepAliveMinutes(int v) {
            connectionPoolKeepAliveMinutes = v;
        }

        /**
         * @return The interval in seconds for sending ping frames to keep connections alive.
         */
        public Integer getPingIntervalSec() {
            return pingIntervalSec;
        }

        /**
         * @param v The interval in seconds for sending ping frames to keep connections alive.
         */
        public void setPingIntervalSec(Integer v) {
            pingIntervalSec = v;
        }
    }

    /**
     * @return The network endpoint of the MinIO service.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param v The network endpoint of the MinIO service.
     */
    public void setEndpoint(String v) {
        endpoint = v;
    }

    /**
     * @return The access key for authentication.
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param v The access key for authentication.
     */
    public void setAccessKey(String v) {
        accessKey = v;
    }

    /**
     * @return The secret key for authentication.
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param v The secret key for authentication.
     */
    public void setSecretKey(String v) {
        secretKey = v;
    }

    /**
     * @return The session token for temporary credentials.
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * @param v The session token for temporary credentials.
     */
    public void setSessionToken(String v) {
        sessionToken = v;
    }

    /**
     * @return The flag for using a secure (HTTPS) connection.
     */
    public Boolean getSecure() {
        return secure;
    }

    /**
     * @param v The flag for using a secure (HTTPS) connection.
     */
    public void setSecure(Boolean v) {
        secure = v;
    }

    /**
     * @return The specific S3 region to connect to.
     */
    public String getRegion() {
        return region;
    }

    /**
     * @param v The specific S3 region to connect to.
     */
    public void setRegion(String v) {
        region = v;
    }

    /**
     * @return The default part size in bytes for multipart uploads.
     */
    public long getPartSize() {
        return partSize;
    }

    /**
     * @param v The default part size in bytes for multipart uploads.
     */
    public void setPartSize(long v) {
        partSize = v;
    }

    /**
     * @return The nested HTTP client configuration.
     */
    public Http getHttp() {
        return http;
    }

    /**
     * @param v The nested HTTP client configuration.
     */
    public void setHttp(Http v) {
        http = v;
    }
}