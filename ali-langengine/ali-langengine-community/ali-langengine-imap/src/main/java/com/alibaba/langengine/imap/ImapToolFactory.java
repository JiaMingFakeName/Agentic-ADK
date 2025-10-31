package com.alibaba.langengine.imap;

import com.alibaba.langengine.core.tool.BaseTool;

import java.util.ArrayList;
import java.util.List;

public class ImapToolFactory {

    public ImapToolFactory() {}

    public ImapListEmailsTool createListEmailsTool() {
        return new ImapListEmailsTool();
    }

    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createListEmailsTool());
        tools.add(createSearchEmailsTool());
        tools.add(createGetEmailByUidTool());
        tools.add(createAdvancedSearchTool());
        return tools;
    }

    public ImapSearchEmailsTool createSearchEmailsTool() {
        return new ImapSearchEmailsTool();
    }

    public ImapGetEmailByUidTool createGetEmailByUidTool() {
        return new ImapGetEmailByUidTool();
    }

    public ImapAdvancedSearchTool createAdvancedSearchTool() {
        return new ImapAdvancedSearchTool();
    }
}


