# MinIO/S3 Connector for AI Agents

This project is a Java-based library that provides an interface designed for AI invocation, used for interacting with MinIO or any S3-compatible object storage service.

The connector simplifies complex tasks like multipart uploads and presigned URL generation into simple method calls. These methods return structured `Map` data, which can be easily serialized to JSON for agent consumption and understanding.

---

## Core Features

- **Bucket Management**: Create and list buckets.
- **Object Listing**: List objects with support for prefixes, recursive listing, and pagination (`startAfter`).
- **Object Uploads**: Upload content as UTF-8 text or Base64-encoded binary data.
- **Object Downloads**: Download object content as either a UTF-8 string or a Base64-encoded string.
- **Multipart Uploads**: Automatically handles multipart uploads for large files with a configurable default part size.
- **Presigned URLs**: Generate temporary, shareable URLs for both downloading (GET) and uploading (PUT) objects.
- **Batch Operations**: Delete multiple objects in a single request.
- **Spring Boot Integration**: Provides seamless auto-configuration for Spring Boot 3 applications.
- **High-Performance HTTP Client**: Utilizes a finely-tuned OkHttp client for efficient and reliable communication.

---

## Prerequisites

- Java 17 or higher.
- Apache Maven.
- Access to a MinIO server or another S3-compatible object storage service.

---

## About the MinIO Version

To ensure a stable and reproducible environment, this project was developed and tested exclusively with a community release of MinIO. We strongly recommend using the following version, which has been validated against our entire test suite:

`minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1`

Pinning to this specific version prevents potential incompatibilities from future MinIO updates and ensures maximum compatibility with this connector.

---

## Quick Start: Setting up a MinIO Server

For local development and testing, the easiest way to get a MinIO server running is with Docker.

1.  **Pull the Docker Image:**
    ```bash
    docker pull minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1
    ```

2.  **Run the MinIO Container:**
    This command starts a MinIO server, exposes the S3 API port (9000) and the web console port (9001), and sets the default credentials.

    ```bash
    docker run -p 9000:9000 -p 9001:9001 \
        --name minio-dev \
        -e "MINIO_ROOT_USER=minioadmin" \
        -e "MINIO_ROOT_PASSWORD=minioadmin" \
        minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1 server /data --console-address ":9001"
    ```

-   **S3 Endpoint**: `http://localhost:9000`
-   **Access Key**: `minioadmin`
-   **Secret Key**: `minioadmin`
-   **Web Console**: `http://localhost:9001`

You can now use these details to configure the connector.

---

## Installation

Add the following dependency to your project's `pom.xml` file:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-minio</artifactId>
    <version>1.2.6-202508111516</version>
</dependency>
```

---

## Usage

### 1. Standalone Usage (Without Spring Boot)

You can directly instantiate and use the `MinioTool` by building a `MinioClient` instance.

```java
import com.alibaba.langengine.minio.MinioTool;
import io.minio.MinioClient;

public class MinioExample {
public static void main(String[] args) throws Exception {
// 1. Configure and build the MinioClient
MinioClient minioClient = MinioClient.builder()
.endpoint("http://localhost:9000")
.credentials("minioadmin", "minioadmin")
.build();

        // 2. Instantiate the MinioTool
        MinioTool tool = new MinioTool(minioClient);

        String bucketName = "my-agent-bucket";
        String objectName = "greeting.txt";
        String content = "Hello from AI Agent!";

        // 3. Use the tool to perform operations
        tool.createBucket(bucketName);
        System.out.println("Bucket created: " + bucketName);

        tool.uploadObject(bucketName, objectName, content, true, "text/plain", null, -1, -1);
        System.out.println("Object uploaded: " + objectName);

        var downloaded = tool.downloadObject(bucketName, objectName, true);
        System.out.println("Downloaded content: " + downloaded.get("content"));

        // 4. Clean up
        tool.deleteObject(bucketName, objectName);
        System.out.println("Object deleted.");
    }
}
```

### 2. With Spring Boot

This library provides auto-configuration, making integration effortless.

1.  **Add Configuration:**
    Add the MinIO connection details to your `src/main/resources/application.yml`.

    ```yaml
    com:
        alibaba:
            langengine:
                minio:
                    endpoint: localhost:9000
                    secure: false  # Use http for local development
                    access-key: minioadmin
                    secret-key: minioadmin
                    # Optional: Configure the HTTP client
                    http:
                        connect-timeout-sec: 10
                        read-timeout-sec: 180
    ```

2.  **Inject the Tool:**
    You can now directly inject `MinioTool` or `MinioTemplate` into any Spring component.

    ```java
    import com.alibaba.langengine.minio.MinioTool;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import java.util.Map;

    @Service
    public class StorageService {

        private final MinioTool minioTool;

        @Autowired
        public StorageService(MinioTool minioTool) {
            this.minioTool = minioTool;
        }

        public void storeAgentMemory(String memoryId, String memoryContent) throws Exception {
            minioTool.createBucket("agent-memory");
            minioTool.uploadObject(
                "agent-memory",
                memoryId + ".txt",
                memoryContent,
                true, // content is text
                "text/plain",
                Map.of("agent-id", "007"), // custom metadata
                -1,
                -1
            );
        }
    }
    ```

---

## AI Agent Tool API Overview

The `MinioTool` class is the primary interface intended for AI agents. All its public methods accept simple parameters (strings, booleans, maps) and return a `Map<String, Object>` which is easily convertible to JSON.

The core "action" is specified as a parameter in the agent's function call. The available actions include:

-   `create_bucket`
-   `list_buckets`
-   `list_objects`
-   `upload_object`
-   `download_object`
-   `delete_object`
-   `delete_objects` (batch)
-   `presigned_get`
-   `presigned_put`

---

## Building from Source

You can build the project and run tests using standard Maven commands.

-   **Package the application into a JAR:**
    ```bash
    mvn clean package
    ```

-   **Install the artifact into your local Maven repository:**
    ```bash
    mvn clean install
    ```

-   **Deploy the artifact to a remote Maven repository:**
    ```bash
    mvn clean deploy
    ```

---

## Running Tests

The project includes a comprehensive suite of integration tests that use **Testcontainers**. This means the tests will automatically start a MinIO Docker container to run against.

**Prerequisite**: You must have Docker installed and running on your system.

To run all tests, simply execute:

```bash
mvn test
```

---

## License

This project is licensed under the **Apache 2.0 License**.