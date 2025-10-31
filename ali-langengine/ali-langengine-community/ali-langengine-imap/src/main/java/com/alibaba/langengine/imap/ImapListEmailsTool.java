package com.alibaba.langengine.imap;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import javax.mail.*;
import java.util.*;

public class ImapListEmailsTool extends BaseTool {

    public ImapListEmailsTool() {
        setName("imap_list_emails");
        setHumanName("IMAP 列出邮件");
        setDescription("通过 IMAP 协议列出邮件（只读）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"host\": {\"type\": \"string\"},\n" +
                "    \"port\": {\"type\": \"integer\", \"default\": 993},\n" +
                "    \"username\": {\"type\": \"string\"},\n" +
                "    \"password\": {\"type\": \"string\"},\n" +
                "    \"folder\": {\"type\": \"string\", \"default\": \"INBOX\"},\n" +
                "    \"limit\": {\"type\": \"integer\", \"default\": 10}\n" +
                "  },\n" +
                "  \"required\": [\"host\", \"username\", \"password\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String host = (String) args.get("host");
            Integer port = (Integer) args.getOrDefault("port", 993);
            String username = (String) args.get("username");
            String password = (String) args.get("password");
            String folder = (String) args.getOrDefault("folder", "INBOX");
            Integer limit = (Integer) args.getOrDefault("limit", 10);

            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", host);
            props.put("mail.imaps.port", String.valueOf(port));
            props.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, username, password);
            Folder f = store.getFolder(folder);
            f.open(Folder.READ_ONLY);

            int count = f.getMessageCount();
            int start = Math.max(1, count - limit + 1);
            Message[] msgs = f.getMessages(start, count);

            List<Map<String, Object>> list = new ArrayList<>();
            for (Message m : msgs) {
                Map<String, Object> item = new HashMap<>();
                item.put("subject", m.getSubject());
                item.put("from", Arrays.toString(m.getFrom()));
                item.put("sentDate", m.getSentDate());
                list.add(item);
            }

            f.close(false);
            store.close();

            return ToolExecuteResult.success(JSON.toJSONString(list));
        } catch (Exception e) {
            return ToolExecuteResult.fail("IMAP list error: " + e.getMessage());
        }
    }
}


