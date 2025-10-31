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

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A template class that simplifies common MinIO operations.
 * <p>
 * This template provides a higher-level abstraction over the standard {@link MinioClient},
 * offering convenient methods for frequent tasks such as creating buckets, listing objects,
 * uploading content (text, Base64, streams), and generating presigned URLs. It is designed
 * to be thread-safe and can be configured as a singleton bean in a Spring application.
 * </p>
 */
public class MinioTemplate {

    private final MinioClient client;
    private final long defaultPartSize;

    /**
     * Constructs a new MinioTemplate.
     *
     * @param client The underlying {@link MinioClient} to be used for all operations.
     * @param defaultPartSize The default part size in bytes for multipart uploads. If the
     * value is non-positive, a default of 8 MiB will be used.
     */
    public MinioTemplate(MinioClient client, long defaultPartSize) {
        this.client = client;
        this.defaultPartSize = defaultPartSize <= 0 ? 8L * 1024 * 1024 : defaultPartSize;
    }

    /**
     * Creates a bucket if it does not already exist.
     *
     * @param bucket The name of the bucket to create.
     * @throws Exception if an error occurs during the operation.
     */
    public void createBucketIfAbsent(String bucket) throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * Lists objects in a specified bucket, optionally matching a prefix and recursively.
     *
     * @param bucket The name of the bucket.
     * @param prefix The prefix to filter objects by. Can be null or empty.
     * @param recursive If true, lists objects in a flat hierarchy. If false, emulates a
     * directory listing.
     * @return A list of maps, where each map represents an object's metadata.
     * @throws Exception if an error occurs during the operation.
     */
    public List<Map<String, Object>> listObjects(String bucket, String prefix, boolean recursive) throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();
        Iterable<Result<Item>> it = client.listObjects(
                ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(recursive).build());
        for (Result<Item> r : it) {
            Item o = r.get();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("object_name", o.objectName());
            m.put("size", o.size());
            m.put("is_dir", o.isDir());
            m.put("etag", o.etag());
            m.put("last_modified", o.lastModified() != null ? o.lastModified().toString() : null);
            items.add(m);
        }
        return items;
    }

    /**
     * Uploads a string as a text object.
     *
     * @param bucket The destination bucket name.
     * @param key The destination object key (name).
     * @param text The string content to upload, which will be UTF-8 encoded.
     * @param contentType The MIME type of the content. If null, "text/plain; charset=utf-8"
     * is used.
     * @return An {@link ObjectWriteResponse} containing metadata about the uploaded object.
     * @throws Exception if an error occurs during the upload.
     */
    public ObjectWriteResponse putText(String bucket, String key, String text, String contentType) throws Exception {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(key)
                    .stream(in, bytes.length, defaultPartSize)
                    .contentType(contentType != null ? contentType : "text/plain; charset=utf-8")
                    .build());
        }
    }

    /**
     * Decodes a Base64 string and uploads it as a binary object.
     *
     * @param bucket The destination bucket name.
     * @param key The destination object key (name).
     * @param base64 The Base64-encoded string content.
     * @param contentType The MIME type of the content.
     * @return An {@link ObjectWriteResponse} containing metadata about the uploaded object.
     * @throws Exception if an error occurs during the upload.
     */
    public ObjectWriteResponse putBase64(String bucket, String key, String base64, String contentType) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(key)
                    .stream(in, bytes.length, defaultPartSize)
                    .contentType(contentType)
                    .build());
        }
    }

    /**
     * Uploads content from an InputStream.
     *
     * @param bucket The destination bucket name.
     * @param key The destination object key (name).
     * @param stream The InputStream providing the content.
     * @param objectSize The total size of the object in bytes. Must be known in advance.
     * @param partSize The part size for multipart upload. If null, the default part size is used.
     * @param contentType The MIME type of the content.
     * @return An {@link ObjectWriteResponse} containing metadata about the uploaded object.
     * @throws Exception if an error occurs during the upload.
     */
    public ObjectWriteResponse putStream(String bucket, String key, InputStream stream,
                                         long objectSize, Long partSize, String contentType) throws Exception {
        long ps = validPartSize(partSize != null ? partSize : defaultPartSize);
        PutObjectArgs.Builder b = PutObjectArgs.builder()
                .bucket(bucket).object(key).stream(stream, objectSize, ps);
        if (contentType != null) {
            b.contentType(contentType);
        }
        return client.putObject(b.build());
    }

    /**
     * Downloads an object and returns its content as a byte array.
     *
     * @param bucket The bucket name.
     * @param key The object key (name).
     * @return A byte array containing the object's data.
     * @throws Exception if an error occurs during the download.
     */
    public byte[] getBytes(String bucket, String key) throws Exception {
        try (GetObjectResponse rsp = client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build())) {
            return rsp.readAllBytes();
        }
    }

    /**
     * Downloads a specific range of bytes from an object.
     *
     * @param bucket The bucket name.
     * @param key The object key (name).
     * @param offset The starting byte offset. If negative, the range download is disabled.
     * @param length The number of bytes to download. If non-positive, range download is disabled.
     * @return A byte array containing the specified portion of the object's data.
     * @throws Exception if an error occurs during the download.
     */
    public byte[] getRange(String bucket, String key, long offset, long length) throws Exception {
        GetObjectArgs.Builder b = GetObjectArgs.builder().bucket(bucket).object(key);
        if (offset >= 0 && length > 0) {
            b.offset(offset).length(length);
        }
        try (GetObjectResponse rsp = client.getObject(b.build())) {
            return rsp.readAllBytes();
        }
    }

    /**
     * Generates a presigned URL for downloading an object (HTTP GET).
     *
     * @param bucket The bucket name.
     * @param key The object key (name).
     * @param expiresSec The URL's expiration time in seconds.
     * @return A temporary, publicly accessible URL to the object.
     * @throws Exception if an error occurs during URL generation.
     */
    public String presignedGet(String bucket, String key, int expiresSec) throws Exception {
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(key)
                .expiry(expiresSec)
                .build());
    }

    /**
     * Generates a presigned URL for uploading an object (HTTP PUT).
     *
     * @param bucket The bucket name.
     * @param key The object key (name).
     * @param expiresSec The URL's expiration time in seconds.
     * @return A temporary URL that can be used to upload an object directly.
     * @throws Exception if an error occurs during URL generation.
     */
    public String presignedPut(String bucket, String key, int expiresSec) throws Exception {
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(key)
                .expiry(expiresSec)
                .build());
    }

    /**
     * Validates and adjusts the part size for multipart uploads.
     * The S3 protocol requires a minimum part size of 5 MiB.
     *
     * @param partSize The requested part size.
     * @return A valid part size, adjusted to the minimum if necessary.
     */
    private static long validPartSize(long partSize) {
        long MIN = 5L * 1024 * 1024;
        long DEF = 8L * 1024 * 1024;
        if (partSize < MIN) {
            return DEF;
        }
        return partSize;
    }
}