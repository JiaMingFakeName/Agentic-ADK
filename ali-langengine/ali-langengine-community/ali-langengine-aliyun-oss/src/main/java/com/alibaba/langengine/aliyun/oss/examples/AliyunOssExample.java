package com.alibaba.langengine.aliyun.oss.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.aliyun.oss.tool.AliyunOssListObjectsTool;
import com.alibaba.langengine.aliyun.oss.tool.AliyunOssPresignedDownloadTool;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import java.util.HashMap;
import java.util.Map;

public class AliyunOssExample {
    private static String env(String k) { return System.getenv(k); }

    public static void main(String[] args) {
        // 环境变量读取
        String ak = env("ALIYUN_AK");
        String sk = env("ALIYUN_SK");
        String region = env("ALIYUN_REGION");
        String bucket = env("ALIYUN_BUCKET");
        String key = env("ALIYUN_KEY");

        // 列对象
        Map<String, Object> listArgs = new HashMap<>();
        listArgs.put("access_key_id", ak);
        listArgs.put("access_key_secret", sk);
        listArgs.put("region", region);
        listArgs.put("bucket", bucket);
        listArgs.put("max_keys", 10);
        ToolExecuteResult listRes = new AliyunOssListObjectsTool().run(JSON.toJSONString(listArgs), new ExecutionContext());
        System.out.println("list_objects => " + listRes.getOutput());

        // 预签名下载
        if (key != null) {
            Map<String, Object> dlArgs = new HashMap<>();
            dlArgs.put("access_key_id", ak);
            dlArgs.put("access_key_secret", sk);
            dlArgs.put("region", region);
            dlArgs.put("bucket", bucket);
            dlArgs.put("key", key);
            dlArgs.put("expires_seconds", 300);
            ToolExecuteResult dlRes = new AliyunOssPresignedDownloadTool().run(JSON.toJSONString(dlArgs), new ExecutionContext());
            System.out.println("presigned_download => " + dlRes.getOutput());
        }
    }
}
