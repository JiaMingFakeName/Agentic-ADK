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
package com.alibaba.langengine.kendra.vectorstore.client;

import com.alibaba.langengine.kendra.vectorstore.KendraConnectionException;
import com.alibaba.langengine.kendra.vectorstore.KendraIndexException;
import com.alibaba.langengine.kendra.vectorstore.KendraParam;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kendra.model.DescribeIndexRequest;
import software.amazon.awssdk.services.kendra.model.DescribeIndexResponse;
import software.amazon.awssdk.services.kendra.model.IndexStatus;

import java.time.Duration;


@Slf4j
public class KendraClient {

    private final KendraParam param;
    private final software.amazon.awssdk.services.kendra.KendraClient awsKendraClient;

    public KendraClient(KendraParam param) {
        this.param = param;

        try {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                param.getAccessKey(),
                param.getSecretKey()
            );

            ClientOverrideConfiguration.Builder configBuilder = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMillis(param.getConnectionTimeout()))
                .apiCallAttemptTimeout(Duration.ofMillis(param.getReadTimeout()));

            this.awsKendraClient = software.amazon.awssdk.services.kendra.KendraClient.builder()
                .region(Region.of(param.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .overrideConfiguration(configBuilder.build())
                .build();

            ensureIndexExists();

        } catch (Exception e) {
            log.error("Failed to initialize Kendra client", e);
            throw new KendraConnectionException("Failed to initialize Kendra client: " + e.getMessage(), e);
        }
    }

    /**
     * Get Kendra client
     */
    public software.amazon.awssdk.services.kendra.KendraClient getKendraClient() {
        return awsKendraClient;
    }

    /**
     * Ensure Kendra index exists and is active
     */
    private void ensureIndexExists() {
        try {
            if (!indexExists(param.getIndexId())) {
                throw new KendraIndexException("Kendra index does not exist: " + param.getIndexId());
            }

            if (!isIndexActive(param.getIndexId())) {
                throw new KendraIndexException("Kendra index is not active: " + param.getIndexId());
            }

            log.info("Kendra index verified: {}", param.getIndexId());

        } catch (Exception e) {
            if (e instanceof KendraIndexException) {
                throw e;
            }
            throw new KendraIndexException("Failed to verify index: " + e.getMessage(), e);
        }
    }

    /**
     * Check if index exists
     */
    public boolean indexExists(String indexId) {
        try {
            DescribeIndexRequest request = DescribeIndexRequest.builder()
                .id(indexId)
                .build();

            awsKendraClient.describeIndex(request);
            return true;

        } catch (software.amazon.awssdk.services.kendra.model.KendraException e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new KendraIndexException("Failed to check index existence: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new KendraIndexException("Failed to check index existence: " + e.getMessage(), e);
        }
    }

    /**
     * Check if index is active
     */
    public boolean isIndexActive(String indexId) {
        try {
            DescribeIndexRequest request = DescribeIndexRequest.builder()
                .id(indexId)
                .build();

            DescribeIndexResponse response = awsKendraClient.describeIndex(request);
            IndexStatus status = response.status();

            log.info("Index {} status: {}", indexId, status);
            return status == IndexStatus.ACTIVE;

        } catch (Exception e) {
            log.error("Failed to check index status for: {}", indexId, e);
            return false;
        }
    }

    /**
     * Get index information
     */
    public DescribeIndexResponse getIndexInfo(String indexId) {
        try {
            DescribeIndexRequest request = DescribeIndexRequest.builder()
                .id(indexId)
                .build();

            return awsKendraClient.describeIndex(request);

        } catch (Exception e) {
            throw new KendraIndexException("Failed to get index information: " + e.getMessage(), e);
        }
    }

    /**
     * Close client and release resources
     */
    public void close() {
        try {
            if (awsKendraClient != null) {
                awsKendraClient.close();
            }
            log.info("Kendra client closed successfully");
        } catch (Exception e) {
            log.error("Failed to close Kendra client", e);
            throw new com.alibaba.langengine.kendra.vectorstore.KendraException("Failed to close client: " + e.getMessage(), e);
        }
    }
}