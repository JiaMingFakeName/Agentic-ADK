# Ali-LangEngine-GCP-Storage

Google Cloud Storage Tool Calling module for Ali-LangEngine framework.

## Overview

This module provides Google Cloud Storage (GCS) integration for the Ali-LangEngine framework, enabling AI agents to interact with GCS buckets - list objects, upload/download files, generate presigned URLs, and manage storage objects.

## Features

- ✅ **GcpStorageListObjectsTool** - List objects in a GCS bucket
- ✅ **GcpStorageGetObjectTextTool** - Download text content from GCS objects
- ✅ **GcpStoragePutObjectTextTool** - Upload text content to GCS objects
- ✅ **GcpStorageDeleteObjectTool** - Delete objects from GCS buckets
- ✅ **GcpStoragePresignedDownloadTool** - Generate presigned download URLs
- ✅ **GcpStoragePresignedUploadTool** - Generate presigned upload URLs
- ✅ Support for service account authentication
- ✅ Configurable expiration times for presigned URLs

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-gcp-storage</artifactId>
    <version>1.2.6-202508111516</version>
</dependency>
```

## Prerequisites

1. **GCP Project**: You need a Google Cloud Platform project with Cloud Storage API enabled
2. **Service Account**: Create a service account with appropriate permissions
3. **Credentials**: Download the service account JSON key file

### Setting up GCP Credentials

**Option 1: Environment Variable (Recommended)**
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-key.json"
```

**Option 2: Pass credentials path in tool parameters**
```json
{
  "credentials_path": "/path/to/your/service-account-key.json"
}
```

## Configuration

### Environment Variables

```properties
# GCP Project ID
gcp_project_id=your-project-id

# Path to service account credentials JSON file
gcp_credentials_path=/path/to/credentials.json

# Default bucket name (optional)
gcp_default_bucket=your-default-bucket

# Request timeout in seconds (default: 60)
gcp_request_timeout=60
```

## Quick Start

### 1. List Objects in Bucket

```java
import com.alibaba.langengine.gcp.storage.tool.GcpStorageListObjectsTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.callback.ExecutionContext;

GcpStorageListObjectsTool tool = new GcpStorageListObjectsTool();

String input = "{\n" +
    "  \"project_id\": \"my-project\",\n" +
    "  \"bucket_name\": \"my-bucket\",\n" +
    "  \"prefix\": \"documents/\",\n" +
    "  \"max_results\": 10\n" +
    "}";

ToolExecuteResult result = tool.run(input, new ExecutionContext());
System.out.println(result.getOutput());
```

### 2. Upload Text File

```java
import com.alibaba.langengine.gcp.storage.tool.GcpStoragePutObjectTextTool;

GcpStoragePutObjectTextTool tool = new GcpStoragePutObjectTextTool();

String input = "{\n" +
    "  \"project_id\": \"my-project\",\n" +
    "  \"bucket_name\": \"my-bucket\",\n" +
    "  \"object_name\": \"documents/report.txt\",\n" +
    "  \"content\": \"This is the content of my report\",\n" +
    "  \"content_type\": \"text/plain\"\n" +
    "}";

ToolExecuteResult result = tool.run(input, new ExecutionContext());
System.out.println(result.getOutput());
```

### 3. Download Text File

```java
import com.alibaba.langengine.gcp.storage.tool.GcpStorageGetObjectTextTool;

GcpStorageGetObjectTextTool tool = new GcpStorageGetObjectTextTool();

String input = "{\n" +
    "  \"project_id\": \"my-project\",\n" +
    "  \"bucket_name\": \"my-bucket\",\n" +
    "  \"object_name\": \"documents/report.txt\"\n" +
    "}";

ToolExecuteResult result = tool.run(input, new ExecutionContext());
System.out.println(result.getOutput());
```

### 4. Generate Presigned Download URL

```java
import com.alibaba.langengine.gcp.storage.tool.GcpStoragePresignedDownloadTool;

GcpStoragePresignedDownloadTool tool = new GcpStoragePresignedDownloadTool();

String input = "{\n" +
    "  \"project_id\": \"my-project\",\n" +
    "  \"bucket_name\": \"my-bucket\",\n" +
    "  \"object_name\": \"documents/report.pdf\",\n" +
    "  \"expiration_minutes\": 60\n" +
    "}";

ToolExecuteResult result = tool.run(input, new ExecutionContext());
System.out.println(result.getOutput());
```

### 5. Generate Presigned Upload URL

```java
import com.alibaba.langengine.gcp.storage.tool.GcpStoragePresignedUploadTool;

GcpStoragePresignedUploadTool tool = new GcpStoragePresignedUploadTool();

String input = "{\n" +
    "  \"project_id\": \"my-project\",\n" +
    "  \"bucket_name\": \"my-bucket\",\n" +
    "  \"object_name\": \"uploads/newfile.pdf\",\n" +
    "  \"expiration_minutes\": 30,\n" +
    "  \"content_type\": \"application/pdf\"\n" +
    "}";

ToolExecuteResult result = tool.run(input, new ExecutionContext());
System.out.println(result.getOutput());
```

### 6. Delete Object

```java
import com.alibaba.langengine.gcp.storage.tool.GcpStorageDeleteObjectTool;

GcpStorageDeleteObjectTool tool = new GcpStorageDeleteObjectTool();

String input = "{\n" +
    "  \"project_id\": \"my-project\",\n" +
    "  \"bucket_name\": \"my-bucket\",\n" +
    "  \"object_name\": \"temp/oldfile.txt\"\n" +
    "}";

ToolExecuteResult result = tool.run(input, new ExecutionContext());
System.out.println(result.getOutput());
```

## Tool Specifications

### 1. GcpStorageListObjectsTool

**Name**: `GcpStorage.list_objects`

**Description**: List objects in a Google Cloud Storage bucket

**Parameters**:
- `project_id` (string, required): GCP Project ID
- `bucket_name` (string, required): Name of the GCS bucket
- `credentials_path` (string, optional): Path to credentials JSON
- `prefix` (string, optional): Filter objects by prefix
- `max_results` (integer, optional): Maximum number of results (default: 100)

**Example Output**:
```json
{
  "bucket": "my-bucket",
  "count": 3,
  "objects": [
    {
      "name": "documents/file1.txt",
      "size": 1024,
      "content_type": "text/plain",
      "created": 1640000000000,
      "updated": 1640000001000,
      "md5": "abc123...",
      "generation": 1640000000000000
    }
  ]
}
```

### 2. GcpStorageGetObjectTextTool

**Name**: `GcpStorage.get_object_text`

**Description**: Get text content from a GCS object

**Parameters**:
- `project_id` (string, required): GCP Project ID
- `bucket_name` (string, required): Name of the GCS bucket
- `object_name` (string, required): Name of the object to retrieve
- `credentials_path` (string, optional): Path to credentials JSON

### 3. GcpStoragePutObjectTextTool

**Name**: `GcpStorage.put_object_text`

**Description**: Upload text content to a GCS object

**Parameters**:
- `project_id` (string, required): GCP Project ID
- `bucket_name` (string, required): Name of the GCS bucket
- `object_name` (string, required): Name of the object to create/update
- `content` (string, required): Text content to upload
- `credentials_path` (string, optional): Path to credentials JSON
- `content_type` (string, optional): Content type (default: text/plain)

### 4. GcpStorageDeleteObjectTool

**Name**: `GcpStorage.delete_object`

**Description**: Delete an object from a GCS bucket

**Parameters**:
- `project_id` (string, required): GCP Project ID
- `bucket_name` (string, required): Name of the GCS bucket
- `object_name` (string, required): Name of the object to delete
- `credentials_path` (string, optional): Path to credentials JSON

### 5. GcpStoragePresignedDownloadTool

**Name**: `GcpStorage.presigned_download`

**Description**: Generate a presigned URL for downloading an object

**Parameters**:
- `project_id` (string, required): GCP Project ID
- `bucket_name` (string, required): Name of the GCS bucket
- `object_name` (string, required): Name of the object
- `expiration_minutes` (integer, optional): URL expiration time (default: 60)
- `credentials_path` (string, optional): Path to credentials JSON

### 6. GcpStoragePresignedUploadTool

**Name**: `GcpStorage.presigned_upload`

**Description**: Generate a presigned URL for uploading an object

**Parameters**:
- `project_id` (string, required): GCP Project ID
- `bucket_name` (string, required): Name of the GCS bucket
- `object_name` (string, required): Name of the object to upload
- `expiration_minutes` (integer, optional): URL expiration time (default: 60)
- `content_type` (string, optional): Content type for the upload
- `credentials_path` (string, optional): Path to credentials JSON

## Usage with Agents

```java
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.OpenAIAgent;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.gcp.storage.tool.*;

import java.util.Arrays;
import java.util.List;

// Create GCP Storage tools
List<BaseTool> tools = Arrays.asList(
    new GcpStorageListObjectsTool(),
    new GcpStorageGetObjectTextTool(),
    new GcpStoragePutObjectTextTool(),
    new GcpStorageDeleteObjectTool(),
    new GcpStoragePresignedDownloadTool(),
    new GcpStoragePresignedUploadTool()
);

// Create agent with tools
OpenAIAgent agent = new OpenAIAgent();
agent.setTools(tools);

// Create executor
AgentExecutor executor = AgentExecutor.builder()
    .agent(agent)
    .tools(tools)
    .build();

// Run agent
String result = executor.run("List all documents in my-bucket and read the content of report.txt");
System.out.println(result);
```

## Best Practices

1. **Use Service Accounts**: Always use service accounts with least privilege principle
2. **Secure Credentials**: Never commit credentials to source control
3. **Set Appropriate Expiration**: Use short expiration times for presigned URLs
4. **Handle Errors**: Always check the output for error messages
5. **Use Prefixes**: Use prefixes when listing objects to improve performance
6. **Content Type**: Always specify content_type when uploading files

## Security Considerations

1. **Credentials Management**: 
   - Use environment variables or secure key management systems
   - Rotate credentials regularly
   - Use IAM roles when running on GCP compute instances

2. **Access Control**:
   - Grant minimal required permissions to service accounts
   - Use bucket-level and object-level permissions appropriately
   - Enable audit logging

3. **Network Security**:
   - Use VPC Service Controls for sensitive data
   - Enable encryption at rest and in transit
   - Use private IPs for internal access

## Error Handling

Common errors and solutions:

### Authentication Error
```
Error: Could not load credentials
```
**Solution**: Ensure GOOGLE_APPLICATION_CREDENTIALS is set or credentials_path is provided

### Permission Denied
```
Error: Permission denied
```
**Solution**: Check service account has required IAM roles (e.g., Storage Object Admin)

### Bucket Not Found
```
Error: The specified bucket does not exist
```
**Solution**: Verify bucket name and that it exists in the specified project

### Object Not Found
```
Error: Object not found
```
**Solution**: Check object name including prefix/path

## IAM Permissions Required

Minimum IAM permissions needed:

- `storage.buckets.get` - To access bucket metadata
- `storage.objects.list` - To list objects
- `storage.objects.get` - To download objects
- `storage.objects.create` - To upload objects
- `storage.objects.delete` - To delete objects

Recommended IAM Role: `roles/storage.objectAdmin`

## Examples

See the test files for more examples:
- `GcpStorageToolsTest.java` - Unit tests and usage examples

## Resources

- [Google Cloud Storage Documentation](https://cloud.google.com/storage/docs)
- [GCS Java Client Library](https://cloud.google.com/java/docs/reference/google-cloud-storage/latest)
- [IAM Permissions Reference](https://cloud.google.com/storage/docs/access-control/iam-permissions)
- [Ali-LangEngine Documentation](../../README.md)

## License

Copyright (C) 2024 AIDC-AI

Licensed under the Apache License, Version 2.0
