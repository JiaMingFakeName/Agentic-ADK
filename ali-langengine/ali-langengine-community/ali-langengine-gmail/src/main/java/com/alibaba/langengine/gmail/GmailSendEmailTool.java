package com.alibaba.langengine.gmail;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class GmailSendEmailTool extends BaseTool {

    private final String accessToken;

    public GmailSendEmailTool(String accessToken) {
        this.accessToken = accessToken;
        setName("gmail_send_email");
        setHumanName("Gmail 发送邮件");
        setDescription("通过 Gmail API 发送邮件");
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
                "      \"enum\": [\"text/plain\", \"text/html\"],\n" +
                "      \"default\": \"text/plain\"\n" +
                "    },\n" +
                "    \"cc\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"抄送，逗号分隔\"\n" +
                "    },\n" +
                "    \"bcc\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"密送，逗号分隔\"\n" +
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
            String contentType = (String) args.getOrDefault("contentType", "text/plain");
            String cc = (String) args.getOrDefault("cc", null);
            String bcc = (String) args.getOrDefault("bcc", null);

            StringBuilder mime = new StringBuilder();
            mime.append("Content-Type: ").append(contentType).append("; charset=UTF-8\r\n");
            mime.append("MIME-Version: 1.0\r\n");
            mime.append("To: ").append(to).append("\r\n");
            if (cc != null && !cc.isEmpty()) mime.append("Cc: ").append(cc).append("\r\n");
            if (bcc != null && !bcc.isEmpty()) mime.append("Bcc: ").append(bcc).append("\r\n");
            mime.append("Subject: ").append(subject).append("\r\n\r\n");
            mime.append(body);

            String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(mime.toString().getBytes(StandardCharsets.UTF_8));
            String payload = "{\"raw\":\"" + raw + "\"}";

            Request req = new Request.Builder()
                    .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(payload, MediaType.parse("application/json")))
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Gmail send failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Gmail send error: " + e.getMessage());
        }
    }
}


