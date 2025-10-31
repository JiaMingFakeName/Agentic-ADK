package com.alibaba.langengine.outlook;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

public class OutlookGetAttachmentTool extends BaseTool {

    private final String accessToken;

    public OutlookGetAttachmentTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_get_attachment");
        setHumanName("Outlook 下载附件");
        setDescription("根据 messageId 与 attachmentId 下载附件（contentBytes base64）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"messageId\": {\"type\": \"string\"},\n" +
                "    \"attachmentId\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"messageId\", \"attachmentId\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String messageId = (String) args.get("messageId");
            String attachmentId = (String) args.get("attachmentId");

            String url = "https://graph.microsoft.com/v1.0/me/messages/" + messageId + "/attachments/" + attachmentId;

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Outlook attachment failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook attachment error: " + e.getMessage());
        }
    }
}


