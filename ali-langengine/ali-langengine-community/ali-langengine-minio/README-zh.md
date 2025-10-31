# 专为 AI 代理设计的 MinIO/S3 连接器

本项目是一个基于 Java 的工具库，提供了一个专为AI调用设计的接口，用于与 MinIO 或任何 S3 兼容的对象存储服务进行交互。

该连接器将分片上传、预签名 URL 生成等复杂任务简化为简单的方法调用。这些方法返回结构化的 `Map` 数据，可以被轻松序列化为 JSON 格式，便于 AI 代理理解和使用。

---

## 核心功能

- **存储桶管理**: 创建和列出存储桶。
- **对象列表**: 支持按前缀、递归查询以及分页（`startAfter`）功能来列出对象。
- **对象上传**: 支持将 UTF-8 编码的文本或 Base64 编码的二进制内容作为对象上传。
- **对象下载**: 支持将对象内容下载为 UTF-8 字符串或 Base64 编码的字符串。
- **分片上传**: 自动处理大文件的分片上传，并支持配置默认的分片大小。
- **预签名 URL**: 为下载（GET）和上传（PUT）操作生成临时的、可公开共享的 URL。
- **批量操作**: 支持在一个请求中删除多个对象。
- **Spring Boot 集成**: 为 Spring Boot 3 应用程序提供无缝的自动配置支持。
- **高性能 HTTP 客户端**: 内置经过优化的 OkHttp 客户端，确保通信的高效与可靠。

---

## 环境要求

- Java 17 或更高版本。
- Apache Maven 项目管理工具。
- 能够访问一个 MinIO 服务器或其它 S3 兼容的对象存储服务。

---

## 关于 MinIO 版本

为确保环境的稳定性和可复现性，本项目在开发和测试中统一使用 MinIO 的社区发行版。我们强烈建议您使用以下经过我们完整测试套件验证的版本：

`minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1`

明确指定此版本可避免因未来 MinIO 更新可能带来的不兼容问题，确保与本连接器的最大兼容性。

---

## 快速入门：搭建 MinIO 服务器

在本地开发和测试时，使用 Docker 是启动 MinIO 服务器最快捷的方式。

1.  **拉取 Docker 镜像:**
    ```bash
    docker pull minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1
    ```

2.  **运行 MinIO 容器:**
    以下命令会启动一个 MinIO 服务器，开放 S3 API 端口（9000）和网页控制台端口（9001），并设置默认的管理员凭证。

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
-   **网页控制台**: `http://localhost:9001`

现在您可以使用这些信息来配置本连接器。

---

## 安装

将以下依赖项添加到您项目的 `pom.xml` 文件中：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-minio</artifactId>
    <version>1.2.6-202508111516</version>
</dependency>
```

---

## 使用方法

### 1. 独立使用 (非 Spring Boot 环境)

您可以手动构建 `MinioClient` 实例，然后直接实例化并使用 `MinioTool`。

```java
import com.alibaba.langengine.minio.MinioTool;
import io.minio.MinioClient;

public class MinioExample {
public static void main(String[] args) throws Exception {
// 1. 配置并构建 MinioClient
MinioClient minioClient = MinioClient.builder()
.endpoint("http://localhost:9000")
.credentials("minioadmin", "minioadmin")
.build();

        // 2. 实例化 MinioTool
        MinioTool tool = new MinioTool(minioClient);

        String bucketName = "my-agent-bucket";
        String objectName = "greeting.txt";
        String content = "来自 AI 代理的问候！";

        // 3. 使用工具执行操作
        tool.createBucket(bucketName);
        System.out.println("存储桶已创建: " + bucketName);

        tool.uploadObject(bucketName, objectName, content, true, "text/plain", null, -1, -1);
        System.out.println("对象已上传: " + objectName);

        var downloaded = tool.downloadObject(bucketName, objectName, true);
        System.out.println("下载的内容: " + downloaded.get("content"));

        // 4. 清理资源
        tool.deleteObject(bucketName, objectName);
        System.out.println("对象已删除。");
    }
}
```

### 2. 在 Spring Boot 中使用

本库提供了自动配置功能，使得集成过程非常简单。

1.  **添加配置:**
    将 MinIO 的连接信息添加到您的 `src/main/resources/application.yml` 文件中。

    ```yaml
    com:
        alibaba:
            langengine:
                minio:
                    endpoint: localhost:9000
                    secure: false  # 本地开发环境使用 http
                    access-key: minioadmin
                    secret-key: minioadmin
                    # 可选: 调优 HTTP 客户端
                    http:
                        connect-timeout-sec: 10
                        read-timeout-sec: 180
    ```

2.  **注入工具类:**
    现在，您可以直接在任何 Spring 组件中注入 `MinioTool` 或 `MinioTemplate`。

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
                true, // 内容是文本
                "text/plain",
                Map.of("agent-id", "007"), // 自定义元数据
                -1,
                -1
            );
        }
    }
    ```

---

## AI 代理工具 API 概览

`MinioTool` 类是为 AI 代理设计的主要接口。它的所有公共方法都接受简单类型的参数（如字符串、布尔值、Map），并返回一个 `Map<String, Object>` 结果，该结果可以轻松地转换为 JSON。

AI 代理在进行函数调用时，通过一个核心的 `action` 参数来指定要执行的操作。可用的 `action` 包括：

-   `create_bucket`
-   `list_buckets`
-   `list_objects`
-   `upload_object`
-   `download_object`
-   `delete_object`
-   `delete_objects` (批量)
-   `presigned_get`
-   `presigned_put`

---

## 从源码构建

您可以使用标准的 Maven 命令来构建项目和运行测试。

-   **将项目打包成 JAR 文件:**
    ```bash
    mvn clean package
    ```

-   **将构建产物安装到本地 Maven 仓库:**
    ```bash
    mvn clean install
    ```

-   **将构建产物部署到远程 Maven 仓库:**
    ```bash
    mvn clean deploy
    ```

---

## 运行测试

本项目包含了一套完整的集成测试，这些测试依赖于 **Testcontainers** 框架。这意味着在运行测试时，会自动启动一个 MinIO 的 Docker 容器作为测试环境。

**先决条件**: 您的系统中必须已安装并正在运行 Docker。

要运行所有测试，只需执行以下命令：

```bash
mvn test
```

---

## 许可证

本项目遵循 **Apache 2.0 许可证**。