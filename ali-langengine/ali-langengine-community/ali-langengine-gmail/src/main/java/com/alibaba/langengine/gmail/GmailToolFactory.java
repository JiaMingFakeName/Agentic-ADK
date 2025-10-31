package com.alibaba.langengine.gmail;

import com.alibaba.langengine.core.tool.BaseTool;

import java.util.ArrayList;
import java.util.List;

public class GmailToolFactory {

    private final String accessToken;

    public GmailToolFactory(String accessToken) {
        this.accessToken = accessToken;
    }

    public GmailSendEmailTool createSendEmailTool() {
        return new GmailSendEmailTool(accessToken);
    }

    public GmailListEmailsTool createListEmailsTool() {
        return new GmailListEmailsTool(accessToken);
    }

    public GmailGetEmailTool createGetEmailTool() {
        return new GmailGetEmailTool(accessToken);
    }

    public GmailSendEmailWithAttachmentTool createSendEmailWithAttachmentTool() {
        return new GmailSendEmailWithAttachmentTool(accessToken);
    }

    public GmailGetAttachmentTool createGetAttachmentTool() {
        return new GmailGetAttachmentTool(accessToken);
    }

    public GmailMarkReadTool createMarkReadTool() {
        return new GmailMarkReadTool(accessToken);
    }

    public GmailBatchModifyLabelsTool createBatchModifyLabelsTool() {
        return new GmailBatchModifyLabelsTool(accessToken);
    }

    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createSendEmailTool());
        tools.add(createListEmailsTool());
        tools.add(createGetEmailTool());
        tools.add(createSendEmailWithAttachmentTool());
        tools.add(createGetAttachmentTool());
        tools.add(createMarkReadTool());
        tools.add(createBatchModifyLabelsTool());
        return tools;
    }
}


