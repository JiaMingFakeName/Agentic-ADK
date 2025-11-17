package com.alibaba.agentic.agent.dflow;

import com.google.adk.tools.ToolContext;
import com.google.genai.types.Part;
import io.a2a.spec.Message;
import io.a2a.spec.TextPart;
import io.reactivex.rxjava3.core.Single;

import java.util.Map;
import java.util.stream.Collectors;

import static com.alibaba.agentic.agent.dflow.LongRunningDFlowTool.DFLOW_TOOL_REPORT_ID;
import static com.alibaba.agentic.agent.dflow.LongRunningDFlowTool.TOOL_NAME;

public class A2AOuterToolFactory {
    public static boolean isOuterToolResponse(Message message) {
        return message.getMetadata().get(DFLOW_TOOL_REPORT_ID) != null;
    }

    public static void onReceive(String contextId, Message message) throws Exception {
        String reportId = String.valueOf(message.getMetadata().get(LongRunningDFlowTool.DFLOW_TOOL_REPORT_ID));

        LongRunningDFlowTool.report(reportId, message.getMetadata().get(TOOL_NAME).toString(),
                message.getParts().stream().map(A2AOuterToolFactory::getText)
                        .collect(Collectors.joining()));

    }

    private static String getText(io.a2a.spec.Part a2aPart) {
        return a2aPart instanceof TextPart ? ((TextPart)a2aPart).getText() : "";
    }

}
