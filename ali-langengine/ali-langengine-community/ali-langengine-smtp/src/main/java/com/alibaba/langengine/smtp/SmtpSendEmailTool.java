package com.alibaba.langengine.smtp;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

public class SmtpSendEmailTool extends BaseTool {

    public SmtpSendEmailTool() {
        setName("smtp_send_email");
        setHumanName("SMTP 发送邮件");
        setDescription("通过 SMTP 协议发送邮件");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"host\": {\"type\": \"string\"},\n" +
                "    \"port\": {\"type\": \"integer\", \"default\": 587},\n" +
                "    \"username\": {\"type\": \"string\"},\n" +
                "    \"password\": {\"type\": \"string\"},\n" +
                "    \"from\": {\"type\": \"string\"},\n" +
                "    \"to\": {\"type\": \"string\", \"description\": \"收件人，逗号分隔\"},\n" +
                "    \"subject\": {\"type\": \"string\"},\n" +
                "    \"body\": {\"type\": \"string\"},\n" +
                "    \"useTls\": {\"type\": \"boolean\", \"default\": true}\n" +
                "  },\n" +
                "  \"required\": [\"host\", \"username\", \"password\", \"from\", \"to\", \"subject\", \"body\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext ctx) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String host = (String) args.get("host");
            Integer port = (Integer) args.getOrDefault("port", 587);
            String username = (String) args.get("username");
            String password = (String) args.get("password");
            String from = (String) args.get("from");
            String to = (String) args.get("to");
            String subject = (String) args.get("subject");
            String body = (String) args.get("body");
            Boolean useTls = (Boolean) args.getOrDefault("useTls", true);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", String.valueOf(useTls));
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            for (String addr : to.split(",")) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(addr.trim()));
            }
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);
            return ToolExecuteResult.success("{\"status\":\"ok\"}");
        } catch (Exception e) {
            return ToolExecuteResult.fail("SMTP send error: " + e.getMessage());
        }
    }
}


