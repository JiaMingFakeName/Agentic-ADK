package com.alibaba.langengine.smtp;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SmtpToolsTest {

    @Test
    @Disabled("本地手动验证，请填入真实连接参数")
    @DisplayName("SMTP 文本发送")
    void sendText() {
        SmtpSendEmailTool tool = new SmtpSendEmailTool();
        String input = "{\"host\":\"smtp.example.com\",\"username\":\"u\",\"password\":\"p\",\"from\":\"u@example.com\",\"to\":\"a@example.com\",\"subject\":\"test\",\"body\":\"hello\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("SMTP HTML/附件发送")
    void sendHtmlAttachment() {
        SmtpSendHtmlOrAttachmentEmailTool tool = new SmtpSendHtmlOrAttachmentEmailTool();
        String input = "{\"host\":\"smtp.example.com\",\"username\":\"u\",\"password\":\"p\",\"from\":\"u@example.com\",\"to\":\"a@example.com\",\"subject\":\"test\",\"html\":\"<b>hello</b>\",\"attachments\":[]}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }
}


