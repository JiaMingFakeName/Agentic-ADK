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
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link MinioTool} class.
 * <p>
 * This test suite uses Testcontainers to spin up a real MinIO Docker container,
 * ensuring that the tool's methods are tested against a live S3-compatible environment.
 * Tests are ordered to follow a logical sequence of operations (create, upload, download, delete).
 * </p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MinioToolIT {

    // It is recommended to pin to a specific release tag for stability.
    private static final String IMAGE = "minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1";
    private static final String ROOT_USER = "minioadmin";
    private static final String ROOT_PASS = "minioadmin";
    private static final int PORT = 9000;

    static GenericContainer<?> minio;
    static MinioClient client;
    static MinioTool tool;

    private static String bucket = "agent-artifacts";
    private static String textKey = "dir/a.txt";
    private static String binKey = "dir/b.bin";

    /**
     * Starts the MinIO Docker container and initializes the MinioClient and MinioTool
     * instances before any tests are run.
     */
    @BeforeAll
    static void start() {
        minio = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withEnv("MINIO_ROOT_USER", ROOT_USER)
                .withEnv("MINIO_ROOT_PASSWORD", ROOT_PASS)
                .withExposedPorts(PORT)
                .withCommand("server", "/data", "--address", ":" + PORT)
                .waitingFor(Wait.forHttp("/minio/health/ready").forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(2));
        minio.start();

        String endpoint = "http://localhost:" + minio.getMappedPort(PORT);
        client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(ROOT_USER, ROOT_PASS)
                .build();
        tool = new MinioTool(client);

        // Wait for the service to be fully ready.
        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            var res = tool.listBuckets();
            assertThat(res).containsKey("buckets");
        });
    }

    /**
     * Stops the MinIO Docker container after all tests have completed.
     */
    @AfterAll
    static void stop() {
        if (minio != null) {
            minio.stop();
        }
    }

    /**
     * Tests that a bucket can be created, and that subsequent attempts to create the
     * same bucket are handled gracefully without error.
     */
    @Test
    @Order(1)
    void create_bucket_if_absent() throws Exception {
        var r1 = tool.createBucket(bucket);
        var r2 = tool.createBucket(bucket);
        assertThat(r1).containsEntry("ok", true);
        assertThat(r2).containsEntry("created", false);
    }

    /**
     * Verifies that the list of buckets is not empty after creating one.
     */
    @Test
    @Order(2)
    void list_buckets_non_empty() throws Exception {
        var r = tool.listBuckets();
        var buckets = (List<?>) r.get("buckets");
        assertThat(buckets).isNotEmpty();
    }

    /**
     * Tests uploading a small text object with custom metadata.
     */
    @Test
    @Order(3)
    void upload_text_small() throws Exception {
        var r = tool.uploadObject(bucket, textKey, "你好 MinIO", true,
                "text/plain; charset=utf-8", Map.of("x-amz-meta-k","v"), -1, 8*1024*1024);
        assertThat(r).containsKey("etag");
    }

    /**
     * Tests downloading the previously uploaded text object and verifies its content and content type.
     */
    @Test
    @Order(4)
    void download_text_equals() throws Exception {
        var r = tool.downloadObject(bucket, textKey, true);
        assertThat(r.get("content")).isEqualTo("你好 MinIO");
        assertThat((String) r.get("content_type")).startsWith("text/plain");
    }

    /**
     * Tests uploading binary data (provided as Base64) using a custom part size.
     * The provided part size (6MiB) is valid, but the tool may adjust it internally.
     */
    @Test
    @Order(5)
    void upload_binary_base64_with_part_size() throws Exception {
        byte[] data = new byte[3_500_000]; // 3.5MB
        new Random(7).nextBytes(data);
        String b64 = Base64.getEncoder().encodeToString(data);
        var r = tool.uploadObject(bucket, binKey, b64, false,
                "application/octet-stream", Map.of(), -1, 6*1024*1024);
        assertThat(r).containsKey("etag");
    }

    /**
     * Tests downloading the binary object and verifies its size.
     */
    @Test
    @Order(6)
    void download_binary_base64() throws Exception {
        var r = tool.downloadObject(bucket, binKey, false);
        String b64 = (String) r.get("content");
        byte[] got = Base64.getDecoder().decode(b64);
        assertThat(got.length).isEqualTo(3_500_000);
    }

    /**
     * Tests listing objects with a specific prefix recursively.
     */
    @Test
    @Order(7)
    void list_objects_prefix_recursive() throws Exception {
        var r = tool.listObjects(bucket, "dir/", true, null);
        var objs = (List<Map<String,Object>>) r.get("objects");
        assertThat(objs.stream().anyMatch(o -> o.get("object_name").equals(textKey))).isTrue();
    }

    /**
     * Tests the objectExists method for both an existing and a non-existent object.
     */
    @Test
    @Order(8)
    void object_exists_true_false() {
        assertThat(tool.objectExists(bucket, textKey)).isTrue();
        assertThat(tool.objectExists(bucket, "not-exist")).isFalse();
    }

    /**
     * Tests generating a presigned GET URL and then using it to fetch the object's content.
     */
    @Test
    @Order(9)
    void presigned_get_and_http_fetch() throws Exception {
        var url = (String) tool.presignedGet(bucket, textKey, 60, Map.of()).get("url");
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        assertThat(conn.getResponseCode()).isEqualTo(200);
        byte[] body = conn.getInputStream().readAllBytes();
        assertThat(new String(body, StandardCharsets.UTF_8)).isEqualTo("你好 MinIO");
    }

    /**
     * Tests generating a presigned PUT URL and then using it to upload a new object.
     */
    @Test
    @Order(10)
    void presigned_put_and_http_upload() throws Exception {
        var url = (String) tool.presignedPut(bucket, "dir/put.txt", 60).get("url");
        byte[] body = "FROM_PRESIGNED_PUT".getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.getOutputStream().write(body);
        assertThat(conn.getResponseCode()).isIn(200, 204);
        var dl = tool.downloadObject(bucket, "dir/put.txt", true);
        assertThat(dl.get("content")).isEqualTo("FROM_PRESIGNED_PUT");
    }

    /**
     * Tests the text-aware range download to get a partial string from an object.
     */
    @Test
    @Order(11)
    void range_download_partial() throws Exception {
        var r = tool.downloadRange(bucket, textKey, 0, 3, true);
        assertThat(((String) r.get("content")).length()).isEqualTo(3);
    }

    /**
     * Tests batch deletion where one of the keys does not exist, expecting it to be reported as an error.
     */
    @Test
    @Order(12)
    void batch_delete_partial_errors() throws Exception {
        var r = tool.deleteObjects(bucket, List.of("dir/put.txt", "not-found"));
        var errors = (List<Map<String,String>>) r.get("errors");
        assertThat(errors.stream().anyMatch(e -> e.get("object").equals("not-found"))).isTrue();
    }

    /**
     * Tests deleting a single object and confirming it no longer exists.
     */
    @Test
    @Order(13)
    void delete_object_ok() throws Exception {
        var r = tool.deleteObject(bucket, textKey);
        assertThat(r).containsEntry("ok", true);
        assertThat(tool.objectExists(bucket, textKey)).isFalse();
    }

    /**
     * Tests uploading multiple small files in parallel to exercise the HTTP connection pool.
     */
    @Test
    @Order(14)
    void parallel_small_uploads_to_saturate_pool() throws Exception {
        int N = 32;
        ExecutorService es = Executors.newFixedThreadPool(16);
        List<Future<?>> futures = new ArrayList<>();
        for (int i=0; i<N; i++) {
            int idx = i;
            futures.add(es.submit(() -> {
                try {
                    String key = "p/" + idx + ".txt";
                    tool.uploadObject(bucket, key, "X".repeat(256), true,
                            "text/plain", Map.of(), -1, 8*1024*1024);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        for (Future<?> f: futures) f.get(60, TimeUnit.SECONDS);
        es.shutdown();
        var r = tool.listObjects(bucket, "p/", true, null);
        var objs = (List<Map<String,Object>>) r.get("objects");
        assertThat(objs.size()).isEqualTo(32);
    }

    /**
     * Verifies that the content type header persists after uploading a larger text object.
     */
    @Test
    @Order(15)
    void long_text_content_type_persists() throws Exception {
        String key = "ct/long.txt";
        tool.uploadObject(bucket, key, "Y".repeat(10_000), true, "text/plain; charset=utf-8", Map.of(), -1, 8*1024*1024);
        var dl = tool.downloadObject(bucket, key, true);
        assertThat(((String) dl.get("content_type")).toLowerCase()).contains("text/plain");
    }

    /**
     * Verifies that user metadata can be included in an upload request and the object
     * remains downloadable.
     */
    @Test
    @Order(16)
    void metadata_roundtrip() throws Exception {
        String key = "meta/m.txt";
        tool.uploadObject(bucket, key, "M", true, "text/plain", Map.of("x-amz-meta-a","1"), -1, 8*1024*1024);
        var dl = tool.downloadObject(bucket, key, true);
        assertThat(dl.get("content")).isEqualTo("M");
    }

    /**
     * Tests the pagination feature (`startAfter`) of object listing.
     */
    @Test
    @Order(17)
    void list_objects_start_after() throws Exception {
        for (int i=0; i<5; i++) {
            tool.uploadObject(bucket, "sa/" + i, "K", true, "text/plain", Map.of(), -1, 8*1024*1024);
        }
        var r1 = tool.listObjects(bucket, "sa/", true, null);
        var r2 = tool.listObjects(bucket, "sa/", true, "sa/2");
        var all = (List<Map<String,Object>>) r1.get("objects");
        var after = (List<Map<String,Object>>) r2.get("objects");
        assertThat(all.size()).isGreaterThan(after.size());
        assertThat(after.stream().allMatch(o -> ((String)o.get("object_name")).compareTo("sa/2") > 0)).isTrue();
    }

    /**
     * Tests a multipart upload for an object larger than the part size.
     */
    @Test
    @Order(18)
    void big_object_multipart_with_custom_part_size() throws Exception {
        String key = "big/1.bin";
        byte[] data = new byte[12 * 1024 * 1024]; // 12MiB
        new Random(9).nextBytes(data);
        String b64 = Base64.getEncoder().encodeToString(data);
        tool.uploadObject(bucket, key, b64, false, "application/octet-stream", Map.of(), -1, 6*1024*1024);
        var dl = tool.downloadObject(bucket, key, false);
        byte[] got = Base64.getDecoder().decode((String) dl.get("content"));
        assertThat(got.length).isEqualTo(data.length);
    }

    /**
     * Verifies generation of a short-lived presigned GET URL with custom response parameters.
     */
    @Test
    @Order(19)
    void presign_get_short_expiry() throws Exception {
        var map = tool.presignedGet(bucket, binKey, 30, Map.of("response-content-disposition","attachment"));
        assertThat(map.get("url")).isInstanceOf(String.class);
    }

    /**
     * Tests deleting a batch of objects where all keys are valid and expects no errors.
     */
    @Test
    @Order(20)
    void delete_objects_all_ok() throws Exception {
        var r = tool.listObjects(bucket, "p/", true, null);
        var objects = (List<Map<String,Object>>) r.get("objects");
        var keys = objects.stream().map(o -> (String) o.get("object_name")).collect(Collectors.toCollection(ArrayList::new));
        var del = tool.deleteObjects(bucket, keys);
        var errors = (List<?>) del.get("errors");
        assertThat(errors).isEmpty();
    }

    /**
     * Cleans up specific objects used across multiple tests.
     */
    @Test
    @Order(21)
    void cleanup_prefixes_ok() throws Exception {
        tool.deleteObjects(bucket, List.of("big/1.bin", binKey));
        assertThat(tool.objectExists(bucket, binKey)).isFalse();
    }

    /**
     * Verifies that listing objects with a non-existent prefix returns an empty list.
     */
    @Test
    @Order(22)
    void list_objects_empty_prefix_ok() throws Exception {
        var r = tool.listObjects(bucket, "nope/", true, null);
        var objs = (List<?>) r.get("objects");
        assertThat(objs).isEmpty();
    }
}