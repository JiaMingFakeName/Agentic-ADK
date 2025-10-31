package com.alibaba.langengine.aliyun.oss.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliyunOssPutObjectTextTool extends BaseTool {

    public AliyunOssPutObjectTextTool() {
        setName("AliyunOss.put_object_text");
        setDescription("上传文本内容到对象");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key_id\": {\"type\": \"string\"},\n" +
                "    \"access_key_secret\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"endpoint\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"content\": {\"type\": \"string\"},\n" +
                "    \"content_type\": {\"type\": \"string\"},\n" +
                "    \"acl\": {\"type\": \"string\", \"enum\": [\"private\", \"public-read\"]}\n" +
                "  },\n" +
                "  \"required\": [\"access_key_id\", \"access_key_secret\", \"region\", \"bucket\", \"key\", \"content\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        OSS client = null;
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String accessKeyId = (String) args.get("access_key_id");
            String accessKeySecret = (String) args.get("access_key_secret");
            String region = (String) args.get("region");
            String endpoint = (String) args.getOrDefault("endpoint", String.format("oss-%s.aliyuncs.com", region));
            String bucket = (String) args.get("bucket");
            String key = (String) args.get("key");
            String content = (String) args.get("content");
            String contentType = (String) args.get("content_type");
            String acl = (String) args.get("acl");

            if (accessKeyId == null || accessKeySecret == null || region == null || bucket == null || key == null || content == null) {
                return new ToolExecuteResult("InvalidArgument: missing required credentials or bucket/key/content");
            }
            if (content.getBytes(StandardCharsets.UTF_8).length > 1024 * 1024) {
                return new ToolExecuteResult("TooLarge: content exceeds 1MB, please use presigned upload");
            }

            client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ObjectMetadata meta = new ObjectMetadata();
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            meta.setContentLength(bytes.length);
            meta.setContentType(contentType != null ? contentType : "text/plain; charset=utf-8");
            client.putObject(bucket, key, new java.io.ByteArrayInputStream(bytes), meta);
            if ("public-read".equalsIgnoreCase(acl)) {
                client.setObjectAcl(bucket, key, CannedAccessControlList.PublicRead);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("bucket", bucket);
            data.put("key", key);
            data.put("size", bytes.length);
            return new ToolExecuteResult(JSON.toJSONString(data));
        } catch (Exception e) {
            log.error("AliyunOss.put_object_text failed", e);
            return new ToolExecuteResult("InternalError: " + e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}
