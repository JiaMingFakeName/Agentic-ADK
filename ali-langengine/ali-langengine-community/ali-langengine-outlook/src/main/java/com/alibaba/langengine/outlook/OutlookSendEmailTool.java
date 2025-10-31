package com.alibaba.langengine.outlook;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.*;

public class OutlookSendEmailTool extends BaseTool {

    private final String accessToken;

    public OutlookSendEmailTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_send_email");
        setHumanName("Outlook 发送邮件");
        setDescription("通过 Microsoft Graph 发送邮件");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"to\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"收件人，逗号分隔\"\n" +
                "    },\n" +
                "    \"subject\": {\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"contentType\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"Text\", \"HTML\"],\n" +
                "      \"default\": \"Text\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"to\", \"subject\", \"body\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String to = (String) args.get("to");
            String subject = (String) args.get("subject");
            String body = (String) args.get("body");
            String contentType = (String) args.getOrDefault("contentType", "Text");

            List<Map<String, Object>> toRecipients = new ArrayList<>();
            for (String addr : to.split(",")) {
                toRecipients.add(Collections.singletonMap("emailAddress", Collections.singletonMap("address", addr.trim())));
            }

            Map<String, Object> message = new HashMap<>();
            message.put("subject", subject);
            Map<String, Object> bodyObj = new HashMap<>();
            bodyObj.put("contentType", contentType);
            bodyObj.put("content", body);
            message.put("body", bodyObj);
            message.put("toRecipients", toRecipients);

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("saveToSentItems", true);

            Request req = new Request.Builder()
                    .url("https://graph.microsoft.com/v1.0/me/sendMail")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON.toJSONString(payload), MediaType.parse("application/json")))
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Outlook send failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success("{\"status\":\"ok\"}");
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook send error: " + e.getMessage());
        }
    }
}


