package com.alibaba.langengine.smtp;

import com.alibaba.langengine.core.tool.BaseTool;

import java.util.ArrayList;
import java.util.List;

public class SmtpToolFactory {

    public SmtpToolFactory() {}

    public SmtpSendEmailTool createSendEmailTool() {
        return new SmtpSendEmailTool();
    }

    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createSendEmailTool());
        tools.add(createSendHtmlOrAttachmentEmailTool());
        return tools;
    }

    public SmtpSendHtmlOrAttachmentEmailTool createSendHtmlOrAttachmentEmailTool() {
        return new SmtpSendHtmlOrAttachmentEmailTool();
    }
}


