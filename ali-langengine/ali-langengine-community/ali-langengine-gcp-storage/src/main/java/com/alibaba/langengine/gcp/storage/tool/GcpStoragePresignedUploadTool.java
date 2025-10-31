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
package com.alibaba.langengine.gcp.storage.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Google Cloud Storage Presigned Upload Tool
 * Generate a presigned URL for uploading an object
 * 
 * @author LangEngine Team
 */
@Slf4j
public class GcpStoragePresignedUploadTool extends BaseTool {
    
    public GcpStoragePresignedUploadTool() {
        setName("GcpStorage.presigned_upload");
        setDescription("Generate a presigned URL for uploading an object to Google Cloud Storage. " +
                "Parameters: project_id, bucket_name, object_name, expiration_minutes (default 60), " +
                "content_type (optional), credentials_path (optional)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"project_id\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"GCP Project ID\"\n" +
                "    },\n" +
                "    \"bucket_name\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Name of the GCS bucket\"\n" +
                "    },\n" +
                "    \"object_name\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Name of the object to upload\"\n" +
                "    },\n" +
                "    \"expiration_minutes\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"URL expiration time in minutes (default: 60)\",\n" +
                "      \"default\": 60\n" +
                "    },\n" +
                "    \"content_type\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Content type for the upload (optional)\"\n" +
                "    },\n" +
                "    \"credentials_path\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Path to service account credentials JSON file (optional)\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"project_id\", \"bucket_name\", \"object_name\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String projectId = (String) args.get("project_id");
            String bucketName = (String) args.get("bucket_name");
            String objectName = (String) args.get("object_name");
            String credentialsPath = (String) args.get("credentials_path");
            String contentType = (String) args.get("content_type");
            Integer expirationMinutes = args.get("expiration_minutes") == null ? 60 :
                Integer.valueOf(String.valueOf(args.get("expiration_minutes")));
            
            if (StringUtils.isBlank(projectId) || StringUtils.isBlank(bucketName) || StringUtils.isBlank(objectName)) {
                return new ToolExecuteResult("Error: project_id, bucket_name, and object_name are required");
            }
            
            Storage storage = createStorageClient(projectId, credentialsPath);
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId);
            
            if (StringUtils.isNotBlank(contentType)) {
                blobInfoBuilder.setContentType(contentType);
            }
            
            BlobInfo blobInfo = blobInfoBuilder.build();
            
            Map<String, String> extensionHeaders = new HashMap<>();
            extensionHeaders.put("Content-Type", contentType != null ? contentType : "application/octet-stream");
            
            URL signedUrl = storage.signUrl(
                blobInfo,
                expirationMinutes,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                Storage.SignUrlOption.withV4Signature()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("bucket", bucketName);
            result.put("object", objectName);
            result.put("url", signedUrl.toString());
            result.put("method", "PUT");
            result.put("expiration_minutes", expirationMinutes);
            result.put("content_type", contentType);
            result.put("status", "success");
            result.put("instructions", "Use HTTP PUT method to upload file to this URL with Content-Type header");
            
            return new ToolExecuteResult(JSON.toJSONString(result));
            
        } catch (Exception e) {
            log.error("GcpStorage.presigned_upload failed", e);
            return new ToolExecuteResult("Error: " + e.getMessage());
        }
    }
    
    private Storage createStorageClient(String projectId, String credentialsPath) throws Exception {
        if (StringUtils.isNotBlank(credentialsPath)) {
            return StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(
                            new FileInputStream(credentialsPath)))
                    .build()
                    .getService();
        } else {
            return StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .build()
                    .getService();
        }
    }
}
