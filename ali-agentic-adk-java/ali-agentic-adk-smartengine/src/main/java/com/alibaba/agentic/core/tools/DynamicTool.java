package com.alibaba.agentic.core.tools;

import com.alibaba.agentic.core.engine.delegation.DelegationTool;
import com.alibaba.agentic.core.engine.delegation.domain.FunctionCallRequest;
import com.alibaba.agentic.core.engine.utils.LoopExecutionContextUtils;
import com.alibaba.agentic.core.executor.AsyncTaskResult;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/9/27 11:59
 */
@Component
@Data
@Accessors(chain = true)
@Slf4j
public class DynamicTool implements BaseTool {


    @Override
    public String name() {
        return "dynamicTool";
    }

    @Override
    public boolean supportAsync(SystemContext systemContext) {
        FunctionCallRequest request = (FunctionCallRequest) LoopExecutionContextUtils.getCurrentLoopItem(systemContext);
        log.info("adk smart engine DynamicTool.supportAsync invoke DelegationTool.getTool, request: {}", request);
        BaseTool baseTool = DelegationTool.getTool(request.getName());
        return baseTool.supportAsync(systemContext);
    }

    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        FunctionCallRequest request = (FunctionCallRequest) LoopExecutionContextUtils.getCurrentLoopItem(systemContext);
        log.info("adk smart engine DynamicTool.run invoke DelegationTool.getTool, request: {}", request);
        BaseTool baseTool = DelegationTool.getTool(request.getName());
        return baseTool.run(new ObjectMapper().convertValue(request.getToolParameter(), JSONObject.class), systemContext);
    }

    @Override
    public AsyncTaskResult asyncRun(String asyncTaskId, Map<String, Object> args, SystemContext systemContext) {
        FunctionCallRequest request = (FunctionCallRequest) LoopExecutionContextUtils.getCurrentLoopItem(systemContext);
        log.info("adk smart engine DynamicTool.asyncRun invoke DelegationTool.getTool, request: {}", request);
        BaseTool baseTool = DelegationTool.getTool(request.getName());
        return baseTool.asyncRun(asyncTaskId, new ObjectMapper().convertValue(request.getToolParameter(), JSONObject.class), systemContext);
    }
}
