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
package com.alibaba.langengine.momento.vectorstore;

import com.alibaba.langengine.momento.MomentoException;
import momento.sdk.CacheClient;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.config.Configurations;
import momento.sdk.exceptions.AlreadyExistsException;
import momento.sdk.exceptions.SdkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;


public class MomentoClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(MomentoClient.class);

    private final CacheClient cacheClient;

    public MomentoClient(MomentoParam momentoParam) {
        try {
            CredentialProvider credentialProvider = CredentialProvider.fromString(momentoParam.getAuthToken());

            this.cacheClient = CacheClient.builder(
                            credentialProvider,
                            Configurations.Laptop.v1(), // A good default for development
                            Duration.ofSeconds(momentoParam.getDefaultTtlSeconds()))
                    .build();

            // Ensure cache exists upon initialization
            createCacheIfNotExist(momentoParam.getCacheName());

        } catch (SdkException e) {
            log.error("Failed to initialize Momento client due to an SDK error. Please check credentials and configuration.", e);
            throw new MomentoException(e.getErrorCode().name(), "Failed to initialize Momento client", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during Momento client initialization.", e);
            throw new MomentoException("UNEXPECTED_ERROR", "An unexpected error occurred during client initialization", e);
        }
    }

    private void createCacheIfNotExist(String cacheName) {
        try {
            cacheClient.createCache(cacheName).join();
            log.info("Momento cache '{}' created successfully.", cacheName);
        } catch (AlreadyExistsException e) {
            log.info("Momento cache '{}' already exists.", cacheName);
        } catch (SdkException e) {
            log.error("Failed to create cache '{}'", cacheName, e);
            throw new MomentoException(e.getErrorCode().name(), "Failed to create cache: " + cacheName, e);
        }
    }

    public CacheClient getCacheClient() {
        return cacheClient;
    }

    @Override
    public void close() {
        if (cacheClient != null) {
            try {
                cacheClient.close();
                log.info("Momento CacheClient closed successfully.");
            } catch (Exception e) {
                log.error("Error closing Momento CacheClient", e);
            }
        }
    }
}
