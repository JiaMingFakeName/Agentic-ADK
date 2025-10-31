package com.alibaba.langengine.gmail;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

public class GmailGetEmailTool extends BaseTool {

    private final String accessToken;

    public GmailGetEmailTool(String accessToken) {
        this.accessToken = accessToken;
        setName("gmail_get_email");
        setHumanName("Gmail 获取单封邮件");
        setDescription("根据 messageId 获取邮件详情（包含 payload）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"messageId\": {\"type\": \"string\"},\n" +
                "    \"format\": {\"type\": \"string\", \"enum\":[\"full\",\"metadata\",\"minimal\",\"raw\"], \"default\": \"full\"}\n" +
                "  },\n" +
                "  \"required\": [\"messageId\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String messageId = (String) args.get("messageId");
            String format = (String) args.getOrDefault("format", "full");

            String url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId + "?format=" + format;

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Gmail get failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Gmail get error: " + e.getMessage());
        }
    }
}


