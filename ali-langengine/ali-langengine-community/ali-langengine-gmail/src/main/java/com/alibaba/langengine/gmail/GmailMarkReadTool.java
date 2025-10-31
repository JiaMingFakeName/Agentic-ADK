package com.alibaba.langengine.gmail;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.*;

import java.util.*;

public class GmailMarkReadTool extends BaseTool {

    private final String accessToken;

    public GmailMarkReadTool(String accessToken) {
        this.accessToken = accessToken;
        setName("gmail_mark_read");
        setHumanName("Gmail 标记已读");
        setDescription("将指定 messageId 的邮件移除 UNREAD 标签");
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

            String url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId + "/modify";
            Map<String, Object> payload = new HashMap<>();
            payload.put("removeLabelIds", Collections.singletonList("UNREAD"));

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON.toJSONString(payload), MediaType.parse("application/json")))
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Gmail mark read failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Gmail mark read error: " + e.getMessage());
        }
    }
}


