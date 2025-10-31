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

package com.alibaba.langengine.minio.example;

import com.alibaba.langengine.minio.MinioTool;
import com.alibaba.langengine.minio.boot.MinioTemplate;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A demonstration class showcasing the usage of {@link MinioTool} and {@link MinioTemplate}.
 * <p>
 * This example provides a runnable main method that connects to a MinIO server,
 * performs a series of common operations (create bucket, upload, download, presign, delete),
 * and then cleans up the created resources.
 * </p>
 */
public class MinioToolDemo {

    /**
     * The main entry point for the demonstration.
     *
     * @param args Command line arguments (not used).
     * @throws Exception if any MinIO operation fails.
     */
    public static void main(String[] args) throws Exception {
        // --- Configuration ---
        // Connect to the public MinIO sandbox service.
        String endpoint = "https://play.min.io";
        String accessKey = "Q3AM3UQ867SPQQA43P2F";
        String secretKey = "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG";

        // 1) Manually create an OkHttpClient. In a real application, you might
        //    use a shared client or the one from MinioClients.tunedHttpClient().
        OkHttpClient http = new OkHttpClient();

        // 2) Build the MinioClient, injecting the custom HttpClient.
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .httpClient(http)
                .build();

        // Instantiate the utility classes with the client.
        MinioTool tool = new MinioTool(client);
        MinioTemplate template = new MinioTemplate(client, 8L * 1024 * 1024);

        // --- Operations ---
        String bucket = "demo-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        System.out.println("Using bucket: " + bucket);

        tool.createBucket(bucket);
        System.out.println("Bucket created successfully.");

        // Upload a text object.
        String textKey = "hello.txt";
        tool.uploadObject(bucket, textKey, "Hello, MinIO!", true,
                "text/plain; charset=utf-8", Map.of(), -1, 8 * 1024 * 1024);
        System.out.println("Uploaded text object: " + textKey);

        // Download and verify the text object.
        var downloadedText = tool.downloadObject(bucket, textKey, true);
        System.out.println("Downloaded content: " + downloadedText.get("content"));

        // Upload a binary object from a byte array (encoded as Base64 for the tool).
        byte[] binaryData = new byte[1024];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (i & 0xFF);
        }
        String binaryKey = "bin/data.bin";
        String base64Content = Base64.getEncoder().encodeToString(binaryData);
        tool.uploadObject(bucket, binaryKey, base64Content, false,
                "application/octet-stream", null, -1, 8 * 1024 * 1024);
        System.out.println("Uploaded binary object: " + binaryKey);

        // Generate a presigned URL for downloading the text object.
        var presignedResult = tool.presignedGet(bucket, textKey, 60, null);
        System.out.println("Presigned GET URL: " + presignedResult.get("url"));

        // --- Cleanup ---
        tool.deleteObject(bucket, textKey);
        tool.deleteObjects(bucket, List.of(binaryKey));
        System.out.println("Cleanup of objects completed.");

        // --- Resource Shutdown ---
        http.dispatcher().executorService().shutdown();
        http.connectionPool().evictAll();
        System.out.println("HTTP client resources released.");
    }
}