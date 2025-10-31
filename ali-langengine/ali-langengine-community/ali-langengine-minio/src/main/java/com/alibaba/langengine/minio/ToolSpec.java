/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law of a firm or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.minio;

import java.util.*;

/**
 * A utility class for building a tool specification schema for MinIO operations.
 * <p>
 * This class is designed to generate a structured map that defines the capabilities
 * and parameters of a MinIO tool, conforming to a specific schema format (e.g., for
 * integration with AI agent frameworks). The schema describes the available actions,
 * connection parameters, and operation-specific arguments.
 * </p>
 */
public final class ToolSpec {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ToolSpec() {}

    /**
     * Builds the tool specification schema as a map.
     *
     * @param name The name to be assigned to the tool in the schema.
     * @return A {@code Map<String, Object>} representing the complete tool schema.
     */
    public static Map<String, Object> build(String name) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("name", name);
        schema.put("description", "MinIO/S3-compatible ops: buckets, objects, presigned URLs (Java)");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of(
                "type", "string",
                "enum", Arrays.asList(
                        "create_bucket", "list_buckets", "list_objects",
                        "upload_object", "download_object",
                        "delete_object", "delete_objects",
                        "presigned_get", "presigned_put"
                )
        ));

        String[] conn = {"endpoint", "access_key", "secret_key", "session_token", "region"};
        for (String k : conn) {
            props.put(k, Map.of("type", "string"));
        }
        props.put("secure", Map.of("type", "boolean", "default", true));

        props.put("bucket", Map.of("type", "string"));
        props.put("object_name", Map.of("type", "string"));
        props.put("prefix", Map.of("type", "string"));
        props.put("recursive", Map.of("type", "boolean", "default", false));
        props.put("start_after", Map.of("type", "string"));

        // Properties for upload/download operations
        props.put("content", Map.of("type", "string"));
        props.put("content_is_text", Map.of("type", "boolean", "default", true));
        props.put("content_type", Map.of("type", "string"));
        props.put("metadata", Map.of("type", "object", "additionalProperties", Map.of("type", "string")));
        props.put("part_size", Map.of("type", "integer", "description", "multipart part size bytes", "default", 8 * 1024 * 1024));
        props.put("object_size", Map.of("type", "integer", "description", "known object size; -1 if unknown", "default", -1));

        // Properties for batch delete operations
        props.put("objects", Map.of("type", "array", "items", Map.of("type", "string")));

        // Properties for presigned URL generation
        props.put("expires_sec", Map.of("type", "integer", "default", 3600));
        props.put("response_params", Map.of("type", "object", "additionalProperties", Map.of("type", "string")));

        params.put("properties", props);
        params.put("required", List.of("action"));
        schema.put("parameters", params);
        return schema;
    }
}