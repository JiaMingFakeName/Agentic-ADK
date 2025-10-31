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
import java.util.*;
import java.util.Base64;

public class GmailSendEmailWithAttachmentTool extends BaseTool {

    private final String accessToken;

    public GmailSendEmailWithAttachmentTool(String accessToken) {
        this.accessToken = accessToken;
        setName("gmail_send_email_with_attachment");
        setHumanName("Gmail 发送带附件邮件");
        setDescription("通过 Gmail API 发送带附件的邮件（multipart/mixed）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"to\": {\"type\": \"string\"},\n" +
                "    \"subject\": {\"type\": \"string\"},\n" +
                "    \"html\": {\"type\": \"string\", \"description\": \"HTML 正文\"},\n" +
                "    \"text\": {\"type\": \"string\", \"description\": \"纯文本正文\"},\n" +
                "    \"attachments\": {\n" +
                "       \"type\": \"array\",\n" +
                "       \"items\": {\n" +
                "           \"type\": \"object\",\n" +
                "           \"properties\": {\n" +
                "              \"filename\": {\"type\": \"string\"},\n" +
                "              \"contentType\": {\"type\": \"string\"},\n" +
                "              \"contentBase64\": {\"type\": \"string\", \"description\": \"Base64 编码内容\"}\n" +
                "           },\n" +
                "           \"required\": [\"filename\", \"contentType\", \"contentBase64\"]\n" +
                "       }\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"to\", \"subject\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String to = (String) args.get("to");
            String subject = (String) args.get("subject");
            String html = (String) args.getOrDefault("html", null);
            String text = (String) args.getOrDefault("text", "");
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) args.getOrDefault("attachments", Collections.emptyList());

            String boundary = "=_JAVA_BOUNDARY_" + UUID.randomUUID();
            StringBuilder mime = new StringBuilder();
            mime.append("MIME-Version: 1.0\r\n");
            mime.append("To: ").append(to).append("\r\n");
            mime.append("Subject: ").append(subject).append("\r\n");
            mime.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\r\n\r\n");

            // body part (alternative)
            String altBoundary = boundary + "_ALT";
            mime.append("--").append(boundary).append("\r\n");
            mime.append("Content-Type: multipart/alternative; boundary=\"").append(altBoundary).append("\"\r\n\r\n");
            // text
            if (text != null && !text.isEmpty()) {
                mime.append("--").append(altBoundary).append("\r\n");
                mime.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
                mime.append(text).append("\r\n\r\n");
            }
            // html
            if (html != null && !html.isEmpty()) {
                mime.append("--").append(altBoundary).append("\r\n");
                mime.append("Content-Type: text/html; charset=UTF-8\r\n\r\n");
                mime.append(html).append("\r\n\r\n");
            }
            mime.append("--").append(altBoundary).append("--\r\n\r\n");

            // attachments
            for (Map<String, Object> att : attachments) {
                String filename = (String) att.get("filename");
                String contentType = (String) att.get("contentType");
                String contentBase64 = (String) att.get("contentBase64");
                mime.append("--").append(boundary).append("\r\n");
                mime.append("Content-Type: ").append(contentType).append("; name=\"").append(filename).append("\"\r\n");
                mime.append("Content-Transfer-Encoding: base64\r\n");
                mime.append("Content-Disposition: attachment; filename=\"").append(filename).append("\"\r\n\r\n");
                mime.append(contentBase64).append("\r\n\r\n");
            }
            mime.append("--").append(boundary).append("--\r\n");

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
                    return ToolExecuteResult.fail("Gmail send(attachment) failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Gmail send(attachment) error: " + e.getMessage());
        }
    }
}


