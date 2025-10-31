package com.alibaba.langengine.imap;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import javax.mail.*;
import javax.mail.search.AndTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;
import java.util.*;

public class ImapSearchEmailsTool extends BaseTool {

    public ImapSearchEmailsTool() {
        setName("imap_search_emails");
        setHumanName("IMAP 搜索邮件");
        setDescription("通过 IMAP 按发件人/主题进行简单搜索");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"host\": {\"type\": \"string\"},\n" +
                "    \"port\": {\"type\": \"integer\", \"default\": 993},\n" +
                "    \"username\": {\"type\": \"string\"},\n" +
                "    \"password\": {\"type\": \"string\"},\n" +
                "    \"folder\": {\"type\": \"string\", \"default\": \"INBOX\"},\n" +
                "    \"from\": {\"type\": \"string\"},\n" +
                "    \"subject\": {\"type\": \"string\"},\n" +
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
            String from = (String) args.getOrDefault("from", null);
            String subject = (String) args.getOrDefault("subject", null);
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

            List<SearchTerm> terms = new ArrayList<>();
            if (from != null && !from.isEmpty()) terms.add(new FromStringTerm(from));
            if (subject != null && !subject.isEmpty()) terms.add(new SubjectTerm(subject));
            SearchTerm term;
            if (terms.isEmpty()) {
                term = null;
            } else if (terms.size() == 1) {
                term = terms.get(0);
            } else {
                term = new AndTerm(terms.get(0), terms.get(1));
            }

            Message[] msgs = term == null ? f.getMessages() : f.search(term);
            int count = msgs.length;
            int start = Math.max(0, count - limit);
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = count - 1; i >= start; i--) {
                Message m = msgs[i];
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
            return ToolExecuteResult.fail("IMAP search error: " + e.getMessage());
        }
    }
}


