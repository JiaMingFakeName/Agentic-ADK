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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Google Cloud Storage Get Object Text Tool
 * Get text content from a GCS object
 * 
 * @author LangEngine Team
 */
@Slf4j
public class GcpStorageGetObjectTextTool extends BaseTool {
    
    public GcpStorageGetObjectTextTool() {
        setName("GcpStorage.get_object_text");
        setDescription("Get text content from a Google Cloud Storage object. " +
                "Parameters: project_id, bucket_name, object_name, credentials_path (optional)");
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
                "      \"description\": \"Name of the object to retrieve\"\n" +
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
            
            if (StringUtils.isBlank(projectId) || StringUtils.isBlank(bucketName) || StringUtils.isBlank(objectName)) {
                return new ToolExecuteResult("Error: project_id, bucket_name, and object_name are required");
            }
            
            Storage storage = createStorageClient(projectId, credentialsPath);
            BlobId blobId = BlobId.of(bucketName, objectName);
            Blob blob = storage.get(blobId);
            
            if (blob == null || !blob.exists()) {
                return new ToolExecuteResult("Error: Object not found: " + objectName);
            }
            
            byte[] content = blob.getContent();
            String text = new String(content, StandardCharsets.UTF_8);
            
            Map<String, Object> result = new HashMap<>();
            result.put("bucket", bucketName);
            result.put("object", objectName);
            result.put("size", blob.getSize());
            result.put("content_type", blob.getContentType());
            result.put("content", text);
            
            return new ToolExecuteResult(JSON.toJSONString(result));
            
        } catch (Exception e) {
            log.error("GcpStorage.get_object_text failed", e);
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
