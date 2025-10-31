package com.alibaba.langengine.outlook;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class OutlookMarkReadTool extends BaseTool {

    private final String accessToken;

    public OutlookMarkReadTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_mark_read");
        setHumanName("Outlook 标记已读");
        setDescription("将指定 messageId 的邮件设置为已读");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"messageId\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"messageId\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String messageId = (String) args.get("messageId");

            String url = "https://graph.microsoft.com/v1.0/me/messages/" + messageId;
            Map<String, Object> payload = new HashMap<>();
            payload.put("isRead", true);

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .patch(RequestBody.create(JSON.toJSONString(payload), MediaType.parse("application/json")))
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Outlook mark read failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook mark read error: " + e.getMessage());
        }
    }
}


