package com.alibaba.langengine.aliyun.oss.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliyunOssDeleteObjectTool extends BaseTool {

    public AliyunOssDeleteObjectTool() {
        setName("AliyunOss.delete_object");
        setDescription("删除对象");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key_id\": {\"type\": \"string\"},\n" +
                "    \"access_key_secret\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"endpoint\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"}\n" +
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
                return new ToolExecuteResult("InvalidArgument: " + "missing required credentials or bucket/key");
            }

            client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            client.deleteObject(bucket, key);

            Map<String, Object> data = new HashMap<>();
            data.put("deleted", true);
            data.put("bucket", bucket);
            data.put("key", key);
            return new ToolExecuteResult(data.toString());
        } catch (Exception e) {
            log.error("AliyunOss.delete_object failed", e);
            return new ToolExecuteResult("InternalError" + e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}
