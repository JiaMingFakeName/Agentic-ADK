package com.alibaba.langengine.outlook;

import com.alibaba.langengine.core.tool.BaseTool;

import java.util.ArrayList;
import java.util.List;

public class OutlookToolFactory {

    private final String accessToken;

    public OutlookToolFactory(String accessToken) {
        this.accessToken = accessToken;
    }

    public OutlookSendEmailTool createSendEmailTool() {
        return new OutlookSendEmailTool(accessToken);
    }

    public OutlookListEmailsTool createListEmailsTool() {
        return new OutlookListEmailsTool(accessToken);
    }

    public OutlookGetEmailTool createGetEmailTool() {
        return new OutlookGetEmailTool(accessToken);
    }

    public OutlookSendEmailWithAttachmentTool createSendEmailWithAttachmentTool() {
        return new OutlookSendEmailWithAttachmentTool(accessToken);
    }

    public OutlookGetAttachmentTool createGetAttachmentTool() {
        return new OutlookGetAttachmentTool(accessToken);
    }

    public OutlookMarkReadTool createMarkReadTool() {
        return new OutlookMarkReadTool(accessToken);
    }

    public OutlookBatchMarkReadTool createBatchMarkReadTool() {
        return new OutlookBatchMarkReadTool(accessToken);
    }

    public OutlookBatchMoveFolderTool createBatchMoveFolderTool() {
        return new OutlookBatchMoveFolderTool(accessToken);
    }

    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createSendEmailTool());
        tools.add(createListEmailsTool());
        tools.add(createGetEmailTool());
        tools.add(createSendEmailWithAttachmentTool());
        tools.add(createGetAttachmentTool());
        tools.add(createMarkReadTool());
        tools.add(createBatchMarkReadTool());
        tools.add(createBatchMoveFolderTool());
        return tools;
    }
}


