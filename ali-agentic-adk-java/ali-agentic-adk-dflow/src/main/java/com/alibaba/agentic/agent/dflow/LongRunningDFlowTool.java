package com.alibaba.agentic.agent.dflow;

import com.alibaba.agentic.agent.dflow.tools.DFlowTool;
import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.InitEntry;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.fastjson.JSON;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;
import io.a2a.spec.*;
import io.reactivex.rxjava3.core.Single;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.agentic.agent.dflow.DFlowA2AExecutorFactory.*;

public class LongRunningDFlowTool extends BaseTool implements DFlowTool {

    private static final String CALLENTRYNAMEPREFIX = "_A2A_longRunningTool_Back_";
    public static final String DFLOW_TOOL_REPORT_ID = "dflow_tool_report_id";
    public static final String TOOL_NAME = "dflow_tool_name";

    private BaseTool tool;

    public LongRunningDFlowTool(BaseTool tool) {
        super(tool.name(), tool.description(), true);
        this.tool = tool;
    }

    public Single<Map<String, Object>> runAsync(Map<String, Object> args, ToolContext toolContext) {
        return tool.runAsync(args, toolContext);
    }
    private static String getCallEntryName(String toolName) {
        return CALLENTRYNAMEPREFIX + toolName;
    }

    @Override
    public DFlow<String> run(ContextStack contextStack, Map<String, Object> args, ToolContext toolContext) {
        StreamEmitter emitter = (StreamEmitter) contextStack.getGlobal().get(A2A_WRITE_BACK_QUEUE);
        Map<String, Object> res =  runAsync(args, toolContext).blockingGet();
        Map<String, Object> meta = new ConcurrentHashMap<>();
        meta.put("is_long_running",true);
        meta.put(TOOL_NAME,tool.name());
        meta.put(DFLOW_TOOL_REPORT_ID,contextStack.getId());

        TaskStatus taskStatus = new TaskStatus(TaskState.WORKING, new Message.Builder()
                .role(Message.Role.AGENT)
                .parts(new TextPart(JSON.toJSONString(res),
                        meta))
                .build(), LocalDateTime.now());
        String taskId = contextStack.get(TASK_ID);
        String contextId = contextStack.get(SESSION_ID);
        TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, taskStatus, contextId, Boolean.FALSE, new ConcurrentHashMap<>());

        emitter.systemEmit(contextStack.getId(), event);

        emitter.systemEmit(taskId, new TaskStatusUpdateEvent(
                        taskId,
                        new TaskStatus(TaskState.INPUT_REQUIRED),
                        contextStack.get(SESSION_ID),
                        false,
                        meta
                        )
                );

        return DFlow.fromCall(getCallEntryName(tool.name()));
    }

    public static void report(String dflowToolReportId, String toolName, String resMap) throws Exception {
        DFlow.call(new InitEntry.Entry(getCallEntryName(toolName)) , resMap, dflowToolReportId);
    }
}
