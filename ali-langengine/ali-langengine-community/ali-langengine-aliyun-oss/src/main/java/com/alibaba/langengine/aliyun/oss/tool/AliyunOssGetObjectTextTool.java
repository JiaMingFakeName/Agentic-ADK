package com.alibaba.langengine.aliyun.oss.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliyunOssGetObjectTextTool extends BaseTool {

    public AliyunOssGetObjectTextTool() {
        setName("AliyunOss.get_object_text");
        setDescription("下载对象文本内容（小文件）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key_id\": {\"type\": \"string\"},\n" +
                "    \"access_key_secret\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"endpoint\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"range_start\": {\"type\": \"integer\"},\n" +
                "    \"range_end\": {\"type\": \"integer\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key_id\", \"access_key_secret\", \"region\", \"bucket\", \"key\"]\n" +
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

            if (accessKeyId == null || accessKeySecret == null || region == null || bucket == null || key == null) {
                return new ToolExecuteResult("InvalidArgument: missing required credentials or bucket/key" );
            }

            client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            try (InputStream in = client.getObject(bucket, key).getObjectContent();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                int maxBytes = 1024 * 1024;
                int total = 0;
                while ((n = in.read(buf)) > 0) {
                    total += n;
                    if (total > maxBytes) {
                        return new ToolExecuteResult("TooLarge: object exceeds 1MB, please use presigned download");
                    }
                    out.write(buf, 0, n);
                }
                String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
                Map<String, Object> data = new HashMap<>();
                data.put("content", text);
                data.put("size", total);
                return new ToolExecuteResult(data.toString());
            }
        } catch (Exception e) {
            log.error("AliyunOss.get_object_text failed", e);
            return new ToolExecuteResult("InternalError" + e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}
