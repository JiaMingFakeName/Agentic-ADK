package com.alibaba.langengine.outlook;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.*;

import java.util.*;

public class OutlookBatchMoveFolderTool extends BaseTool {

    private final String accessToken;

    public OutlookBatchMoveFolderTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_batch_move_folder");
        setHumanName("Outlook 批量移动到文件夹");
        setDescription("批量将邮件移动到指定文件夹，使用 $batch 端点");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"ids\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}},\n" +
                "    \"destinationFolderId\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"ids\", \"destinationFolderId\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            List<String> ids = (List<String>) args.get("ids");
            String destinationFolderId = (String) args.get("destinationFolderId");

            List<Map<String, Object>> requests = new ArrayList<>();
            int i = 1;
            for (String id : ids) {
                Map<String, Object> body = new HashMap<>();
                body.put("destinationId", destinationFolderId);
                Map<String, Object> req = new HashMap<>();
                req.put("id", String.valueOf(i++));
                req.put("method", "POST");
                req.put("url", "/me/messages/" + id + "/move");
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
                    return ToolExecuteResult.fail("Outlook batch move failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook batch move error: " + e.getMessage());
        }
    }
}


