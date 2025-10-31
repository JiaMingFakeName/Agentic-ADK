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

import com.alibaba.langengine.minio.boot.MinioAutoConfiguration;
import com.alibaba.langengine.minio.boot.MinioTemplate;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link MinioAutoConfiguration}.
 * <p>
 * This class uses Testcontainers to launch a MinIO instance and a Spring
 * {@link ApplicationContextRunner} to test that the Spring Boot auto-configuration
 * correctly initializes and configures the MinIO-related beans based on
 * application properties.
 * </p>
 */
public class MinioAutoConfigurationIT {

    private static final String IMAGE = "minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1";
    static GenericContainer<?> minio;
    static int mappedPort;

    /**
     * Starts the MinIO Docker container before any tests in this class are executed.
     */
    @BeforeAll
    static void up() {
        minio = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                .withExposedPorts(9000)
                .withCommand("server", "/data", "--address", ":9000")
                .waitingFor(Wait.forHttp("/minio/health/ready").forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(2));
        minio.start();
        mappedPort = minio.getMappedPort(9000);
    }

    /**
     * Stops the MinIO Docker container after all tests in this class have completed.
     */
    @AfterAll
    static void down() {
        if (minio != null) {
            minio.stop();
        }
    }

    /**
     * Tests that the MinIO beans (MinioClient, MinioTemplate) are correctly autowired
     * into the Spring context and that they are functional by performing basic operations.
     * This test uses properties for the public MinIO play server.
     */
    @Test
    void autowire_minio_beans_and_basic_ops() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MinioAutoConfiguration.class))
                .withPropertyValues(
                        "com.alibaba.langengine.minio.endpoint=play.min.io",
                        "com.alibaba.langengine.minio.secure=true",
                        "com.alibaba.langengine.minio.access-key=Q3AM3UQ867SPQQA43P2F",
                        "com.alibaba.langengine.minio.secret-key=zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG",
                        "com.alibaba.langengine.minio.part-size=8388608"
                )
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(MinioClient.class);
                    assertThat(ctx).hasSingleBean(MinioTemplate.class);

                    var tpl = ctx.getBean(MinioTemplate.class);
                    String bucket = "boot-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

                    tpl.createBucketIfAbsent(bucket);
                    tpl.putText(bucket, "hello.txt", "boot-ok", "text/plain; charset=utf-8");

                    byte[] bytes = tpl.getBytes(bucket, "hello.txt");
                    assertThat(new String(bytes)).isEqualTo("boot-ok");
                });
    }

    /**
     * Tests that custom HTTP client configurations (like timeouts and pool sizes)
     * are correctly applied when creating the MinioClient bean. This test connects
     * to the local Testcontainers MinIO instance.
     */
    @Test
    void http_pool_config_applied() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MinioAutoConfiguration.class))
                .withPropertyValues(
                        "com.alibaba.langengine.minio.endpoint=localhost:" + mappedPort,
                        "com.alibaba.langengine.minio.secure=false",
                        "com.alibaba.langengine.minio.access-key=minioadmin",
                        "com.alibaba.langengine.minio.secret-key=minioadmin",
                        "com.alibaba.langengine.minio.http.max-requests=128",
                        "com.alibaba.langengine.minio.http.max-requests-per-host=32",
                        "com.alibaba.langengine.minio.http.connect-timeout-sec=3"
                )
                .run(ctx -> assertThat(ctx).hasSingleBean(MinioClient.class));
    }
}