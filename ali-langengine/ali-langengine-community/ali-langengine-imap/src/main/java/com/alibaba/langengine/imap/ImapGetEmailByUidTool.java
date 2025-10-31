package com.alibaba.langengine.imap;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import com.sun.mail.imap.*;
import java.io.InputStream;
import java.util.*;

public class ImapGetEmailByUidTool extends BaseTool {

    public ImapGetEmailByUidTool() {
        setName("imap_get_email_by_uid");
        setHumanName("IMAP 按 UID 获取邮件（含正文与附件占位）");
        setDescription("通过 IMAP UID 拉取邮件基础信息与正文（附件以占位记录元信息）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"host\": {\"type\": \"string\"},\n" +
                "    \"port\": {\"type\": \"integer\", \"default\": 993},\n" +
                "    \"username\": {\"type\": \"string\"},\n" +
                "    \"password\": {\"type\": \"string\"},\n" +
                "    \"folder\": {\"type\": \"string\", \"default\": \"INBOX\"},\n" +
                "    \"uid\": {\"type\": \"integer\"}\n" +
                "  },\n" +
                "  \"required\": [\"host\", \"username\", \"password\", \"uid\"]\n" +
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
            Number uidNum = (Number) args.get("uid");
            long uid = uidNum.longValue();

            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", host);
            props.put("mail.imaps.port", String.valueOf(port));
            props.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            IMAPStore store = (IMAPStore) session.getStore("imaps");
            store.connect(host, username, password);
            IMAPFolder f = (IMAPFolder) store.getFolder(folder);
            f.open(Folder.READ_ONLY);

            Message msg = f.getMessageByUID(uid);
            if (msg == null) {
                f.close(false);
                store.close();
                return ToolExecuteResult.fail("IMAP uid not found: " + uid);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("subject", msg.getSubject());
            data.put("from", Arrays.toString(msg.getFrom()));
            data.put("sentDate", msg.getSentDate());

            Object content = msg.getContent();
            if (content instanceof String) {
                data.put("body", content);
            } else if (content instanceof MimeMultipart) {
                MimeMultipart mp = (MimeMultipart) content;
                List<Map<String, Object>> parts = new ArrayList<>();
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);
                    Map<String, Object> part = new HashMap<>();
                    part.put("contentType", bp.getContentType());
                    part.put("fileName", bp.getFileName());
                    part.put("disposition", bp.getDisposition());
                    parts.add(part);
                }
                data.put("parts", parts);
            }

            f.close(false);
            store.close();

            return ToolExecuteResult.success(JSON.toJSONString(data));
        } catch (Exception e) {
            return ToolExecuteResult.fail("IMAP get by uid error: " + e.getMessage());
        }
    }
}


