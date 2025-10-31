/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may a copy of the License at
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

import com.alibaba.langengine.minio.MinioTool;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for MinIO.
 * <p>
 * This class provides the necessary Spring beans to integrate MinIO with a Spring Boot
 * application. It automatically configures {@link MinioClient}, {@link MinioTool},
 * and {@link MinioTemplate} based on the properties defined in {@link MinioProperties}.
 * The configuration is activated when {@code MinioClient} is present on the classpath.
 * </p>
 */
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    /**
     * Creates an {@link OkHttpClient} bean if one is not already present.
     * <p>
     * This HTTP client is specifically configured for MinIO communication, with
     * settings tuned for performance and reliability based on the application properties.
     * </p>
     *
     * @param props The MinIO configuration properties.
     * @return A configured {@code OkHttpClient} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient minioOkHttpClient(MinioProperties props) {
        return HttpClientFactory.build(props.getHttp());
    }

    /**
     * Creates a {@link MinioClient} bean if one is not already present.
     * <p>
     * This is the core client for interacting with the MinIO server. It is built
     * using the auto-configured {@link OkHttpClient} and connection details
     * from {@link MinioProperties}.
     * </p>
     *
     * @param http  The OkHttpClient for communication.
     * @param props The MinIO configuration properties.
     * @return A configured {@code MinioClient} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(OkHttpClient http, MinioProperties props) {
        MinioClient.Builder b = MinioClient.builder()
                .endpoint((Boolean.TRUE.equals(props.getSecure()) ? "https://" : "http://") + props.getEndpoint())
                .httpClient(http);

        if (props.getAccessKey() != null && props.getSecretKey() != null) {
            b.credentials(props.getAccessKey(), props.getSecretKey());
        }
        if (props.getRegion() != null) {
            b.region(props.getRegion());
        }
        return b.build();
    }

    /**
     * Creates a {@link MinioTool} bean if one is not already present.
     * <p>
     * {@code MinioTool} provides a higher-level, more user-friendly API for
     * common MinIO operations, wrapping the base {@link MinioClient}.
     * </p>
     *
     * @param client The configured MinioClient.
     * @return A {@code MinioTool} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioTool minioTool(MinioClient client) {
        return new MinioTool(client);
    }

    /**
     * Creates a {@link MinioTemplate} bean if one is not already present.
     * <p>
     * {@code MinioTemplate} offers another convenient abstraction over the
     * {@link MinioClient}, tailored for common application use cases like
     * uploading text or streams.
     * </p>
     *
     * @param client The configured MinioClient.
     * @param props  The MinIO configuration properties.
     * @return A {@code MinioTemplate} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioTemplate minioTemplate(MinioClient client, MinioProperties props) {
        return new MinioTemplate(client, props.getPartSize());
    }
}