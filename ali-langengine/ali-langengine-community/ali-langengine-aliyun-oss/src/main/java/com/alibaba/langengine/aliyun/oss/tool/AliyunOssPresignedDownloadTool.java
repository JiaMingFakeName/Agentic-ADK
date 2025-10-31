package com.alibaba.langengine.aliyun.oss.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliyunOssPresignedDownloadTool extends BaseTool {

    public AliyunOssPresignedDownloadTool() {
        setName("AliyunOss.get_presigned_url_download");
        setDescription("生成对象下载的预签名 URL");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key_id\": {\"type\": \"string\"},\n" +
                "    \"access_key_secret\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"endpoint\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"expires_seconds\": {\"type\": \"integer\", \"default\": 600},\n" +
                "    \"response_content_type\": {\"type\": \"string\"},\n" +
                "    \"response_content_disposition\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key_id\", \"access_key_secret\", \"region\", \"bucket\", \"key\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String accessKeyId = (String) args.get("access_key_id");
            String accessKeySecret = (String) args.get("access_key_secret");
            String region = (String) args.get("region");
            String endpoint = (String) args.getOrDefault("endpoint", String.format("oss-%s.aliyuncs.com", region));
            String bucket = (String) args.get("bucket");
            String key = (String) args.get("key");
            Integer expires = args.get("expires_seconds") == null ? 600 : Integer.valueOf(String.valueOf(args.get("expires_seconds")));

            if (expires < 60) expires = 60;
            if (expires > 7 * 24 * 3600) expires = 7 * 24 * 3600;

            if (accessKeyId == null || accessKeySecret == null || region == null || bucket == null || key == null) {
                return new ToolExecuteResult("InvalidArgument: missing required credentials or bucket/key");
            }

            OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            try {
                Date expiration = new Date(System.currentTimeMillis() + expires * 1000L);
                URL url = client.generatePresignedUrl(bucket, key, expiration, HttpMethod.GET);

                Map<String, Object> data = new HashMap<>();
                data.put("url", url.toString());
                data.put("expires_at", expiration.getTime() / 1000);
                return new ToolExecuteResult(JSON.toJSONString(data));
            } finally {
                client.shutdown();
            }
        } catch (Exception e) {
            log.error("AliyunOss.get_presigned_url_download failed", e);
            return new ToolExecuteResult("InternalError: " + e.getMessage());
        }
    }
}
