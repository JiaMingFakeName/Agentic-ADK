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

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A comprehensive tool for interacting with a MinIO or S3-compatible service.
 * <p>
 * This class provides a rich set of methods that simplify common storage operations,
 * including bucket management, object uploading/downloading, listing, and deletion.
 * It is designed to work with structured data (maps) for input and output, making it
 * suitable for integration into larger systems, such as agent-based frameworks or
 * backend services. The methods handle details like content encoding (text vs. Base64),
 * multipart uploads, and error handling.
 * </p>
 */
public class MinioTool implements AutoCloseable {
    private final MinioClient client;

    /**
     * Constructs a MinioTool instance by creating a new MinioClient from
     * environment variables using {@link MinioClients#fromEnv()}.
     */
    public MinioTool() {
        this(MinioClients.fromEnv());
    }

    /**
     * Constructs a MinioTool instance with a provided MinioClient.
     *
     * @param client The {@link MinioClient} to use for all operations.
     */
    public MinioTool(MinioClient client) {
        this.client = client;
    }

    // ---------- Bucket Operations ----------

    /**
     * Lists all buckets accessible with the configured credentials.
     *
     * @return A map containing a list of bucket details. Each bucket is represented
     * by a map with its "name" and "creation_date".
     * @throws Exception if an error occurs while communicating with the service.
     */
    public Map<String,Object> listBuckets() throws Exception {
        List<Bucket> buckets = client.listBuckets();
        List<Map<String,Object>> items = new ArrayList<>();
        for (Bucket b : buckets) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("name", b.name());
            m.put("creation_date", b.creationDate().toString());
            items.add(m);
        }
        return Map.of("buckets", items);
    }

    /**
     * Creates a new bucket if it does not already exist.
     *
     * @param bucket The name of the bucket to create.
     * @return A map indicating the result, with "ok" (true) and "created" (true if
     * a new bucket was made, false if it already existed).
     * @throws Exception if an error occurs during the operation.
     */
    public Map<String,Object> createBucket(String bucket) throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        return Map.of("ok", true, "created", !exists);
    }

    // ---------- Object Operations ----------

    /**
     * Lists objects within a specified bucket.
     *
     * @param bucket The name of the bucket.
     * @param prefix An optional prefix to filter objects by.
     * @param recursive If true, lists objects recursively. If false, emulates directory listing.
     * @param startAfter An optional object name to start listing after, for pagination.
     * @return A map containing a list of object details.
     * @throws Exception if an error occurs during the operation.
     */
    public Map<String,Object> listObjects(String bucket, String prefix, boolean recursive, String startAfter) throws Exception {
        List<Map<String,Object>> items = new ArrayList<>();
        ListObjectsArgs.Builder builder = ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(recursive)
                .startAfter(startAfter);

        Iterable<Result<Item>> results = client.listObjects(builder.build());
        for (Result<Item> r : results) {
            Item it = r.get();
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("object_name", it.objectName());
            m.put("size", it.size());
            m.put("is_dir", it.isDir());
            m.put("etag", it.etag());
            m.put("last_modified", it.lastModified() != null ? it.lastModified().toString() : null);
            items.add(m);
        }
        return Map.of("objects", items);
    }

    /**
     * Uploads content to an object from a String.
     *
     * @param bucket The target bucket name.
     * @param objectName The target object name (key).
     * @param content The object content. Can be plain text or Base64 encoded binary data.
     * @param contentIsText If true, content is treated as UTF-8 text; otherwise, it's Base64 decoded.
     * @param contentType The MIME type of the content.
     * @param metadata Optional user-defined metadata for the object (headers starting with x-amz-meta-).
     * @param objectSize The size of the object. Use -1 if the size is unknown for streaming.
     * @param partSize The part size for multipart uploads.
     * @return A map containing the etag and optional versionId of the uploaded object.
     * @throws Exception if an error occurs during the upload.
     */
    public Map<String,Object> uploadObject(String bucket, String objectName,
                                           String content, boolean contentIsText,
                                           String contentType, Map<String,String> metadata,
                                           long objectSize, long partSize) throws Exception {
        byte[] bytes = contentIsText ? content.getBytes(StandardCharsets.UTF_8)
                : Base64.getDecoder().decode(content);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket).object(objectName)
                    .stream(bais, objectSize < 0 ? bytes.length : objectSize, validPartSize(partSize));

            if (contentType != null) {
                builder.contentType(contentType);
            }
            if (metadata != null && !metadata.isEmpty()) {
                builder.headers(metadata);
            }
            ObjectWriteResponse resp = client.putObject(builder.build());

            Map<String,Object> out = new LinkedHashMap<>();
            out.put("etag", resp.etag());
            if (resp.versionId() != null) {
                out.put("version_id", resp.versionId());
            }
            return out;
        }
    }

    /**
     * Uploads content to an object from an InputStream.
     *
     * @param bucket The target bucket name.
     * @param objectName The target object name (key).
     * @param stream The InputStream containing the object data.
     * @param objectSize The total size of the object in bytes. Must be known for streaming.
     * @param partSize The part size for multipart uploads.
     * @param contentType The MIME type of the content.
     * @param metadata Optional user-defined metadata for the object.
     * @return A map containing the etag and optional versionId of the uploaded object.
     * @throws Exception if an error occurs during the upload.
     */
    public Map<String,Object> uploadStream(String bucket, String objectName,
                                           InputStream stream, long objectSize, long partSize,
                                           String contentType, Map<String,String> metadata) throws Exception {
        PutObjectArgs.Builder builder = PutObjectArgs.builder()
                .bucket(bucket).object(objectName)
                .stream(stream, objectSize, validPartSize(partSize));
        if (contentType != null) {
            builder.contentType(contentType);
        }
        if (metadata != null && !metadata.isEmpty()) {
            builder.headers(metadata);
        }
        ObjectWriteResponse resp = client.putObject(builder.build());

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("etag", resp.etag());
        if (resp.versionId() != null) {
            out.put("version_id", resp.versionId());
        }
        return out;
    }

    /**
     * Downloads an object's content.
     *
     * @param bucket The bucket where the object resides.
     * @param objectName The name (key) of the object to download.
     * @param asText If true, the content is returned as a UTF-8 string; otherwise, as Base64.
     * @return A map containing the content, content type, and other metadata.
     * @throws Exception if an error occurs during the download.
     */
    public Map<String,Object> downloadObject(String bucket, String objectName, boolean asText) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try (GetObjectResponse rsp = client.getObject(args)) {
            byte[] data = rsp.readAllBytes();
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("content", asText ? new String(data, StandardCharsets.UTF_8)
                    : Base64.getEncoder().encodeToString(data));
            out.put("content_is_text", asText);
            String ct = rsp.headers().get("Content-Type");
            if (ct != null) {
                out.put("content_type", ct);
            }
            out.put("byte_length", data.length);
            return out;
        }
    }

    /**
     * Downloads a specific byte range from an object.
     * This method contains special logic for text files to ensure character boundaries
     * are not broken. For binary files, it performs a direct byte-range request.
     *
     * @param bucket The bucket where the object resides.
     * @param objectName The name (key) of the object.
     * @param offset The starting offset. For text, this is a character offset; for binary, a byte offset.
     * @param length The number of items to download. For text, this is number of characters; for binary, number of bytes.
     * @param asText If true, content is treated as text with character-safe slicing; otherwise, as binary with byte-range requests.
     * @return A map containing the partial content and related metadata.
     * @throws Exception if an error occurs during the download.
     */
    public Map<String,Object> downloadRange(String bucket, String objectName, long offset, long length, boolean asText) throws Exception {
        if (asText && offset >= 0 && length > 0) {
            // Text mode: To ensure character boundaries are correct, download the full object
            // and then perform a substring operation on the resulting string.
            try (GetObjectResponse full = client.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {
                byte[] all = full.readAllBytes();
                String s = new String(all, StandardCharsets.UTF_8);
                int start = (int) Math.min(offset, s.length());
                int end = (int) Math.min((long)start + length, (long)s.length());
                String sub = s.substring(start, end);

                Map<String,Object> out = new LinkedHashMap<>();
                out.put("content", sub);
                out.put("content_is_text", true);
                String ct = full.headers().get("Content-Type");
                if (ct != null) {
                    out.put("content_type", ct);
                }
                out.put("char_length", sub.length());
                out.put("range_offset", offset);
                out.put("range_length", length);
                return out;
            }
        }

        // Binary mode: Perform a standard byte-range download.
        GetObjectArgs.Builder b = GetObjectArgs.builder().bucket(bucket).object(objectName);
        if (offset >= 0 && length > 0) {
            b.offset(offset).length(length);
        }
        try (GetObjectResponse rsp = client.getObject(b.build())) {
            byte[] data = rsp.readAllBytes();
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("content", asText ? new String(data, StandardCharsets.UTF_8)
                    : Base64.getEncoder().encodeToString(data));
            out.put("content_is_text", asText);
            String ct = rsp.headers().get("Content-Type");
            if (ct != null) {
                out.put("content_type", ct);
            }
            out.put("byte_length", data.length);
            out.put("range_offset", offset);
            out.put("range_length", length);
            return out;
        }
    }

    /**
     * Generates a presigned URL for downloading an object (HTTP GET).
     *
     * @param bucket The bucket name.
     * @param objectName The object name (key).
     * @param expiresSec The URL's expiration time in seconds.
     * @param respParams Optional query parameters to override response headers (e.g., "response-content-disposition").
     * @return A map containing the generated URL, HTTP method, and expiration time.
     * @throws Exception if an error occurs during URL generation.
     */
    public Map<String,Object> presignedGet(String bucket, String objectName, int expiresSec, Map<String,String> respParams) throws Exception {
        GetPresignedObjectUrlArgs.Builder b = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket).object(objectName)
                .expiry(expiresSec);
        if (respParams != null && !respParams.isEmpty()) {
            b.extraQueryParams(respParams);
        }
        String url = client.getPresignedObjectUrl(b.build());
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("url", url);
        out.put("method", "GET");
        out.put("expires_sec", expiresSec);
        return out;
    }

    /**
     * Generates a presigned URL for uploading an object (HTTP PUT).
     *
     * @param bucket The bucket name.
     * @param objectName The object name (key).
     * @param expiresSec The URL's expiration time in seconds.
     * @return A map containing the generated URL, HTTP method, and expiration time.
     * @throws Exception if an error occurs during URL generation.
     */
    public Map<String,Object> presignedPut(String bucket, String objectName, int expiresSec) throws Exception {
        GetPresignedObjectUrlArgs.Builder b = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(objectName)
                .expiry(expiresSec);
        String url = client.getPresignedObjectUrl(b.build());
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("url", url);
        out.put("method", "PUT");
        out.put("expires_sec", expiresSec);
        return out;
    }

    /**
     * Deletes a single object.
     *
     * @param bucket The bucket where the object resides.
     * @param objectName The name (key) of the object to delete.
     * @return A map with an "ok" status of true.
     * @throws Exception if an error occurs during deletion.
     */
    public Map<String,Object> deleteObject(String bucket, String objectName) throws Exception {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
        return Map.of("ok", true);
    }

    /**
     * Deletes multiple objects in a single batch request. This method first checks
     * for the existence of each object and reports non-existent objects as errors
     * before attempting the actual deletion of the ones that exist.
     *
     * @param bucket The bucket where the objects reside.
     * @param keys A list of object names (keys) to delete.
     * @return A map containing a list of any errors that occurred.
     * @throws Exception if a critical error occurs during the batch operation.
     */
    public Map<String,Object> deleteObjects(String bucket, List<String> keys) throws Exception {
        List<DeleteObject> toDelete = new ArrayList<>();
        List<Map<String,String>> errors = new ArrayList<>();

        // First, stat each object to explicitly identify and report non-existent ones.
        for (String k : keys) {
            try {
                client.statObject(StatObjectArgs.builder().bucket(bucket).object(k).build());
                toDelete.add(new DeleteObject(k));
            } catch (ErrorResponseException e) {
                // A 404/NoSuchKey error means the object does not exist. Record it as an error.
                Map<String,String> err = new LinkedHashMap<>();
                err.put("object", k);
                err.put("message", "NoSuchKey");
                errors.add(err);
            }
        }

        // Only proceed with batch deletion if there are existing objects to delete.
        if (!toDelete.isEmpty()) {
            Iterable<Result<DeleteError>> res =
                    client.removeObjects(RemoveObjectsArgs.builder().bucket(bucket).objects(toDelete).build());
            for (Result<DeleteError> r : res) {
                DeleteError e = r.get();
                Map<String,String> err = new LinkedHashMap<>();
                err.put("object", e.objectName());
                err.put("message", e.message());
                errors.add(err);
            }
        }

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("errors", errors);
        return out;
    }


    /**
     * Checks if an object exists and is accessible.
     *
     * @param bucket The bucket where the object might reside.
     * @param objectName The name (key) of the object to check.
     * @return True if the object exists, otherwise false. Returns false on any exception.
     */
    public boolean objectExists(String bucket, String objectName) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
            return true;
        } catch (ErrorResponseException e) {
            // Typically a 404 Not Found error, which means the object does not exist.
            return false;
        } catch (Exception e) {
            // Any other exception (network issues, permissions) also means we can't "see" the object.
            return false;
        }
    }

    /**
     * Validates and adjusts the part size for multipart uploads to meet S3 requirements.
     *
     * @param partSize The requested part size.
     * @return A valid part size, adjusted up to the minimum if necessary.
     */
    private static long validPartSize(long partSize) {
        final long MIN_PART_SIZE = 5L * 1024 * 1024; // 5 MiB
        final long DEFAULT_PART_SIZE = 8L * 1024 * 1024; // 8 MiB

        if (partSize < MIN_PART_SIZE) {
            return DEFAULT_PART_SIZE;
        }
        return partSize;
    }

    /**
     * Implements the AutoCloseable interface. The underlying MinioClient, which manages
     * a shared OkHttpClient, does not need to be explicitly closed in most application
     * contexts (e.g., when managed as a Spring bean).
     */
    @Override
    public void close() {
        /* MinioClient does not require explicit closing. */
    }
}