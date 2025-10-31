package com.alibaba.langengine.smtp;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class SmtpSendHtmlOrAttachmentEmailTool extends BaseTool {

    public SmtpSendHtmlOrAttachmentEmailTool() {
        setName("smtp_send_html_or_attachment_email");
        setHumanName("SMTP 发送HTML/带附件邮件");
        setDescription("通过 SMTP 发送 HTML 与附件（multipart/mixed）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"host\": {\"type\": \"string\"},\n" +
                "    \"port\": {\"type\": \"integer\", \"default\": 587},\n" +
                "    \"username\": {\"type\": \"string\"},\n" +
                "    \"password\": {\"type\": \"string\"},\n" +
                "    \"from\": {\"type\": \"string\"},\n" +
                "    \"to\": {\"type\": \"string\"},\n" +
                "    \"subject\": {\"type\": \"string\"},\n" +
                "    \"html\": {\"type\": \"string\"},\n" +
                "    \"text\": {\"type\": \"string\"},\n" +
                "    \"attachments\": {\n" +
                "       \"type\": \"array\",\n" +
                "       \"items\": {\n" +
                "           \"type\": \"object\",\n" +
                "           \"properties\": {\n" +
                "              \"filename\": {\"type\": \"string\"},\n" +
                "              \"contentType\": {\"type\": \"string\"},\n" +
                "              \"contentBase64\": {\"type\": \"string\"}\n" +
                "           },\n" +
                "           \"required\": [\"filename\", \"contentType\", \"contentBase64\"]\n" +
                "       }\n" +
                "    },\n" +
                "    \"useTls\": {\"type\": \"boolean\", \"default\": true}\n" +
                "  },\n" +
                "  \"required\": [\"host\", \"username\", \"password\", \"from\", \"to\", \"subject\"]\n" +
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
            String html = (String) args.getOrDefault("html", null);
            String text = (String) args.getOrDefault("text", "");
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) args.getOrDefault("attachments", Collections.emptyList());
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

            MimeMultipart mixed = new MimeMultipart("mixed");

            MimeBodyPart altPart = new MimeBodyPart();
            MimeMultipart alternative = new MimeMultipart("alternative");
            if (text != null && !text.isEmpty()) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(text, "UTF-8");
                alternative.addBodyPart(textPart);
            }
            if (html != null && !html.isEmpty()) {
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(html, "text/html; charset=UTF-8");
                alternative.addBodyPart(htmlPart);
            }
            altPart.setContent(alternative);
            mixed.addBodyPart(altPart);

            for (Map<String, Object> att : attachments) {
                MimeBodyPart attach = new MimeBodyPart();
                String filename = (String) att.get("filename");
                String contentType = (String) att.get("contentType");
                String contentBase64 = (String) att.get("contentBase64");
                attach.setDataHandler(new DataHandler(new javax.mail.util.ByteArrayDataSource(Base64.getDecoder().decode(contentBase64), contentType)));
                attach.setFileName(MimeUtility.encodeText(filename, "UTF-8", null));
                mixed.addBodyPart(attach);
            }

            message.setContent(mixed);
            Transport.send(message);
            return ToolExecuteResult.success("{\"status\":\"ok\"}");
        } catch (Exception e) {
            return ToolExecuteResult.fail("SMTP send(html/attachment) error: " + e.getMessage());
        }
    }
}


