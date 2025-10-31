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

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GCP Storage Tools Test
 * 
 * Note: These tests require valid GCP credentials to run.
 * Set GOOGLE_APPLICATION_CREDENTIALS environment variable or provide credentials_path.
 * 
 * @author LangEngine Team
 */
public class GcpStorageToolsTest {
    
    private static final String TEST_PROJECT_ID = "your-gcp-project-id";
    private static final String TEST_BUCKET = "your-test-bucket";
    
    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_APPLICATION_CREDENTIALS", matches = ".*")
    public void testListObjects() {
        GcpStorageListObjectsTool tool = new GcpStorageListObjectsTool();
        
        String input = String.format("{\"project_id\": \"%s\", \"bucket_name\": \"%s\", \"max_results\": 5}",
                TEST_PROJECT_ID, TEST_BUCKET);
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        
        assertNotNull(result);
        assertNotNull(result.getOutput());
        System.out.println("List Objects Result:");
        System.out.println(result.getOutput());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_APPLICATION_CREDENTIALS", matches = ".*")
    public void testPutAndGetObject() {
        GcpStoragePutObjectTextTool putTool = new GcpStoragePutObjectTextTool();
        GcpStorageGetObjectTextTool getTool = new GcpStorageGetObjectTextTool();
        
        String objectName = "test-file-" + System.currentTimeMillis() + ".txt";
        String content = "This is a test file created by GCP Storage Tool";
        
        // Put object
        String putInput = String.format(
                "{\"project_id\": \"%s\", \"bucket_name\": \"%s\", \"object_name\": \"%s\", \"content\": \"%s\"}",
                TEST_PROJECT_ID, TEST_BUCKET, objectName, content);
        
        ToolExecuteResult putResult = putTool.run(putInput, new ExecutionContext());
        assertNotNull(putResult);
        System.out.println("Put Object Result:");
        System.out.println(putResult.getOutput());
        
        // Get object
        String getInput = String.format(
                "{\"project_id\": \"%s\", \"bucket_name\": \"%s\", \"object_name\": \"%s\"}",
                TEST_PROJECT_ID, TEST_BUCKET, objectName);
        
        ToolExecuteResult getResult = getTool.run(getInput, new ExecutionContext());
        assertNotNull(getResult);
        assertTrue(getResult.getOutput().contains(content));
        System.out.println("Get Object Result:");
        System.out.println(getResult.getOutput());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_APPLICATION_CREDENTIALS", matches = ".*")
    public void testPresignedUrls() {
        GcpStoragePresignedDownloadTool downloadTool = new GcpStoragePresignedDownloadTool();
        GcpStoragePresignedUploadTool uploadTool = new GcpStoragePresignedUploadTool();
        
        String objectName = "test-presigned-" + System.currentTimeMillis() + ".txt";
        
        // Generate presigned download URL
        String downloadInput = String.format(
                "{\"project_id\": \"%s\", \"bucket_name\": \"%s\", \"object_name\": \"%s\", \"expiration_minutes\": 30}",
                TEST_PROJECT_ID, TEST_BUCKET, objectName);
        
        ToolExecuteResult downloadResult = downloadTool.run(downloadInput, new ExecutionContext());
        assertNotNull(downloadResult);
        assertTrue(downloadResult.getOutput().contains("url"));
        System.out.println("Presigned Download URL:");
        System.out.println(downloadResult.getOutput());
        
        // Generate presigned upload URL
        String uploadInput = String.format(
                "{\"project_id\": \"%s\", \"bucket_name\": \"%s\", \"object_name\": \"%s\", \"expiration_minutes\": 30, \"content_type\": \"text/plain\"}",
                TEST_PROJECT_ID, TEST_BUCKET, objectName);
        
        ToolExecuteResult uploadResult = uploadTool.run(uploadInput, new ExecutionContext());
        assertNotNull(uploadResult);
        assertTrue(uploadResult.getOutput().contains("url"));
        System.out.println("Presigned Upload URL:");
        System.out.println(uploadResult.getOutput());
    }
    
    @Test
    public void testInvalidInput() {
        GcpStorageListObjectsTool tool = new GcpStorageListObjectsTool();
        
        // Missing required fields
        String input = "{\"project_id\": \"test\"}";
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        
        assertNotNull(result);
        assertTrue(result.getOutput().contains("Error"));
    }
}
