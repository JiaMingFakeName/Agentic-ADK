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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Cloud Storage List Objects Tool
 * List objects in a GCS bucket
 * 
 * @author LangEngine Team
 */
@Slf4j
public class GcpStorageListObjectsTool extends BaseTool {
    
    public GcpStorageListObjectsTool() {
        setName("GcpStorage.list_objects");
        setDescription("List objects in a Google Cloud Storage bucket. " +
                "Parameters: project_id, bucket_name, credentials_path (optional), " +
                "prefix (optional), max_results (optional, default 100)");
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
                "    \"credentials_path\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Path to service account credentials JSON file (optional)\"\n" +
                "    },\n" +
                "    \"prefix\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Filter objects by prefix (optional)\"\n" +
                "    },\n" +
                "    \"max_results\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"Maximum number of results to return (default: 100)\",\n" +
                "      \"default\": 100\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"project_id\", \"bucket_name\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String projectId = (String) args.get("project_id");
            String bucketName = (String) args.get("bucket_name");
            String credentialsPath = (String) args.get("credentials_path");
            String prefix = (String) args.get("prefix");
            Integer maxResults = args.get("max_results") == null ? 100 : 
                Integer.valueOf(String.valueOf(args.get("max_results")));
            
            if (StringUtils.isBlank(projectId) || StringUtils.isBlank(bucketName)) {
                return new ToolExecuteResult("Error: project_id and bucket_name are required");
            }
            
            Storage storage = createStorageClient(projectId, credentialsPath);
            
            Page<Blob> blobs;
            if (StringUtils.isNotBlank(prefix)) {
                blobs = storage.list(bucketName, 
                    Storage.BlobListOption.prefix(prefix),
                    Storage.BlobListOption.pageSize(maxResults));
            } else {
                blobs = storage.list(bucketName, 
                    Storage.BlobListOption.pageSize(maxResults));
            }
            
            List<Map<String, Object>> objects = new ArrayList<>();
            int count = 0;
            for (Blob blob : blobs.iterateAll()) {
                if (count >= maxResults) {
                    break;
                }
                
                Map<String, Object> obj = new HashMap<>();
                obj.put("name", blob.getName());
                obj.put("size", blob.getSize());
                obj.put("content_type", blob.getContentType());
                obj.put("created", blob.getCreateTime());
                obj.put("updated", blob.getUpdateTime());
                obj.put("md5", blob.getMd5());
                obj.put("generation", blob.getGeneration());
                
                objects.add(obj);
                count++;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("bucket", bucketName);
            result.put("count", objects.size());
            result.put("objects", objects);
            
            return new ToolExecuteResult(JSON.toJSONString(result));
            
        } catch (Exception e) {
            log.error("GcpStorage.list_objects failed", e);
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
            // Use default credentials (GOOGLE_APPLICATION_CREDENTIALS env var)
            return StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .build()
                    .getService();
        }
    }
}
