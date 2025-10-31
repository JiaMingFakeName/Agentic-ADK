package com.alibaba.langengine.outlook;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OutlookToolsTest {

    private static final String OUTLOOK_TOKEN = System.getenv("OUTLOOK_ACCESS_TOKEN");

    @Test
    @Disabled("本地手动验证时，设置环境变量 OUTLOOK_ACCESS_TOKEN 后取消禁用")
    @DisplayName("Outlook 发送纯文本邮件")
    void sendEmail() {
        OutlookSendEmailTool tool = new OutlookSendEmailTool(OUTLOOK_TOKEN);
        String input = "{\"to\":\"someone@example.com\",\"subject\":\"Test\",\"body\":\"hello\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("Outlook 列表分页")
    void listEmails() {
        OutlookListEmailsTool tool = new OutlookListEmailsTool(OUTLOOK_TOKEN);
        String input = "{\"top\":5}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("Outlook 获取单封")
    void getEmail() {
        OutlookGetEmailTool tool = new OutlookGetEmailTool(OUTLOOK_TOKEN);
        String input = "{\"messageId\":\"<PUT_ID_HERE>\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("Outlook 批量已读 + 批量移动")
    void batchOps() {
        OutlookBatchMarkReadTool mark = new OutlookBatchMarkReadTool(OUTLOOK_TOKEN);
        ToolExecuteResult r1 = mark.run("{\"ids\":[\"id1\",\"id2\"]}", null);
        System.out.println(r1.getResult());

        OutlookBatchMoveFolderTool move = new OutlookBatchMoveFolderTool(OUTLOOK_TOKEN);
        ToolExecuteResult r2 = move.run("{\"ids\":[\"id1\",\"id2\"],\"destinationFolderId\":\"<FOLDER_ID>\"}", null);
        System.out.println(r2.getResult());
    }
}


