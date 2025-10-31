package com.alibaba.langengine.gmail;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.*;

import java.util.*;

public class GmailBatchModifyLabelsTool extends BaseTool {

    private final String accessToken;

    public GmailBatchModifyLabelsTool(String accessToken) {
        this.accessToken = accessToken;
        setName("gmail_batch_modify_labels");
        setHumanName("Gmail 批量标签修改");
        setDescription("对多封邮件批量添加/移除标签，常用于批量标记已读（移除 UNREAD）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"ids\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}},\n" +
                "    \"addLabelIds\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}},\n" +
                "    \"removeLabelIds\": {\"type\": \"array\", \"items\": {\"type\": \"string\"}}\n" +
                "  },\n" +
                "  \"required\": [\"ids\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            List<String> ids = (List<String>) args.get("ids");
            List<String> addLabelIds = (List<String>) args.getOrDefault("addLabelIds", Collections.emptyList());
            List<String> removeLabelIds = (List<String>) args.getOrDefault("removeLabelIds", Collections.emptyList());

            Map<String, Object> payload = new HashMap<>();
            payload.put("ids", ids);
            if (!addLabelIds.isEmpty()) payload.put("addLabelIds", addLabelIds);
            if (!removeLabelIds.isEmpty()) payload.put("removeLabelIds", removeLabelIds);

            Request req = new Request.Builder()
                    .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/batchModify")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON.toJSONString(payload), MediaType.parse("application/json")))
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Gmail batch modify failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success("{\"status\":\"ok\"}");
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Gmail batch modify error: " + e.getMessage());
        }
    }
}


