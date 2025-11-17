package com.alibaba.agentic.langengine.tool;

import com.google.adk.models.LlmRequest;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Tool;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;

public class LangEngineTool extends BaseTool {
    com.alibaba.langengine.core.tool.BaseTool baseTool;

    protected LangEngineTool(com.alibaba.langengine.core.tool.BaseTool baseTool) {
        super(baseTool.getName(), baseTool.getDescription());
    }

    public Single<Map<String, Object>> runAsync(Map<String, Object> args, ToolContext toolContext) {

        throw new UnsupportedOperationException("This method is not implemented.");
    }

}
