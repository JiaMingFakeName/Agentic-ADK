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

public class OutlookSendEmailWithAttachmentTool extends BaseTool {

    private final String accessToken;

    public OutlookSendEmailWithAttachmentTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_send_email_with_attachment");
        setHumanName("Outlook 发送带附件邮件");
        setDescription("通过 Microsoft Graph 发送带附件邮件");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"to\": {\"type\": \"string\"},\n" +
                "    \"subject\": {\"type\": \"string\"},\n" +
                "    \"html\": {\"type\": \"string\"},\n" +
                "    \"text\": {\"type\": \"string\"},\n" +
                "    \"attachments\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"filename\": {\"type\": \"string\"},\n" +
                "          \"contentType\": {\"type\": \"string\"},\n" +
                "          \"contentBase64\": {\"type\": \"string\"}\n" +
                "        },\n" +
                "        \"required\": [\"filename\", \"contentType\", \"contentBase64\"]\n" +
                "      }\n" +
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

            List<Map<String, Object>> toRecipients = new ArrayList<>();
            for (String addr : to.split(",")) {
                toRecipients.add(Collections.singletonMap("emailAddress", Collections.singletonMap("address", addr.trim())));
            }

            Map<String, Object> body = new HashMap<>();
            if (html != null && !html.isEmpty()) {
                body.put("contentType", "HTML");
                body.put("content", html);
            } else {
                body.put("contentType", "Text");
                body.put("content", text);
            }

            List<Map<String, Object>> attList = new ArrayList<>();
            for (Map<String, Object> att : attachments) {
                Map<String, Object> item = new HashMap<>();
                item.put("@odata.type", "#microsoft.graph.fileAttachment");
                item.put("name", att.get("filename"));
                item.put("contentType", att.get("contentType"));
                item.put("contentBytes", att.get("contentBase64"));
                attList.add(item);
            }

            Map<String, Object> message = new HashMap<>();
            message.put("subject", subject);
            message.put("body", body);
            message.put("toRecipients", toRecipients);
            if (!attList.isEmpty()) message.put("attachments", attList);

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
                    return ToolExecuteResult.fail("Outlook send(attachment) failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success("{\"status\":\"ok\"}");
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook send(attachment) error: " + e.getMessage());
        }
    }
}


