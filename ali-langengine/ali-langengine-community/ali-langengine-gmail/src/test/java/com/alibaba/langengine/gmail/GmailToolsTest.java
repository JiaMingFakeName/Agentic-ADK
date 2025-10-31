package com.alibaba.langengine.gmail;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GmailToolsTest {

    private static final String GMAIL_TOKEN = System.getenv("GMAIL_ACCESS_TOKEN");

    @Test
    @Disabled("本地手动验证时，设置环境变量 GMAIL_ACCESS_TOKEN 后取消禁用")
    @DisplayName("Gmail 发送纯文本邮件")
    void sendEmail() {
        GmailSendEmailTool tool = new GmailSendEmailTool(GMAIL_TOKEN);
        String input = "{\"to\":\"someone@example.com\",\"subject\":\"Test\",\"body\":\"hello\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("Gmail 列表分页")
    void listEmails() {
        GmailListEmailsTool tool = new GmailListEmailsTool(GMAIL_TOKEN);
        String input = "{\"query\":\"in:inbox\",\"maxResults\":5}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("Gmail 获取单封")
    void getEmail() {
        GmailGetEmailTool tool = new GmailGetEmailTool(GMAIL_TOKEN);
        String input = "{\"messageId\":\"<PUT_ID_HERE>\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("Gmail 批量修改标签")
    void batchModifyLabels() {
        GmailBatchModifyLabelsTool tool = new GmailBatchModifyLabelsTool(GMAIL_TOKEN);
        String input = "{\"ids\":[\"id1\",\"id2\"],\"removeLabelIds\":[\"UNREAD\"]}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }
}


