package com.alibaba.agentic.agent.dflow.tools;

import com.alibaba.agentic.agent.dflow.LongRunningDFlowTool;
import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.ContextStack;
import com.alibaba.fastjson.JSON;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;

import java.util.Map;

public interface DFlowTool {
    DFlow<String> run(ContextStack contextStack, Map<String, Object> args, ToolContext toolContext);

    static DFlowTool of(BaseTool tool){
        if(tool instanceof DFlowTool){
            return (DFlowTool)tool;
        }
        if(tool.longRunning()){
            return new LongRunningDFlowTool(tool);
        }else{
            return new DFlowTool() {
                @Override
                public DFlow<String> run(ContextStack contextStack, Map<String, Object> args, ToolContext toolContext) {
                    return DFlow.just(JSON.toJSONString(tool.runAsync(args, toolContext).blockingGet()));
                }
            };
        }
    }
}
