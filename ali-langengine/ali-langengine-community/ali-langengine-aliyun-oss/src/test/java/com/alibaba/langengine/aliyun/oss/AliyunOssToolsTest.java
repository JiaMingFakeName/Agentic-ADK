package com.alibaba.langengine.aliyun.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.aliyun.oss.tool.AliyunOssListObjectsTool;
import com.alibaba.langengine.aliyun.oss.tool.AliyunOssPresignedDownloadTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AliyunOssToolsTest {

    private static String env(String key) {
        return System.getenv(key);
    }

    @Test
    void testListObjects() {
        String ak = env("ALIYUN_AK");
        String sk = env("ALIYUN_SK");
        String region = env("ALIYUN_REGION");
        String bucket = env("ALIYUN_BUCKET");
        Assumptions.assumeTrue(ak != null && sk != null && region != null && bucket != null,
                "skip: env ALIYUN_AK/SK/REGION/BUCKET not set");

        Map<String, Object> args = new HashMap<>();
        args.put("access_key_id", ak);
        args.put("access_key_secret", sk);
        args.put("region", region);
        args.put("bucket", bucket);
        args.put("max_keys", 10);

        ToolExecuteResult res = new AliyunOssListObjectsTool().run(JSON.toJSONString(args), new ExecutionContext());
        assertNotNull(res);
        assertNotNull(res.getOutput(), "list_objects should return output");
        assertFalse(res.getOutput().contains("InternalError"), "list_objects should not fail: " + res.getOutput());
    }

    @Test
    void testPresignedDownload() {
        String ak = env("ALIYUN_AK");
        String sk = env("ALIYUN_SK");
        String region = env("ALIYUN_REGION");
        String bucket = env("ALIYUN_BUCKET");
        String key = env("ALIYUN_KEY");
        Assumptions.assumeTrue(ak != null && sk != null && region != null && bucket != null && key != null,
                "skip: env ALIYUN_AK/SK/REGION/BUCKET/KEY not set");

        Map<String, Object> args = new HashMap<>();
        args.put("access_key_id", ak);
        args.put("access_key_secret", sk);
        args.put("region", region);
        args.put("bucket", bucket);
        args.put("key", key);
        args.put("expires_seconds", 300);

        ToolExecuteResult res = new AliyunOssPresignedDownloadTool().run(JSON.toJSONString(args), new ExecutionContext());
        assertNotNull(res);
        assertNotNull(res.getOutput(), "presigned_download should return output");
        assertFalse(res.getOutput().contains("InternalError"), "presigned_download should not fail: " + res.getOutput());
        assertTrue(res.getOutput().contains("http"), "presigned URL should contain http");
    }
}
