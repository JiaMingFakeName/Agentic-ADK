package com.alibaba.langengine.outlook;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.*;

import java.util.*;

public class OutlookBatchMarkReadTool extends BaseTool {

    private final String accessToken;

    public OutlookBatchMarkReadTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_batch_mark_read");
        setHumanName("Outlook 批量标记已读");
        setDescription("对多封邮件批量设置 isRead=true，使用 $batch 端点");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"ids\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}\n" +
                "  },\n" +
                "  \"required\": [\"ids\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            List<String> ids = (List<String>) args.get("ids");

            List<Map<String, Object>> requests = new ArrayList<>();
            int i = 1;
            for (String id : ids) {
                Map<String, Object> body = new HashMap<>();
                body.put("isRead", true);
                Map<String, Object> req = new HashMap<>();
                req.put("id", String.valueOf(i++));
                req.put("method", "PATCH");
                req.put("url", "/me/messages/" + id);
                req.put("body", body);
                req.put("headers", Collections.singletonMap("Content-Type", "application/json"));
                requests.add(req);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("requests", requests);

            Request req = new Request.Builder()
                    .url("https://graph.microsoft.com/v1.0/$batch")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON.toJSONString(payload), MediaType.parse("application/json")))
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Outlook batch read failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook batch read error: " + e.getMessage());
        }
    }
}


