package com.alibaba.langengine.outlook;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

public class OutlookListEmailsTool extends BaseTool {

    private final String accessToken;

    public OutlookListEmailsTool(String accessToken) {
        this.accessToken = accessToken;
        setName("outlook_list_emails");
        setHumanName("Outlook 列出邮件");
        setDescription("通过 Microsoft Graph 按条件列出收件箱邮件");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"top\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"default\": 10\n" +
                "    },\n" +
                "    \"filter\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"$filter 语法\"\n" +
                "    },\n" +
                "    \"search\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"?search= 语法\"\n" +
                "    },\n" +
                "    \"skiptoken\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"上一页返回的 @odata.nextLink 中的 $skiptoken\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            Integer top = (Integer) args.getOrDefault("top", 10);
            String filter = (String) args.getOrDefault("filter", null);
            String search = (String) args.getOrDefault("search", null);

            HttpUrl.Builder b = HttpUrl.parse("https://graph.microsoft.com/v1.0/me/mailFolders/Inbox/messages").newBuilder()
                    .addQueryParameter("$top", String.valueOf(top));
            if (filter != null && !filter.isEmpty()) b.addQueryParameter("$filter", filter);
            if (search != null && !search.isEmpty()) b.addQueryParameter("$search", search);
            String skiptoken = (String) args.getOrDefault("skiptoken", null);
            if (skiptoken != null && !skiptoken.isEmpty()) b.addQueryParameter("$skiptoken", skiptoken);

            Request req = new Request.Builder()
                    .url(b.build())
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();

            OkHttpClient client = new OkHttpClient();
            try (Response resp = client.newCall(req).execute()) {
                String respBody = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    return ToolExecuteResult.fail("Outlook list failed: " + resp.code() + " " + respBody);
                }
                return ToolExecuteResult.success(respBody);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("Outlook list error: " + e.getMessage());
        }
    }
}


