package com.alibaba.langengine.gmail;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

public class GmailListEmailsTool extends BaseTool {

    private final String accessToken;

    public GmailListEmailsTool(String accessToken) {
        this.accessToken = accessToken;
        setName("gmail_list_emails");
        setHumanName("Gmail 列出邮件");
        setDescription("通过 Gmail API 按查询条件列出邮件");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索语法，如 from:xxx has:attachment\"\n" +
                "    },\n" +
                "    \"maxResults\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"default\": 10\n" +
                "    },\n" +
                "    \"pageToken\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"上一页返回的 nextPageToken，用于分页\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String query = (String) args.getOrDefault("query", "");
            Integer maxResults = (Integer) args.getOrDefault("maxResults", 10);

            HttpUrl.Builder ub = HttpUrl.parse("https://gmail.googleapis.com/gmail/v1/users/me/messages").newBuilder()
                    .addQueryParameter("q", query)
                    .addQueryParameter("maxResults", String.valueOf(maxResults));
            String pageToken = (String) args.getOrDefault("pageToken", null);
            if (pageToken != null && !pageToken.isEmpty()) {
                ub.addQueryParameter("pageToken", pageToken);
            }
            HttpUrl url = ub.build();

            Request req = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Gmail list failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Gmail list error: " + e.getMessage());
        }
    }
}


