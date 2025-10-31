package com.alibaba.langengine.aliyun.oss.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AliyunOssListObjectsTool extends BaseTool {

    public AliyunOssListObjectsTool() {
        setName("AliyunOss.list_objects");
        setDescription("列出指定 bucket 的对象，可选 prefix/delimiter/marker/max_keys");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key_id\": {\"type\": \"string\"},\n" +
                "    \"access_key_secret\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"endpoint\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"prefix\": {\"type\": \"string\"},\n" +
                "    \"delimiter\": {\"type\": \"string\"},\n" +
                "    \"marker\": {\"type\": \"string\"},\n" +
                "    \"max_keys\": {\"type\": \"integer\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key_id\", \"access_key_secret\", \"region\", \"bucket\"]\n" +
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
            String prefix = (String) args.getOrDefault("prefix", null);
            String delimiter = (String) args.getOrDefault("delimiter", null);
            String marker = (String) args.getOrDefault("marker", null);
            Integer maxKeys = args.get("max_keys") == null ? null : Integer.valueOf(String.valueOf(args.get("max_keys")));

            if (accessKeyId == null || accessKeySecret == null || region == null || bucket == null) {
                return new ToolExecuteResult("InvalidArgument: missing required credentials or bucket");
            }

            OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            try {
                ListObjectsRequest req = new ListObjectsRequest(bucket);
                if (prefix != null) req.setPrefix(prefix);
                if (delimiter != null) req.setDelimiter(delimiter);
                if (marker != null) req.setMarker(marker);
                if (maxKeys != null) req.setMaxKeys(maxKeys);

                List<OSSObjectSummary> summaries = client.listObjects(req).getObjectSummaries();
                List<Map<String, Object>> objects = summaries.stream().map(s -> {
                    Map<String, Object> o = new HashMap<>();
                    o.put("key", s.getKey());
                    o.put("size", s.getSize());
                    o.put("etag", s.getETag());
                    o.put("last_modified", s.getLastModified());
                    return o;
                }).collect(Collectors.toList());

                Map<String, Object> data = new HashMap<>();
                data.put("objects", objects);
                return new ToolExecuteResult(JSON.toJSONString(data));
            } finally {
                client.shutdown();
            }
        } catch (Exception e) {
            log.error("AliyunOss.list_objects failed", e);
            return new ToolExecuteResult("InternalError: " + e.getMessage());
        }
    }
}
