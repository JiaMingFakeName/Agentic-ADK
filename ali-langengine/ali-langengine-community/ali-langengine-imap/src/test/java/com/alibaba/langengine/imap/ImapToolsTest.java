package com.alibaba.langengine.imap;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ImapToolsTest {

    @Test
    @Disabled("本地手动验证，请填入真实连接参数")
    @DisplayName("IMAP 列表")
    void listEmails() {
        ImapListEmailsTool tool = new ImapListEmailsTool();
        String input = "{\"host\":\"imap.example.com\",\"username\":\"u\",\"password\":\"p\",\"limit\":3}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("IMAP 搜索")
    void searchEmails() {
        ImapSearchEmailsTool tool = new ImapSearchEmailsTool();
        String input = "{\"host\":\"imap.example.com\",\"username\":\"u\",\"password\":\"p\",\"subject\":\"test\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }

    @Test
    @Disabled("本地手动验证")
    @DisplayName("IMAP 高级搜索")
    void advancedSearch() {
        ImapAdvancedSearchTool tool = new ImapAdvancedSearchTool();
        String input = "{\"host\":\"imap.example.com\",\"username\":\"u\",\"password\":\"p\",\"unread\":true,\"fromDate\":\"2025-01-01\",\"toDate\":\"2025-12-31\"}";
        ToolExecuteResult res = tool.run(input, null);
        System.out.println(res.getResult());
    }
}


